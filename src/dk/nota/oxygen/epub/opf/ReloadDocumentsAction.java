package dk.nota.oxygen.epub.opf;

import java.io.IOException;

import dk.nota.oxygen.common.EditorAccess;
import dk.nota.oxygen.epub.common.ArchiveSensitiveAction;
import dk.nota.oxygen.epub.common.EpubAccess;
import net.sf.saxon.s9api.SaxonApiException;

public class ReloadDocumentsAction extends ArchiveSensitiveAction {

	public ReloadDocumentsAction() {
		super("Reload");
	}

	@Override
	public void actionPerformed(EditorAccess editorAccess) {
		EpubAccess epubAccess = editorAccess.getEpubAccess();
		try {
			editorAccess.getCurrentEditor().setEditable(false);
			epubAccess.reloadContentDocuments();
			editorAccess.getCurrentEditor().setEditable(true);
		} catch (IOException | SaxonApiException e) {
			editorAccess.showErrorMessage(e.getMessage());
		}
	}

}
