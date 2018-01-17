package dk.nota.oxygen.common;

import javax.swing.SwingWorker;

public abstract class AbstractResultsWorker extends SwingWorker<Object,Object> {
	
	private ResultsView resultsView;
	
	public AbstractResultsWorker(ResultsView consoleWindow) {
		this.resultsView = consoleWindow;
	}

	@Override
	protected abstract Object doInBackground() throws Exception;
	
	public ResultsView getResultsView() {
		return resultsView;
	}
}
