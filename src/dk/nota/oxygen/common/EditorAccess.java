package dk.nota.oxygen.common;

import java.io.File;
import java.net.URL;
import dk.nota.oxygen.epub.common.EpubAccess;
import dk.nota.oxygen.epub.plugin.WorkspaceSetupListener;
import ro.sync.exml.workspace.api.PluginWorkspace;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;
import ro.sync.exml.workspace.api.editor.WSEditor;
import ro.sync.exml.workspace.api.listeners.WSEditorChangeListener;
import ro.sync.exml.workspace.api.standalone.StandalonePluginWorkspace;
import ro.sync.util.editorvars.EditorVariables;

public class EditorAccess {
	
	WSEditor editor;
	
	public EditorAccess(WSEditor editor) {
		this.editor = editor;
	}
	
	public File[] chooseFiles(String title, String[] extensions,
			String description) {
		return getWorkspace().chooseFiles(null, title, extensions, description);
	}
	
	public URL[] getAllEditorUrls() {
		return getWorkspace().getAllEditorLocations(
				StandalonePluginWorkspace.MAIN_EDITING_AREA);
	}
	
	public WSEditor getCurrentEditor() {
		return editor;
	}
	
	public URL getCurrentEditorUrl() {
		return editor.getEditorLocation();
	}
	
	public WSEditor getEditor(URL url) {
		WSEditor editor = getWorkspace().getEditorAccess(url,
				StandalonePluginWorkspace.MAIN_EDITING_AREA);
		if (editor == null) {
			if (!open(url)) return null;
			editor = getWorkspace().getEditorAccess(url,
					StandalonePluginWorkspace.MAIN_EDITING_AREA);
		}
		return editor;
	}
	
	public EpubAccess getEpubAccess() {
		return getWorkspaceSetupListener().getEpubAccess(getCurrentEditorUrl()
				.toString());
	}
	
	public WSEditorChangeListener[] getEditorChangeListeners() {
		return getWorkspace().getEditorChangeListeners(
				StandalonePluginWorkspace.MAIN_EDITING_AREA);
	}
	
	public WorkspaceSetupListener getWorkspaceSetupListener() {
		for (WSEditorChangeListener listener : getEditorChangeListeners()) {
			if (listener instanceof WorkspaceSetupListener)
				return (WorkspaceSetupListener)listener;
		}
		return null;
	}
	
	public PluginWorkspace getWorkspace() {
		return PluginWorkspaceProvider.getPluginWorkspace();
	}
	
	public String expandEditorVariable(String editorVariable) {
		return EditorVariables.expandEditorVariables(editorVariable,
				editor.getEditorLocation().toString());
	}
	
	public boolean open(URL url) {
		return getWorkspace().open(url);
	}
	
	public boolean openInAuthorMode(URL url) {
		return getWorkspace().open(url, WSEditor.PAGE_AUTHOR);
	}
	
	public void saveCurrentEditor() {
		editor.save();
	}
	
	public void showErrorMessage(String message) {
		getWorkspace().showErrorMessage(message);
	}
	
	public void showInformationMessage(String message) {
		getWorkspace().showInformationMessage(message);
	}
	
	public void showStatusMessage(String message) {
		getWorkspace().showStatusMessage(message);
	}
	
	public boolean unsavedChangesInArchive(URL archiveUrl) {
		String archiveUrlAsString = archiveUrl.toString();
		for (URL editorUrl : getAllEditorUrls()) {
			if (editorUrl.toString().contains(archiveUrlAsString)) {
				WSEditor editor = getWorkspace().getEditorAccess(editorUrl,
						StandalonePluginWorkspace.MAIN_EDITING_AREA);
				if (editor.isModified()) {
					return true;
				}
			}
		}
		return false;
	}

}
