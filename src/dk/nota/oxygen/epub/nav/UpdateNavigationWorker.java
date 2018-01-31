package dk.nota.oxygen.epub.nav;

import dk.nota.oxygen.common.ResultsView;
import dk.nota.oxygen.epub.common.AbstractEpubResultsWorker;
import dk.nota.oxygen.epub.common.EpubAccess;
import dk.nota.oxygen.xml.ResultsViewListener;
import net.sf.saxon.s9api.XsltTransformer;

public class UpdateNavigationWorker extends AbstractEpubResultsWorker {
	
	public UpdateNavigationWorker(EpubAccess epubAccess,
			ResultsView resultsView) {
		super(resultsView, new ResultsViewListener(resultsView), epubAccess);
	}

	@Override
	protected Object doInBackground() throws Exception {
		XsltTransformer navigationTransformer = getEpubAccess()
				.getNavUpdateTransformer(getListener(), getListener());
		XsltTransformer outputTransformer = getEpubAccess()
				.getOutputTransformer(getListener(), getListener());
		navigationTransformer.setDestination(outputTransformer);
		navigationTransformer.transform();
		setSuccess();
		return null;
	}
	
	@Override
	protected void done() {
		if (getSuccess()) getListener().writeToResultsView(
				"NAVIGATION UPDATE DONE");
	}

}
