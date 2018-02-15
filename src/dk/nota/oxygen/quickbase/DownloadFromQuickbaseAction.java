package dk.nota.oxygen.quickbase;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;

import dk.nota.oxygen.common.EditorAccess;
import dk.nota.oxygen.epub.plugin.EpubPluginExtension;

public class DownloadFromQuickbaseAction extends AbstractAction {
	
	private boolean openAfterDownload = false;
	private String pid;
	
	public DownloadFromQuickbaseAction(boolean openAfterDownload) {
		super(openAfterDownload ? "Download production and open..." :
			"Download production...");
		this.openAfterDownload = openAfterDownload;
	}
	
	public DownloadFromQuickbaseAction(String pid, boolean openAfterDownload) {
		super(openAfterDownload ? String.format("Download %s and open...", pid) :
				String.format("Download %s...", pid));
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
		DownloadFromQuickbaseWorker downloadFromQuickbaseWorker =
				new DownloadFromQuickbaseWorker(outputFile, openAfterDownload);
		try {
			editorAccess.getWorkspace().open(downloadFromQuickbaseWorker
					.doInBackground().toURI().toURL());
		} catch (Exception e) {
			editorAccess.showStatusMessage("Could not download " + outputFile.getName());
			e.printStackTrace();
		}
		
	}

}
