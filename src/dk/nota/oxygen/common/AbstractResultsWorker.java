package dk.nota.oxygen.common;

import javax.swing.SwingWorker;

import dk.nota.oxygen.xml.ResultsViewListener;

public abstract class AbstractResultsWorker extends SwingWorker<Object,Object> {
	
	private ResultsView resultsView;
	private ResultsViewListener resultsViewListener;
	private boolean success = false;
	
	public AbstractResultsWorker(ResultsView resultsView,
			ResultsViewListener resultsViewListener) {
		this.resultsView = resultsView;
		this.resultsViewListener = resultsViewListener;
	}

	@Override
	protected abstract Object doInBackground() throws Exception;
	
	public ResultsViewListener getListener() {
		return resultsViewListener;
	}
	
	public ResultsView getResultsView() {
		return resultsView;
	}
	
	public boolean getSuccess() {
		return success;
	}
	
	public void setSuccess() {
		success = true;
	}
}
