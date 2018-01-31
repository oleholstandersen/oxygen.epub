package dk.nota.oxygen.epub.opf;

import dk.nota.oxygen.common.ResultsView;
import dk.nota.oxygen.epub.common.AbstractEpubResultsWorker;
import dk.nota.oxygen.epub.common.EpubAccess;
import dk.nota.oxygen.xml.ResultsViewListener;
import net.sf.saxon.s9api.XsltTransformer;

public class SplitWorker extends AbstractEpubResultsWorker {
	
	public SplitWorker(EpubAccess epubAccess, ResultsView resultsView) {
		super(resultsView, new ResultsViewListener(resultsView), epubAccess);
	}

	@Override
	protected Object doInBackground() throws Exception {
		XsltTransformer splitTransformer = getEpubAccess().getSplitTransformer(
				getListener(), getListener());
		XsltTransformer outputTransformer = getEpubAccess()
				.getOutputTransformer(getListener(), getListener());
		splitTransformer.setDestination(outputTransformer);
		splitTransformer.transform();
		setSuccess();
		return null;
	}
	
	@Override
	protected void done() {
		if (getSuccess()) getListener().writeToResultsView("SPLIT DONE");
	}

}