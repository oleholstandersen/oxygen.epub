package dk.nota.oxygen.epub.common;

import dk.nota.oxygen.common.AbstractWorkerWithResults;
import dk.nota.oxygen.common.ResultsListener;

public abstract class AbstractEpubWorkerWithResults<T,E>
		extends AbstractWorkerWithResults<T,E> {

	private EpubAccess epubAccess;
	
	public AbstractEpubWorkerWithResults(String title, 
			ResultsListener resultsListener, EpubAccess epubAccess) {
		super(title, resultsListener);
		this.epubAccess = epubAccess;
	}
	
	public EpubAccess getEpubAccess() {
		return epubAccess;
	}

}
