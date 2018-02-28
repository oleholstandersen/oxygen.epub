package dk.nota.oxygen.common;

import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;

import ro.sync.document.DocumentPositionedInfo;

public abstract class AbstractWorkerWithResults
		extends SwingWorker<Object,Object>{
	
	private ResultsListener resultsListener;
	private String title;
	
	public AbstractWorkerWithResults(String title,
			ResultsListener resultsListener) {
		this.resultsListener = resultsListener;
		this.title = title;
	}
	
	@Override
	protected void done() {
		// Default behaviour: Write to results view that the task is done, or
		// print out exceptions
		try {
			// Catch exceptions from doInBackground()
			get();
			// Write to results view that update is done
			resultsListener.getResultsView().writeResult(title + " DONE");
		} catch (ExecutionException | InterruptedException e) {
			getResultsListener().writeException(
					e instanceof ExecutionException ? e.getCause() : e,
					DocumentPositionedInfo.SEVERITY_FATAL);
		}
	}
	
	public ResultsListener getResultsListener() {
		return resultsListener;
	}

}
