package dk.nota.oxygen.epub.opf;

import java.net.MalformedURLException;
import java.util.LinkedList;

import javax.xml.transform.SourceLocator;

import de.schlichtherle.io.File;
import dk.nota.oxygen.common.AbstractConsoleWorker;
import dk.nota.oxygen.common.ConsoleWindow;
import dk.nota.oxygen.common.EditorAccess;
import dk.nota.oxygen.epub.common.EpubAccess;
import dk.nota.oxygen.xml.ConsoleListener;
import dk.nota.oxygen.xml.XmlAccess;
import net.sf.saxon.s9api.Axis;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.XdmDestination;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmSequenceIterator;
import net.sf.saxon.s9api.XsltTransformer;

public class CreateDtbWorker extends AbstractConsoleWorker {
	
	private java.io.File dtbFile;
	private EditorAccess editorAccess;
	private EpubAccess epubAccess;
	private ImageListener imageListener;
	private boolean returnDtbDocument = false;
	private boolean success = false;
	
	public CreateDtbWorker(EditorAccess editorAccess, EpubAccess epubAccess,
			ConsoleWindow consoleWindow, java.io.File dtbFile,
			boolean returnDtbDocument) {
		super(consoleWindow);
		this.dtbFile = dtbFile;
		this.editorAccess = editorAccess;
		this.epubAccess = epubAccess;
		this.imageListener = new ImageListener(consoleWindow);
		this.returnDtbDocument = returnDtbDocument;
	}

	@Override
	protected Object doInBackground() throws Exception {
		XsltTransformer concatTransformer = epubAccess.getConcatTransformer(
				imageListener, imageListener);
		XsltTransformer dtbConverter = epubAccess.getDtbConverter(
				imageListener, imageListener);
		if (returnDtbDocument) {
			dtbConverter.setDestination(new XdmDestination());
			((XdmDestination)dtbConverter.getDestination()).setBaseURI(dtbFile
					.toURI());
		} else dtbConverter.setDestination(epubAccess.getXmlAccess()
				.getSerializer(dtbFile));
		dtbConverter.setParameter(new QName("NAV_DOCUMENT"), epubAccess
				.getNavigationDocument());
		concatTransformer.setDestination(dtbConverter);
		concatTransformer.setParameter(new QName("UPDATE_EPUB"),
				new XdmAtomicValue(false));
		concatTransformer.transform();
		imageListener.writeToConsole("COPYING IMAGE FILES...");
		for (String imagePath : imageListener.getImagePaths()) {
			imageListener.writeToConsole("Copying " + imagePath);
			File imageFile = epubAccess.getFileFromContentFolder(imagePath);
			File newImageFile = new File(dtbFile.getParentFile(), imageFile
					.getName());
			newImageFile.archiveCopyFrom(imageFile);
		}
		success = true;
		if (returnDtbDocument) return ((XdmDestination)dtbConverter
				.getDestination()).getXdmNode();
		return null;
	}
	
	@Override
	protected void done() {
		if (success) {
			imageListener.writeToConsole("DTBOOK CONVERSION DONE");
			try {
				if (!returnDtbDocument) editorAccess.getWorkspace().open(
						dtbFile.toURI().toURL());
			} catch (MalformedURLException e) {
				editorAccess.showErrorMessage(e.toString());
			}
		}
	}
	
	public class ImageListener extends ConsoleListener {
		
		private LinkedList<String> imagePaths = new LinkedList<String>();
		
		public ImageListener(ConsoleWindow consoleWindow) {
			super(consoleWindow);
		}
		
		public LinkedList<String> getImagePaths() {
			return imagePaths;
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
