package dk.nota.oxygen.epub.plugin;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;

import dk.nota.oxygen.epub.common.EpubAccess;
import net.sf.saxon.s9api.SaxonApiException;
import ro.sync.exml.workspace.api.editor.WSEditor;
import ro.sync.exml.workspace.api.listeners.WSEditorChangeListener;
import ro.sync.exml.workspace.api.standalone.StandalonePluginWorkspace;

public class WorkspaceSetupListener extends WSEditorChangeListener {
	
		private HashMap<String,EpubAccess> epubAccessMap =
				new HashMap<String,EpubAccess>();
		private StandalonePluginWorkspace pluginWorkspace;
		
		public WorkspaceSetupListener(StandalonePluginWorkspace pluginWorkspace) {
			this.pluginWorkspace = pluginWorkspace;
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
			EpubPluginExtension.getQuickbaseMenu().updateForEpub(getEpubAccess(
					editorUrl.toString()));
		}
		
		public EpubAccess getEpubAccess(String url) {
			url = url.replaceFirst("^zip:", "").replaceFirst("\\.epub!/*.*?$",
					".epub");
			return epubAccessMap.get(url);
		}
		
		private void hideAllEpubToolbars() {
			pluginWorkspace.hideToolbar(EpubPluginExtension.NAV_TOOLBAR);
			pluginWorkspace.hideToolbar(EpubPluginExtension.OPF_TOOLBAR);
			pluginWorkspace.hideToolbar(EpubPluginExtension.XHTML_TOOLBAR);
		}
		
		private boolean setupEpubAccess(URL editorUrl) {
			String epubUrl = editorUrl.toString().replaceFirst("^zip:",  "")
					.replaceFirst("\\.epub!/*.*?$", ".epub");
			if (epubAccessMap.containsKey(epubUrl)) return true;
			try {
				EpubAccess epubAccess = new EpubAccess(editorUrl);
				epubAccessMap.put(epubUrl, epubAccess);
				return true;
			} catch (IOException | SaxonApiException e) {
				e.printStackTrace();
				return false;
			}
		}
		
	}