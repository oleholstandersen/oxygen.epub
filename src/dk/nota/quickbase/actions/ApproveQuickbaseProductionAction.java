package dk.nota.quickbase.actions;

import java.awt.event.ActionEvent;
import java.time.LocalDateTime;
import java.util.HashMap;

import javax.swing.AbstractAction;

import dk.nota.oxygen.epub.plugin.EpubPluginExtension;
import dk.nota.quickbase.QuickbaseAccess;
import dk.nota.quickbase.QuickbaseException;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;

public class ApproveQuickbaseProductionAction extends AbstractAction {
	
	private String pid;
	
	public ApproveQuickbaseProductionAction() {
		super("Approve production");
		setEnabled(false);
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		QuickbaseAccess quickbaseAccess = EpubPluginExtension
				.getQuickbaseAccess();
		try {
			String date = LocalDateTime.now().toLocalDate().toString();
			HashMap<Integer,String> updateMap = new HashMap<Integer,String>();
			updateMap.put(13, date);
			quickbaseAccess.updateFields(pid, updateMap);
			PluginWorkspaceProvider.getPluginWorkspace().showStatusMessage(
					String.format("%s approved", pid));
		} catch (QuickbaseException e) {
			PluginWorkspaceProvider.getPluginWorkspace().showErrorMessage(e
					.getMessage(), e);
		}
	}
	
	public void update(String pid) {
		this.pid = pid;
		putValue(AbstractAction.NAME, "Approve " + pid.replaceFirst("^dk-nota-",
				""));
	}

}
