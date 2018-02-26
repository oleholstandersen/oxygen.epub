package dk.nota.oxygen.quickbase;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.util.LinkedList;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.JMenuItem;

import dk.nota.oxygen.epub.common.EpubAccess;
import dk.nota.oxygen.epub.plugin.EpubPluginExtension;
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
		actionMenuItems.add(add(approveProductionAction));
		add(showProductionAction).setEnabled(false);
		addSeparator();
		actionMenuItems.add(add(
				new AbstractAction("Refresh queue") {
					@Override
					public void actionPerformed(ActionEvent event) {
						populateQueue(EpubPluginExtension.getQuickbaseAccess());
					}
				}));
		addEmptyQueueLabel();
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
		enableActions();
		updateForEpub(EpubPluginExtension.getEditorAccess().getEpubAccess());
		populateQueue(quickbaseAccess);
	}
	
	public void disableActions() {
		for (JMenuItem actionMenuItem : actionMenuItems)
			actionMenuItem.setEnabled(false);
	}
	
	@Override
	public void disconnected(QuickbaseAccess quickbaseAccess) {
		disableActions();
	}
	
	public void enableActions() {
		for (JMenuItem actionMenuItem : actionMenuItems)
			actionMenuItem.setEnabled(true);
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
				EpubPluginExtension.getQuickbaseAccess().isConnected());
		showProductionAction.update(pid);
		showProductionAction.setEnabled(epubAccess != null);
	}

}
