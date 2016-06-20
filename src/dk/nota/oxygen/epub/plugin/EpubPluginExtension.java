package dk.nota.oxygen.epub.plugin;

import javax.swing.JComponent;

import dk.nota.oxygen.common.EditorAccess;
import dk.nota.oxygen.common.TreeTestAction;
import dk.nota.oxygen.epub.common.ImportDocxAction;
import dk.nota.oxygen.epub.nav.UpdateNavigationAction;
import dk.nota.oxygen.epub.opf.ConcatAction;
import dk.nota.oxygen.epub.opf.CreateDtbAction;
import dk.nota.oxygen.epub.opf.SplitAction;
import ro.sync.exml.plugin.workspace.WorkspaceAccessPluginExtension;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;
import ro.sync.exml.workspace.api.editor.WSEditor;
import ro.sync.exml.workspace.api.standalone.StandalonePluginWorkspace;
import ro.sync.exml.workspace.api.standalone.ToolbarComponentsCustomizer;
import ro.sync.exml.workspace.api.standalone.ToolbarInfo;
import ro.sync.exml.workspace.api.standalone.ui.ToolbarButton;

public class EpubPluginExtension implements WorkspaceAccessPluginExtension {
	
	public static final String TOOLBAR_PREFIX = "dk.nota.oxygen.epub";
	public static final String NAV_TOOLBAR = "dk.nota.oxygen.epub.toolbar.nav";
	public static final String OPF_TOOLBAR = "dk.nota.oxygen.epub.toolbar.opf";
	public static final String XHTML_TOOLBAR = "dk.nota.oxygen.epub.toolbar.xhtml";
	
	@Override
	public boolean applicationClosing() {
		return true;
	}

	@Override
	public void applicationStarted(StandalonePluginWorkspace pluginWorkspace) {
		pluginWorkspace.addEditorChangeListener(new WorkspaceSetupListener(
				pluginWorkspace), StandalonePluginWorkspace.MAIN_EDITING_AREA);
		pluginWorkspace.addToolbarComponentsCustomizer(
				new EpubToolbarCustomizer());
	}
	
	public static WSEditor getCurrentEditor() {
		return PluginWorkspaceProvider.getPluginWorkspace()
				.getCurrentEditorAccess(StandalonePluginWorkspace
						.MAIN_EDITING_AREA);
	}
	
	public static EditorAccess getEditorAccess() {
		return new EditorAccess(getCurrentEditor());
	}
	
	private class EpubToolbarCustomizer implements ToolbarComponentsCustomizer {
		
		private JComponent[] navComponents = new JComponent[] {
				new ToolbarButton(new UpdateNavigationAction(), true)
		};
		private JComponent[] opfComponents = new JComponent[] {
				new ToolbarButton(new ConcatAction(), true),
				new ToolbarButton(new SplitAction(), true),
				new ToolbarButton(new ImportDocxAction(true), true),
				new ToolbarButton(new CreateDtbAction(), true)
		};
		private JComponent[] xhtmlComponents = new JComponent[] {
				new ToolbarButton(new UpdateNavigationAction(), true),
				new ToolbarButton(new ImportDocxAction(false), true)
		};

		@Override
		public void customizeToolbar(ToolbarInfo toolbar) {
			if (!toolbar.getToolbarID().startsWith(TOOLBAR_PREFIX)) return;
			if (toolbar.getToolbarID().equals(NAV_TOOLBAR)) {
				toolbar.setTitle("EPUB Navigation");
				toolbar.setComponents(navComponents);
			} else if (toolbar.getToolbarID().equals(OPF_TOOLBAR)) {
				toolbar.setTitle("EPUB OPF");
				toolbar.setComponents(opfComponents);
			} else if (toolbar.getToolbarID().equals(XHTML_TOOLBAR)) {
				toolbar.setTitle("EPUB XHTML");
				toolbar.setComponents(xhtmlComponents);
			}
		}
		
	}

}
