package dk.nota.oxygen.epub.opf;

import java.net.MalformedURLException;
import java.util.LinkedList;

import javax.swing.SwingWorker;
import javax.xml.transform.SourceLocator;

import de.schlichtherle.io.File;
import dk.nota.oxygen.common.ConsoleWindow;
import dk.nota.oxygen.common.EditorAccess;
import dk.nota.oxygen.epub.common.EpubAccess;
import dk.nota.oxygen.xml.ConsoleListener;
import dk.nota.oxygen.xml.XmlAccess;
import net.sf.saxon.s9api.Axis;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmSequenceIterator;
import net.sf.saxon.s9api.XsltTransformer;

public class CreateDtbWorker extends SwingWorker<Object,Object> {
	
	private EditorAccess editorAccess;
	private EpubAccess epubAccess;
	private LinkedList<String> imagePaths = new LinkedList<String>();
	private ImageListener messageListener;
	private java.io.File dtbFile;
	private boolean success = false;
	
	public CreateDtbWorker(EditorAccess editorAccess, EpubAccess epubAccess,
			ConsoleWindow consoleWindow, java.io.File dtbFile) {
		this.editorAccess = editorAccess;
		this.epubAccess = epubAccess;
		this.messageListener = new ImageListener(consoleWindow);
		this.dtbFile = dtbFile;
	}

	@Override
	protected Object doInBackground() throws Exception {
		XsltTransformer concatTransformer = epubAccess.getConcatTransformer(
				messageListener, messageListener);
		XsltTransformer dtbConverter = epubAccess.getDtbConverter(
				messageListener, messageListener);
		dtbConverter.setDestination(epubAccess.getXmlAccess().getSerializer(
				dtbFile));
		dtbConverter.setParameter(new QName("NAV_DOCUMENT"), epubAccess
				.getNavigationDocument());
		concatTransformer.setDestination(dtbConverter);
		concatTransformer.setParameter(new QName("UPDATE_EPUB"),
				new XdmAtomicValue(false));
		concatTransformer.transform();
		messageListener.writeToConsole("MOVING IMAGE FILES...");
		for (String imagePath : imagePaths) {
			messageListener.writeToConsole("Copying " + imagePath);
			File imageFile = epubAccess.getFileFromContentFolder(imagePath);
			File newImageFile = new File(dtbFile.getParentFile(), imageFile
					.getName());
			newImageFile.archiveCopyFrom(imageFile);
		}
		success = true;
		return null;
	}
	
	@Override
	protected void done() {
		if (success) {
			messageListener.writeToConsole("DONE");
			try {
				editorAccess.getWorkspace().open(dtbFile.toURI().toURL());
			} catch (MalformedURLException e) {
				editorAccess.showErrorMessage(e.toString());
			}
		}
	}
	
	public class ImageListener extends ConsoleListener {
		
		public ImageListener(ConsoleWindow consoleWindow) {
			super(consoleWindow);
		}
		
		@Override
		public void handleMessage(XdmNode message, boolean terminate,
				SourceLocator sourceLocator) {
			XdmSequenceIterator messageIterator = message.axisIterator(Axis
					.DESCENDANT_OR_SELF, new QName(XmlAccess.NOTA_NAMESPACE,
							"image"));
			while (messageIterator.hasNext()) imagePaths.add(messageIterator
					.next().getStringValue());						
		}
		
	}

}
