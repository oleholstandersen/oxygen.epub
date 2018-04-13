package dk.nota.oxygen.epub.plugin;

import java.net.URL;

import dk.nota.epub.EpubAccessProvider;
import dk.nota.epub.EpubException;
import dk.nota.oxygen.EditorAccess;
import ro.sync.exml.workspace.api.editor.WSEditor;
import ro.sync.exml.workspace.api.listeners.WSEditorChangeListener;
import ro.sync.exml.workspace.api.standalone.StandalonePluginWorkspace;

public class WorkspaceSetupListener extends WSEditorChangeListener {
	
		private StandalonePluginWorkspace pluginWorkspace;
		
		public WorkspaceSetupListener(StandalonePluginWorkspace pluginWorkspace) {
			this.pluginWorkspace = pluginWorkspace;
		}
		
		@Override
		public void editorClosed(URL editorUrl) {
			EpubPluginExtension.getQuickbaseMenu().updateForEpub(null);
		}
		
		@Override
		public void editorOpened(URL editorUrl) {
			establishWorkspace(editorUrl);
		}
		
		@Override
		public void editorRelocated(URL previousEditorUrl, URL newEditorUrl) {
			establishWorkspace(newEditorUrl);
		}
		
		@Override
		public void editorSelected(URL editorUrl) {
			establishWorkspace(editorUrl);
		}
		
		private void establishWorkspace(URL editorUrl) {
			WSEditor editor = pluginWorkspace.getEditorAccess(editorUrl,
					StandalonePluginWorkspace.MAIN_EDITING_AREA);
			hideAllEpubToolbars();
			if (editor == null || editor.getDocumentTypeInformation() == null)
				return;
			switch (editor.getDocumentTypeInformation().getName()) {
			case "XHTML [EPUB 3]":
				if (!setupEpubAccess(editorUrl)) return;
				if (editorUrl.toString().endsWith("/nav\\.xhtml"))
					pluginWorkspace.showToolbar(EpubPluginExtension
							.NAV_TOOLBAR);
				else pluginWorkspace.showToolbar(EpubPluginExtension
						.XHTML_TOOLBAR);
				break;
			case "OPF":
				if (!setupEpubAccess(editorUrl)) return;
				pluginWorkspace.showToolbar(EpubPluginExtension.OPF_TOOLBAR);
				break;
			case "NCX":
				if (!setupEpubAccess(editorUrl)) return;
				pluginWorkspace.showToolbar(EpubPluginExtension.NAV_TOOLBAR);
			}
			try {
				EpubPluginExtension.getQuickbaseMenu().updateForEpub(
						EpubAccessProvider.getEpubAccess(EditorAccess
								.getArchiveUri(editorUrl)));
			} catch (EpubException e) {
				e.printStackTrace();
			}
		}
		
		private void hideAllEpubToolbars() {
			pluginWorkspace.hideToolbar(EpubPluginExtension.NAV_TOOLBAR);
			pluginWorkspace.hideToolbar(EpubPluginExtension.OPF_TOOLBAR);
			pluginWorkspace.hideToolbar(EpubPluginExtension.XHTML_TOOLBAR);
		}
		
		private boolean setupEpubAccess(URL editorUrl) {
			try {
				EpubAccessProvider.getEpubAccess(EditorAccess.getArchiveUri(
						editorUrl));
			} catch (EpubException e) {
				return false;
			}
			return true;
		}
		
	}