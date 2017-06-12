package dk.nota.oxygen.epub.plugin;

import javax.swing.JComponent;

import dk.nota.oxygen.common.EditorAccess;
import dk.nota.oxygen.epub.common.ImportDocxAction;
import dk.nota.oxygen.epub.nav.UpdateNavigationAction;
import dk.nota.oxygen.epub.opf.ConcatAction;
import dk.nota.oxygen.epub.opf.CreateDocxAction;
import dk.nota.oxygen.epub.opf.CreateDtbAction;
import dk.nota.oxygen.epub.opf.CreateInspirationOutputAction;
import dk.nota.oxygen.epub.opf.ImportDtbAction;
import dk.nota.oxygen.epub.opf.InspOutputType;
import dk.nota.oxygen.epub.opf.SplitAction;
import dk.nota.oxygen.epub.xhtml.ImportCatListAction;
import ro.sync.exml.plugin.workspace.WorkspaceAccessPluginExtension;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;
import ro.sync.exml.workspace.api.editor.WSEditor;
import ro.sync.exml.workspace.api.standalone.StandalonePluginWorkspace;
import ro.sync.exml.workspace.api.standalone.ToolbarComponentsCustomizer;
import ro.sync.exml.workspace.api.standalone.ToolbarInfo;
import ro.sync.exml.workspace.api.standalone.ui.Menu;
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
		
		@Override
		public void customizeToolbar(ToolbarInfo toolbar) {
			if (!toolbar.getToolbarID().startsWith(TOOLBAR_PREFIX)) return;
			switch (toolbar.getToolbarID()) {
			case NAV_TOOLBAR:
				setupNavToolbar(toolbar);
				return;
			case OPF_TOOLBAR:
				setupOpfToolbar(toolbar);
				return;
			case XHTML_TOOLBAR:
				setupXhtmlToolbar(toolbar);
			}
		}
		
		private void setupNavToolbar(ToolbarInfo toolbar) {
			JComponent[] navComponents = new JComponent[] {
				new ToolbarButton(new UpdateNavigationAction(), true)
			};
			toolbar.setTitle("EPUB Navigation");
			toolbar.setComponents(navComponents);
		}
		
		private void setupOpfToolbar(ToolbarInfo toolbar) {
			Menu exportMenu = new Menu("Export");
			exportMenu.insertAction(new CreateDtbAction(), 0);
			exportMenu.insertSeparator(1);
			exportMenu.insertAction(new CreateInspirationOutputAction(
					"Inspiration: E-tekst", InspOutputType.INSP_ETEXT), 2);
			exportMenu.insertAction(new CreateInspirationOutputAction(
					"Inspiration: Korrektur", InspOutputType.INSP_PROOF), 3);
			exportMenu.insertAction(new CreateInspirationOutputAction(
					"Inspiration: Lyd", InspOutputType.INSP_AUDIO), 4);
			exportMenu.insertAction(new CreateInspirationOutputAction(
					"Inspiration: Punkt", InspOutputType.INSP_BRAILLE), 5);
			exportMenu.insertAction(new CreateInspirationOutputAction(
					"Inspiration: Tryk", InspOutputType.INSP_PRINT), 6);
			exportMenu.insertSeparator(7);
			exportMenu.insertAction(new CreateDocxAction(), 8);
			Menu importMenu = new Menu("Import");
			importMenu.insertAction(new ImportDocxAction(), 0);
			importMenu.insertSeparator(1);
			importMenu.insertAction(new ImportDtbAction(), 2);
			JComponent[] opfComponents = new JComponent[] {
				new ToolbarButton(new ConcatAction(), true),
				new ToolbarButton(new SplitAction(), true),
				importMenu,
				exportMenu
			};
			toolbar.setTitle("EPUB OPF");
			toolbar.setComponents(opfComponents);
		}
		
		private void setupXhtmlToolbar(ToolbarInfo toolbar) {
			JComponent[] xhtmlComponents = new JComponent[] {
				new ToolbarButton(new UpdateNavigationAction(), true),
				new ToolbarButton(new ImportDocxAction(), true),
				new ToolbarButton(new ImportCatListAction(), true)
			};
			toolbar.setTitle("EPUB XHTML");
			toolbar.setComponents(xhtmlComponents);
		}
		
	}

}
