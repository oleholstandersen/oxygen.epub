package dk.nota.oxygen.epub.plugin;

import java.io.IOException;
import java.net.URL;

import dk.nota.oxygen.epub.common.EpubAccess;
import net.sf.saxon.s9api.SaxonApiException;
import ro.sync.exml.workspace.api.editor.WSEditor;
import ro.sync.exml.workspace.api.listeners.WSEditorChangeListener;
import ro.sync.exml.workspace.api.standalone.StandalonePluginWorkspace;

public class WorkspaceSetupListener extends WSEditorChangeListener {
	
		private EpubAccess epubAccess;
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
			if (editor == null || editor.getDocumentTypeInformation() == null) {
				setupOtherWorkspace();
				return;
			}
			String documentType = editor.getDocumentTypeInformation().getName();
			if (documentType.equals("XHTML [EPUB 3]"))
				setupXhtmlWorkspace(editor);
			else if (documentType.equals("OPF"))
				setupOPFWorkspace(editor);
			else if (documentType.equals("NCX"))
				setupNavWorkspace(editor);
			else setupOtherWorkspace();
		}
		
		public EpubAccess getEpubAccess() {
			return epubAccess;
		}
		
		private void hideAllEpubToolbars() {
			pluginWorkspace.hideToolbar(EpubPluginExtension.NAV_TOOLBAR);
			pluginWorkspace.hideToolbar(EpubPluginExtension.OPF_TOOLBAR);
			pluginWorkspace.hideToolbar(EpubPluginExtension.XHTML_TOOLBAR);
		}
		
		private boolean setupEpubAccess(WSEditor editor) {
			try {
				epubAccess = new EpubAccess(editor);
				return true;
			} catch (IOException | SaxonApiException e) {
				return false;
			}
		}
		
		private void setupNavWorkspace(WSEditor editor) {
			hideAllEpubToolbars();
			if (setupEpubAccess(editor))
				pluginWorkspace.showToolbar(EpubPluginExtension.NAV_TOOLBAR);
		}
		
		private void setupOPFWorkspace(WSEditor editor) {
			hideAllEpubToolbars();
			if (setupEpubAccess(editor))
				pluginWorkspace.showToolbar(EpubPluginExtension.OPF_TOOLBAR);
		}
		
		private void setupOtherWorkspace() {
			hideAllEpubToolbars();
		}
		
		private void setupXhtmlWorkspace(WSEditor editor) {
			hideAllEpubToolbars();
			if (setupEpubAccess(editor)) {
				if (editor.getEditorLocation().toString().endsWith("/nav.xhtml"))
					pluginWorkspace.showToolbar(EpubPluginExtension.NAV_TOOLBAR);
				else pluginWorkspace.showToolbar(EpubPluginExtension.XHTML_TOOLBAR);
			}
		}
		
	}