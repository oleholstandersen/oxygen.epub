package dk.nota.oxygen.quickbase;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;

import ro.sync.exml.workspace.api.PluginWorkspaceProvider;

public class DownloadFromQuickbaseAction extends AbstractAction {
	
	private String pid;
	
	public DownloadFromQuickbaseAction() {
		super("Download production...");
	}
	
	public DownloadFromQuickbaseAction(String pid) {
		super(String.format("Download %s...", pid));
		this.pid = pid;
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		File outputFile = PluginWorkspaceProvider.getPluginWorkspace()
				.chooseFile(new File("N:/XMLarkiv/" + (pid == null ? "" : pid +
						".epub")), "Download from QuickBase",
						new String[] { "epub" }, "EPUB files", true);
		if (outputFile == null) return;
		if (pid == null) pid = outputFile.getName().replaceFirst("\\..*$", "");
		DownloadFromQuickbaseWorker downloadFromQuickbaseWorker =
				new DownloadFromQuickbaseWorker(pid, outputFile);
		QuickbaseDownloadDialog quickbaseDownloadDialog =
				new QuickbaseDownloadDialog(downloadFromQuickbaseWorker,
						pid, outputFile);
		downloadFromQuickbaseWorker.addPropertyChangeListener(
				quickbaseDownloadDialog);
		quickbaseDownloadDialog.setVisible(true);
		downloadFromQuickbaseWorker.execute();
	}

}
