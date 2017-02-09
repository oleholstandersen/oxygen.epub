package dk.nota.oxygen.epub.opf;

import java.io.File;
import java.net.URISyntaxException;

import dk.nota.oxygen.common.ConsoleWindow;
import dk.nota.oxygen.common.EditorAccess;
import dk.nota.oxygen.epub.common.ArchiveSensitiveAction;
import dk.nota.oxygen.epub.common.EpubAccess;
import net.sf.saxon.s9api.SaxonApiException;

public class CreateDocxAction extends ArchiveSensitiveAction {

	public CreateDocxAction() {
		super("Docx");
	}

	@Override
	public void actionPerformed(EditorAccess editorAccess) {
		try {
			EpubAccess epubAccess = editorAccess.getEpubAccess();
			String docxFileName = "./" + epubAccess.getPid() + ".docx";
			File docxFile = editorAccess.getWorkspace().chooseFile(new File(
					epubAccess.getArchiveFileUrl().toURI().resolve(
					docxFileName)), "Export [Docx]", new String[] {"docx"},
					"Word documents", true);
			if (docxFile == null) return;
			CreateDocxWorker createDocxWorker = new CreateDocxWorker(
					editorAccess, epubAccess, new ConsoleWindow("Create Docx"),
					docxFile);
			createDocxWorker.execute();
		} catch (SaxonApiException | URISyntaxException e) {
			editorAccess.showErrorMessage(e.toString());
		}
	}

}
