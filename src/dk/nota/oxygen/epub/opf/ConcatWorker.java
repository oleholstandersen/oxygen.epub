package dk.nota.oxygen.epub.opf;

import java.net.URL;
import java.util.LinkedList;

import javax.xml.transform.SourceLocator;

import dk.nota.oxygen.common.AbstractConsoleWorker;
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

public class ConcatWorker extends AbstractConsoleWorker {
	
	private LinkedList<String> documentPaths = new LinkedList<String>();
	private EditorAccess editorAccess;
	private EpubAccess epubAccess;
	private ConsoleListener messageListener;
	private boolean success = false;
	
	public ConcatWorker(EditorAccess editorAccess, EpubAccess epubAccess,
			ConsoleWindow consoleWindow) {
		super(consoleWindow);
		this.editorAccess = editorAccess;
		this.epubAccess = epubAccess;
		this.messageListener = new DocumentListener(consoleWindow);
	}

	@Override
	protected Object doInBackground() throws Exception {
		XsltTransformer concatTransformer = epubAccess.getConcatTransformer(
				messageListener, messageListener);
		concatTransformer.setParameter(new QName("UPDATE_EPUB"),
				new XdmAtomicValue(true));
		XsltTransformer outputTransformer = epubAccess.getOutputTransformer(
				messageListener, messageListener);
		concatTransformer.setDestination(outputTransformer);
		concatTransformer.transform();
		messageListener.writeToConsole("DELETING DOCUMENTS...");
		for (String documentPath : documentPaths) {
			messageListener.writeToConsole("Deleting " + documentPath);
			editorAccess.getWorkspace().delete(new URL(epubAccess
					.getContentFolderUrl(), documentPath));
		}
		success = true;
		return null;
	}
	
	@Override
	protected void done() {
		if (success) messageListener.writeToConsole("CONCAT DONE");
	}
	
	private class DocumentListener extends ConsoleListener {
		
		public DocumentListener(ConsoleWindow consoleWindow) {
			super(consoleWindow);
		}
		
		@Override
		public void handleMessage(XdmNode message, boolean terminate,
				SourceLocator sourceLocator) {
			XdmSequenceIterator messageIterator = message.axisIterator(Axis
					.DESCENDANT_OR_SELF, new QName(XmlAccess.NOTA_NAMESPACE,
							"document"));
			while (messageIterator.hasNext()) documentPaths.add(messageIterator
					.next().getStringValue());						
		}
		
	}

}
