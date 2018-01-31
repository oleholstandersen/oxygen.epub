package dk.nota.oxygen.epub.opf;

import java.net.URL;
import dk.nota.oxygen.common.EditorAccess;
import dk.nota.oxygen.common.ResultsView;
import dk.nota.oxygen.common.ResultsViewDocumentListener;
import dk.nota.oxygen.epub.common.AbstractEpubResultsWorker;
import dk.nota.oxygen.epub.common.EpubAccess;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.XsltTransformer;

public class ConcatWorker extends AbstractEpubResultsWorker {
	
	private EditorAccess editorAccess;
	
	public ConcatWorker(EditorAccess editorAccess, EpubAccess epubAccess,
			ResultsView resultsView) {
		super(resultsView, new ResultsViewDocumentListener(resultsView),
				epubAccess);
		this.editorAccess = editorAccess;
	}

	@Override
	protected Object doInBackground() throws Exception {
		XsltTransformer concatTransformer = getEpubAccess()
				.getConcatTransformer(getListener(), getListener());
		concatTransformer.setParameter(new QName("UPDATE_EPUB"),
				new XdmAtomicValue(true));
		XsltTransformer outputTransformer = getEpubAccess()
				.getOutputTransformer(getListener(), getListener());
		concatTransformer.setDestination(outputTransformer);
		concatTransformer.transform();
		getListener().writeToResultsView("DELETING DOCUMENTS...");
		for (String documentPath : ((ResultsViewDocumentListener)getListener())
				.getDocumentPaths()) {
			getListener().writeToResultsView("Deleting " + documentPath);
			editorAccess.getWorkspace().delete(new URL(getEpubAccess()
					.getContentFolderUrl(), documentPath));
		}
		setSuccess();
		return null;
	}
	
	@Override
	protected void done() {
		if (getSuccess()) getListener().writeToResultsView("CONCAT DONE",
				getEpubAccess().getContentFolderUrl() + "concat.xhtml");
	}

}
