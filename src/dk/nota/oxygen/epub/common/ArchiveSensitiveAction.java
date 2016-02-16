package dk.nota.oxygen.epub.common;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.URL;

import javax.swing.AbstractAction;

import dk.nota.oxygen.common.EditorAccess;
import dk.nota.oxygen.epub.plugin.EpubPluginExtension;

public abstract class ArchiveSensitiveAction extends AbstractAction {
	
	public ArchiveSensitiveAction(String name) {
		super(name);
	}
	
	public abstract void actionPerformed(EditorAccess editorAccess);
	
	@Override
	public void actionPerformed(ActionEvent event) {
		EditorAccess editorAccess = EpubPluginExtension.getEditorAccess();
		URL archiveUrl = editorAccess.getEpubAccess().getArchiveUrlInternal();
		if (editorAccess.unsavedChangesInArchive(archiveUrl)) {
			editorAccess.showErrorMessage("Unsaved changes in archive files: " +
					"please review and save before trying again");
			return;
		}
		try {
			editorAccess.getEpubAccess().backupArchive();
		} catch (IOException e) {
			editorAccess.showErrorMessage("Unable to back up archive: " +
					e.toString());
			return;
		}
		actionPerformed(editorAccess);
	}

}
