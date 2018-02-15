package dk.nota.oxygen.quickbase;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;

import javax.swing.AbstractAction;

import dk.nota.oxygen.epub.plugin.EpubPluginExtension;
import net.sf.saxon.s9api.SaxonApiException;
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
		} catch (IOException | SaxonApiException e) {
			e.printStackTrace();
		}
	}
	
	public void update(String pid) {
		this.pid = pid;
		putValue(AbstractAction.NAME, "Approve " + pid.replaceFirst("^dk-nota-",
				""));
	}

}
