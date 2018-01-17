package dk.nota.oxygen.epub.opf;

import java.net.URL;
import java.util.LinkedList;

import javax.xml.transform.SourceLocator;

import dk.nota.oxygen.common.AbstractResultsWorker;
import dk.nota.oxygen.common.EditorAccess;
import dk.nota.oxygen.common.ResultsView;
import dk.nota.oxygen.epub.common.EpubAccess;
import dk.nota.oxygen.xml.ResultsViewListener;
import dk.nota.oxygen.xml.XmlAccess;
import net.sf.saxon.s9api.Axis;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmSequenceIterator;
import net.sf.saxon.s9api.XsltTransformer;

public class ConcatWorker extends AbstractResultsWorker {
	
	private LinkedList<String> documentPaths = new LinkedList<String>();
	private EditorAccess editorAccess;
	private EpubAccess epubAccess;
	private ResultsViewListener messageListener;
	private boolean success = false;
	
	public ConcatWorker(EditorAccess editorAccess, EpubAccess epubAccess,
			ResultsView resultsView) {
		super(resultsView);
		this.editorAccess = editorAccess;
		this.epubAccess = epubAccess;
		this.messageListener = new DocumentListener(resultsView);
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
		messageListener.writeToResultsView("DELETING DOCUMENTS...");
		for (String documentPath : documentPaths) {
			messageListener.writeToResultsView("Deleting " + documentPath);
			editorAccess.getWorkspace().delete(new URL(epubAccess
					.getContentFolderUrl(), documentPath));
		}
		success = true;
		return null;
	}
	
	@Override
	protected void done() {
		if (success) messageListener.writeToResultsView("CONCAT DONE",
				epubAccess.getContentFolderUrl() + "concat.xhtml");
	}
	
	private class DocumentListener extends ResultsViewListener {
		
		public DocumentListener(ResultsView resultsView) {
			super(resultsView);
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
