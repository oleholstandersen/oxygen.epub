package dk.nota.oxygen.epub.common;

import dk.nota.oxygen.common.AbstractResultsWorker;
import dk.nota.oxygen.common.ResultsView;
import dk.nota.oxygen.xml.ResultsViewListener;

public class AbstractEpubResultsWorker extends AbstractResultsWorker {
	
	private EpubAccess epubAccess;
	
	public AbstractEpubResultsWorker(ResultsView resultsView,
			ResultsViewListener resultsViewListener, EpubAccess epubAccess) {
		super(resultsView, resultsViewListener);
		this.epubAccess = epubAccess;
	}

	@Override
	protected Object doInBackground() throws Exception {
		return null;
	}
	
	public EpubAccess getEpubAccess() {
		return epubAccess;
	}

}
