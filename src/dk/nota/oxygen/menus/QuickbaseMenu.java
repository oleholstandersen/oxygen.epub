package dk.nota.oxygen.menus;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.util.LinkedList;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.JMenuItem;

import dk.nota.epub.EpubAccess;
import dk.nota.epub.EpubAccessProvider;
import dk.nota.epub.EpubException;
import dk.nota.oxygen.EditorAccess;
import dk.nota.oxygen.EditorAccessProvider;
import dk.nota.oxygen.actions.quickbase.ApproveQuickbaseProductionAction;
import dk.nota.oxygen.actions.quickbase.DownloadFromQuickbaseAction;
import dk.nota.oxygen.actions.quickbase.ShowQuickbaseProductionAction;
import dk.nota.quickbase.QuickbaseAccess;
import dk.nota.quickbase.QuickbaseAccessListener;
import dk.nota.quickbase.QuickbaseAccessProvider;
import dk.nota.quickbase.QuickbaseException;
import dk.nota.quickbase.QuickbaseRecord;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;
import ro.sync.exml.workspace.api.standalone.ui.Menu;

public class QuickbaseMenu extends Menu implements QuickbaseAccessListener {

	private LinkedList<JMenuItem> actionMenuItems = new LinkedList<JMenuItem>();
	private ApproveQuickbaseProductionAction approveProductionAction =
			new ApproveQuickbaseProductionAction();
	private LinkedList<JMenuItem> queueMenuItems = new LinkedList<JMenuItem>();
	private ShowQuickbaseProductionAction showProductionAction
			= new ShowQuickbaseProductionAction();
	
	public QuickbaseMenu() {
		super("QuickBase");
		actionMenuItems.add(add(new DownloadFromQuickbaseAction()));
		addSeparator();
		add(approveProductionAction).setEnabled(false);
		add(showProductionAction).setEnabled(false);
		addSeparator();
		actionMenuItems.add(add(
				new AbstractAction("Refresh queue") {
					@Override
					public void actionPerformed(ActionEvent event) {
						populateQueue(QuickbaseAccessProvider
								.getQuickbaseAccess());
					}
				}));
		addEmptyQueueLabel();
		if (!QuickbaseAccessProvider.getQuickbaseAccess().isConnected())
			enableActions(false);
	}
	
	private void addEmptyQueueLabel() {
		JMenuItem emptyMenuItem = new JMenuItem("Queue is empty");
		emptyMenuItem.setFont(new Font(emptyMenuItem.getFont()
				.getFontName(), Font.ITALIC, emptyMenuItem.getFont()
				.getSize()));
		emptyMenuItem.setEnabled(false);
		queueMenuItems.add(add(emptyMenuItem));
	}
	
	@Override
	public void connected(QuickbaseAccess quickbaseAccess) {
		enableActions(true);
		populateQueue(quickbaseAccess);
		EditorAccess editorAccess = EditorAccessProvider.getEditorAccess();
		if (editorAccess.getCurrentEditorUrl() == null) return;
		try {
			updateForEpub(EpubAccessProvider.getEpubAccess(editorAccess
					.getArchiveUri()));
		} catch (EpubException e) {
			editorAccess.showErrorMessage("Unable to get EPUB access", e);
		}
	}
	
	@Override
	public void disconnected(QuickbaseAccess quickbaseAccess) {
		enableActions(false);
	}
	
	public void enableActions(boolean enable) {
		for (JMenuItem actionMenuItem : actionMenuItems)
			actionMenuItem.setEnabled(enable);
		for (JMenuItem queueMenuItem : queueMenuItems)
			queueMenuItem.setEnabled(enable);
	}
	
	public void emptyQueue() {
		for (JMenuItem queueMenuItem : queueMenuItems) remove(queueMenuItem);
		addEmptyQueueLabel();
	}
	
	public void populateQueue(QuickbaseAccess quickbaseAccess) {
		if (quickbaseAccess == null) {
			emptyQueue();
			return;
		}
		// Remove queue entries
		for (JMenuItem queueMenuItem : queueMenuItems) remove(queueMenuItem);
		try {
			String query = String.format(
					"{'77'.TV.'%s'}AND{'18'.XEX.''}AND{'13'.EX.''}",
					quickbaseAccess.getUserId());
			Map<String,QuickbaseRecord> records = quickbaseAccess.query(query,
					"112", "num-10.sortorder-D");
			if (records.isEmpty()) addEmptyQueueLabel();
			else {
				for (QuickbaseRecord quickbaseRecord : records.values()) {
					String pid = quickbaseRecord.getPid().replaceFirst(
							"^dk-nota-", "");
					Menu recordMenu = new Menu(pid);
					recordMenu.add(new DownloadFromQuickbaseAction(pid));
					recordMenu.addSeparator();
					recordMenu.add(new ShowQuickbaseProductionAction(pid,
							quickbaseRecord.getRid()));
					queueMenuItems.add(add(recordMenu));
				}
			}
		} catch (QuickbaseException e) {
			PluginWorkspaceProvider.getPluginWorkspace().showErrorMessage(e
					.getMessage(), e);
		}
	}
	
	public void updateForEpub(EpubAccess epubAccess) {
		String pid = epubAccess == null ? "" : epubAccess.getPid();
		approveProductionAction.update(pid);
		approveProductionAction.setEnabled(epubAccess != null &&
				QuickbaseAccessProvider.getQuickbaseAccess().isConnected());
		showProductionAction.update(pid);
		showProductionAction.setEnabled(epubAccess != null);
	}

}
