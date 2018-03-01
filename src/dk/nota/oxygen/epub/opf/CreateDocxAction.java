package dk.nota.oxygen.epub.opf;

import java.io.File;
import java.net.URISyntaxException;

import dk.nota.oxygen.common.ResultsView;
import dk.nota.oxygen.common.EditorAccess;
import dk.nota.oxygen.common.ImageStoringResultsListener;
import dk.nota.oxygen.epub.common.ArchiveSensitiveAction;
import dk.nota.oxygen.epub.common.EpubAccess;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;

public class CreateDocxAction extends ArchiveSensitiveAction {

	public CreateDocxAction() {
		super("Docx");
	}

	@Override
	public void actionPerformed(EditorAccess editorAccess) {
		EpubAccess epubAccess = editorAccess.getEpubAccess();
		String docxFileName = "./" + epubAccess.getPid().replaceFirst(
				"dk-nota-", "") + ".docx";
		File docxFile = null;
		try {
			docxFile = editorAccess.getWorkspace().chooseFile(new File(
					epubAccess.getArchiveFileUrl().toURI().resolve(
					docxFileName)), "Export [Docx]", new String[] {"docx"},
					"Word documents", true);
		} catch (URISyntaxException e) {
			PluginWorkspaceProvider.getPluginWorkspace().showErrorMessage(
					"Unable to generate URI for output file", e);
		}
		if (docxFile == null) return;
		ImageStoringResultsListener imageListener =
				new ImageStoringResultsListener(new ResultsView(epubAccess
						.getPid() + " - Create Docx"));
		CreateDocxWorker createDocxWorker = new CreateDocxWorker(
					imageListener, epubAccess, docxFile);
		createDocxWorker.execute();
	}

}
