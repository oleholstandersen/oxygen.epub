package dk.nota.oxygen.epub.opf;

import java.io.File;
import java.net.URISyntaxException;

import dk.nota.oxygen.common.ConsoleWindow;
import dk.nota.oxygen.common.EditorAccess;
import dk.nota.oxygen.epub.common.ArchiveSensitiveAction;
import dk.nota.oxygen.epub.common.EpubAccess;
import net.sf.saxon.s9api.SaxonApiException;

public class CreateDtbAction extends ArchiveSensitiveAction {
	
	public CreateDtbAction() {
		super("DTBook 1.1.0");
	}

	@Override
	public void actionPerformed(EditorAccess editorAccess) {
		try {
			EpubAccess epubAccess = editorAccess.getEpubAccess();
			String dtbFileName = "./" + epubAccess.getPid() + ".xml";
			File dtbFile = editorAccess.getWorkspace().chooseFile(new File(
					epubAccess.getArchiveFileUrl().toURI().resolve(dtbFileName)),
					"Create DTBook", new String[] {"xml"}, "DTBook files",
					true);
			if (dtbFile == null) return;
			CreateDtbWorker createDtbWorker = new CreateDtbWorker(editorAccess,
					epubAccess, new ConsoleWindow("Create DTBook"), dtbFile,
					false);
			createDtbWorker.execute();
		} catch (URISyntaxException | SaxonApiException e) {
			editorAccess.showErrorMessage(e.toString());
		}
	}

}