package dk.nota.quickbase;

import ro.sync.exml.workspace.api.PluginWorkspaceProvider;

public class QuickbaseAccessProvider {
	
	private static QuickbaseAccess quickbaseAccess;
	
	static {
		quickbaseAccess = new QuickbaseAccess(PluginWorkspaceProvider
				.getPluginWorkspace());
	}
	
	private QuickbaseAccessProvider() {
		
	}
	
	public static QuickbaseAccess getQuickbaseAccess() {
		return quickbaseAccess;
	}

}
