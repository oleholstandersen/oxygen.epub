package dk.nota.oxygen.quickbase;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;

import dk.nota.oxygen.common.EditorAccess;
import dk.nota.oxygen.epub.plugin.EpubPluginExtension;

public class DownloadFromQuickbaseAction extends AbstractAction {
	
	private String pid;
	
	public DownloadFromQuickbaseAction() {
		super("Download production...");
	}
	
	public DownloadFromQuickbaseAction(String pid) {
		super(String.format("Download %s..."));
		this.pid = pid;
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		EditorAccess editorAccess = EpubPluginExtension.getEditorAccess();
		File outputFile = editorAccess.getWorkspace().chooseFile(
				new File("N:/XMLarkiv/" + (pid == null ? "" : pid + ".epub")),
				"Download from QuickBase", new String[] { "epub" },
				"EPUB files", true);
		if (outputFile == null) return;
		if (pid == null) pid = outputFile.getName().replaceFirst("\\..*$", "");
		DownloadFromQuickbaseWorker downloadFromQuickbaseWorker =
				new DownloadFromQuickbaseWorker(pid, outputFile);
		QuickbaseDownloadDialog quickbaseDownloadDialog =
				new QuickbaseDownloadDialog(downloadFromQuickbaseWorker,
						editorAccess, pid, outputFile);
		downloadFromQuickbaseWorker.addPropertyChangeListener(
				quickbaseDownloadDialog);
		try {
			downloadFromQuickbaseWorker.execute();
			quickbaseDownloadDialog.setVisible(true);
		} catch (Exception e) {
			editorAccess.showErrorMessage(String.format(
					"Could not download %s: %s", pid, e.toString()));
			e.printStackTrace();
		}
		
	}

}
