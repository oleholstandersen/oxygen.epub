package dk.nota.oxygen.quickbase;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.JMenuItem;

import dk.nota.oxygen.epub.common.EpubAccess;
import dk.nota.oxygen.epub.plugin.EpubPluginExtension;
import net.sf.saxon.s9api.SaxonApiException;
import ro.sync.exml.workspace.api.standalone.ui.Menu;

public class QuickbaseMenu extends Menu {

	private LinkedList<JMenuItem> actionMenuItems = new LinkedList<JMenuItem>();
	private ApproveQuickbaseProductionAction approveProductionAction =
			new ApproveQuickbaseProductionAction();
	private LinkedList<JMenuItem> queueMenuItems = new LinkedList<JMenuItem>();
	private ShowQuickbaseProductionAction showProductionAction
			= new ShowQuickbaseProductionAction();
	
	public QuickbaseMenu() {
		super("QuickBase");
		actionMenuItems.add(add(new DownloadFromQuickbaseAction(true)));
		actionMenuItems.add(add(new DownloadFromQuickbaseAction(false)));
		addSeparator();
		actionMenuItems.add(add(approveProductionAction));
		actionMenuItems.add(add(showProductionAction));
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
	
	public void disableActions() {
		for (JMenuItem actionMenuItem : actionMenuItems)
			actionMenuItem.setEnabled(false);
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
					recordMenu.add(new DownloadFromQuickbaseAction(pid, true));
					recordMenu.add(new DownloadFromQuickbaseAction(pid, false));
					recordMenu.addSeparator();
					recordMenu.add(new ShowQuickbaseProductionAction(pid,
							quickbaseRecord.getRid()));
					queueMenuItems.add(add(recordMenu));
				}
			}
		} catch (IOException | SaxonApiException e) {
			e.printStackTrace();
		}
	}
	
	public void updateForEpub(EpubAccess epubAccess) {
		if (epubAccess == null) return;
		String pid = epubAccess.getPid();
		approveProductionAction.update(pid);
		showProductionAction.update(pid);
	}

}
