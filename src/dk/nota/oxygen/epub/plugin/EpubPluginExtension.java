package dk.nota.oxygen.epub.plugin;

import javax.swing.JComponent;
import javax.swing.JMenuBar;

import dk.nota.dtb.conversion.InspirationOutput;
import dk.nota.epub.actions.ConcatAction;
import dk.nota.epub.actions.EpubToDocxAction;
import dk.nota.epub.actions.EpubToDtbAction;
import dk.nota.epub.actions.InspirationOutputAction;
import dk.nota.epub.actions.NavigationUpdateAction;
import dk.nota.epub.actions.SplitAction;
import dk.nota.oxygen.common.EditorAccess;
import dk.nota.oxygen.epub.common.ImportDocxAction;
import dk.nota.oxygen.epub.opf.CreateDaisyComicAction;
import dk.nota.oxygen.epub.opf.ReloadDocumentsAction;
import dk.nota.oxygen.epub.xhtml.ImportCatListAction;
import dk.nota.oxygen.quickbase.QuickbaseAccess;
import dk.nota.oxygen.quickbase.QuickbaseMenu;
import ro.sync.exml.plugin.workspace.WorkspaceAccessPluginExtension;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;
import ro.sync.exml.workspace.api.editor.WSEditor;
import ro.sync.exml.workspace.api.standalone.MenuBarCustomizer;
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
	
	private static QuickbaseAccess quickbaseAccess;
	private static QuickbaseMenu quickbaseMenu;
	
	@Override
	public boolean applicationClosing() {
		return true;
	}

	@Override
	public void applicationStarted(StandalonePluginWorkspace pluginWorkspace) {
		quickbaseAccess = new QuickbaseAccess(pluginWorkspace);
		quickbaseMenu = new QuickbaseMenu();
		quickbaseAccess.addListener(quickbaseMenu);
		pluginWorkspace.addEditorChangeListener(new WorkspaceSetupListener(
				pluginWorkspace), StandalonePluginWorkspace.MAIN_EDITING_AREA);
		pluginWorkspace.addToolbarComponentsCustomizer(
				new EpubToolbarCustomizer());
		pluginWorkspace.addMenuBarCustomizer(new EpubMenuBarCustomizer());
	}
	
	public static WSEditor getCurrentEditor() {
		return PluginWorkspaceProvider.getPluginWorkspace()
				.getCurrentEditorAccess(StandalonePluginWorkspace
						.MAIN_EDITING_AREA);
	}
	
	public static EditorAccess getEditorAccess() {
		return new EditorAccess(getCurrentEditor());
	}
	
	public static QuickbaseAccess getQuickbaseAccess() {
		return quickbaseAccess;
	}
	
	public static QuickbaseMenu getQuickbaseMenu() {
		return quickbaseMenu;
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
				new ToolbarButton(new NavigationUpdateAction(), true)
			};
			toolbar.setTitle("EPUB Navigation");
			toolbar.setComponents(navComponents);
		}
		
		private void setupOpfToolbar(ToolbarInfo toolbar) {
			Menu contentMenu = new Menu("Content");
			contentMenu.add(new ConcatAction());
			contentMenu.add(new SplitAction());
			contentMenu.addSeparator();
			contentMenu.add(new ReloadDocumentsAction());
			Menu exportMenu = new Menu("Export");
			exportMenu.add(new EpubToDtbAction());
			exportMenu.addSeparator();
			exportMenu.add(new InspirationOutputAction(InspirationOutput
					.INSP_ETEXT));
			exportMenu.add(new InspirationOutputAction(InspirationOutput
					.INSP_PROOF));
			exportMenu.add(new InspirationOutputAction(InspirationOutput
					.INSP_AUDIO));
			exportMenu.add(new InspirationOutputAction(InspirationOutput
					.INSP_BRAILLE));
			exportMenu.add(new InspirationOutputAction(InspirationOutput
					.INSP_PRINT));
			exportMenu.addSeparator();
			exportMenu.add(new EpubToDocxAction());
			exportMenu.addSeparator();
			exportMenu.add(new CreateDaisyComicAction());
			Menu importMenu = new Menu("Import");
			importMenu.add(new ImportDocxAction());
//			importMenu.addSeparator();
//			importMenu.add(new ImportDtbAction());
			JComponent[] opfComponents = new JComponent[] {
				contentMenu,
				importMenu,
				exportMenu
			};
			toolbar.setTitle("EPUB OPF");
			toolbar.setComponents(opfComponents);
		}
		
		private void setupXhtmlToolbar(ToolbarInfo toolbar) {
			Menu importMenu = new Menu("Import");
			importMenu.add(new ImportDocxAction());
			importMenu.add(new ImportCatListAction());
			JComponent[] xhtmlComponents = new JComponent[] {
				new ToolbarButton(new NavigationUpdateAction(), true),
				importMenu
			};
			toolbar.setTitle("EPUB XHTML");
			toolbar.setComponents(xhtmlComponents);
		}
		
	}
	
	private class EpubMenuBarCustomizer implements MenuBarCustomizer {

		@Override
		public void customizeMainMenu(JMenuBar menuBar) {
			menuBar.add(quickbaseMenu);
		}
		
		
	}

}
