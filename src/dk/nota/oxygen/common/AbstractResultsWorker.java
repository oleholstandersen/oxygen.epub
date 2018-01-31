package dk.nota.oxygen.common;

import javax.swing.SwingWorker;

public abstract class AbstractResultsWorker extends SwingWorker<Object,Object> {
	
	private ResultsView resultsView;
	
	public AbstractResultsWorker(ResultsView resultsView) {
		this.resultsView = resultsView;
	}

	@Override
	protected abstract Object doInBackground() throws Exception;
	
	public ResultsView getResultsView() {
		return resultsView;
	}
}
