package dk.nota.oxygen.common;

import ro.sync.document.DocumentPositionedInfo;
import ro.sync.exml.workspace.api.PluginWorkspace;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;
import ro.sync.exml.workspace.api.results.ResultsManager;

public class ResultsView {
	
	private ResultsManager resultsManager;
	private String title;
	
	public ResultsView(String title) {
		PluginWorkspace pluginWorkspace = PluginWorkspaceProvider
				.getPluginWorkspace();
		resultsManager = pluginWorkspace.getResultsManager();
		resultsManager.setResults(title, null, null);
		this.title = title;
	}
	
	public void writeResult(DocumentPositionedInfo documentInfo) {
		resultsManager.addResult(title, documentInfo, ResultsManager.ResultType
				.GENERIC, true, true);
	}
	
	public void writeResult(String string) {
		writeResult(new DocumentPositionedInfo(DocumentPositionedInfo
				.SEVERITY_INFO, string));
	}
	
	public void writeResult(String message, String systemId) {
		writeResult(new DocumentPositionedInfo(DocumentPositionedInfo
				.SEVERITY_INFO, message, systemId));
	}

}
