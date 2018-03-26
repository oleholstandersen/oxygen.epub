package dk.nota.epub.actions;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.LinkedList;

import javax.swing.AbstractAction;

import dk.nota.epub.EpubAccess;
import dk.nota.epub.EpubAccessProvider;
import dk.nota.epub.EpubException;
import dk.nota.oxygen.EditorAccess;
import dk.nota.oxygen.EditorAccessProvider;
import ro.sync.exml.workspace.api.editor.WSEditor;

public abstract class EpubAction extends AbstractAction {
	
	protected EpubAccess epubAccess;
	
	public EpubAction(String name) {
		super(name);
	}
	
	public abstract void actionPerformed(EditorAccess editorAccess,
			LinkedList<URL> affectedEditorUrls);
	
	@Override
	public void actionPerformed(ActionEvent event) {
		EditorAccess editorAccess = EditorAccessProvider.getEditorAccess();
		URL editorUrl = editorAccess.getCurrentEditorUrl();
		LinkedList<URL> affectedEditorUrls = new LinkedList<URL>();
		for (URL url : editorAccess.getArchiveEditorUrls(editorAccess
				.getArchiveUrlComponent(editorUrl))) {
			WSEditor editor = editorAccess.getEditor(url);
			if (editor.isModified()) {
				editorAccess.showErrorMessage("Unsaved changes in archive: "
						+ "please review and save before trying again");
				return;
			}
			affectedEditorUrls.add(url);
		}
		affectedEditorUrls.forEach(url -> editorAccess.close(url));
		try {
			epubAccess = EpubAccessProvider.getEpubAccess(URI.create(
					editorAccess.getArchiveUrlComponent(editorUrl)));
			epubAccess.backupArchive();
		} catch (EpubException e) {
			editorAccess.showErrorMessage("Unable to get EPUB access", e);
			return;
		} catch (IOException e) {
			editorAccess.showErrorMessage("Unable to back up EPUB archive", e);
			return;
		}
		actionPerformed(editorAccess, affectedEditorUrls);
	}

}
