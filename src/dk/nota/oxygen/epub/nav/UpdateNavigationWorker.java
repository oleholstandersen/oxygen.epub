package dk.nota.oxygen.epub.nav;

import dk.nota.oxygen.common.AbstractResultsWorker;
import dk.nota.oxygen.common.ResultsView;
import dk.nota.oxygen.epub.common.EpubAccess;
import dk.nota.oxygen.xml.ResultsViewListener;
import net.sf.saxon.s9api.XsltTransformer;

public class UpdateNavigationWorker extends AbstractResultsWorker {
	
	private EpubAccess epubAccess;
	private ResultsViewListener messageListener;
	private boolean success = false;
	
	public UpdateNavigationWorker(EpubAccess epubAccess,
			ResultsView resultsView) {
		super(resultsView);
		this.epubAccess = epubAccess;
		this.messageListener = new ResultsViewListener(resultsView);
	}

	@Override
	protected Object doInBackground() throws Exception {
		XsltTransformer navigationTransformer = epubAccess
				.getNavUpdateTransformer(messageListener, messageListener);
		XsltTransformer outputTransformer = epubAccess.getOutputTransformer(
				messageListener, messageListener);
		navigationTransformer.setDestination(outputTransformer);
		navigationTransformer.transform();
		success = true;
		return null;
	}
	
	@Override
	protected void done() {
		if (success) messageListener.writeToResultsView("NAVIGATION UPDATE DONE");
	}

}
