package dk.nota.oxygen.epub.opf;

import java.io.File;
import java.net.URISyntaxException;

import dk.nota.oxygen.common.EditorAccess;
import dk.nota.oxygen.common.ImageStoringResultsListener;
import dk.nota.oxygen.common.ResultsView;
import dk.nota.oxygen.epub.common.ArchiveSensitiveAction;
import dk.nota.oxygen.epub.common.EpubAccess;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;

public class CreateDtbAction extends ArchiveSensitiveAction {
	
	public CreateDtbAction() {
		super("DTBook 1.1.0");
	}

	@Override
	public void actionPerformed(EditorAccess editorAccess) {
		EpubAccess epubAccess = editorAccess.getEpubAccess();
		String dtbFileName = "./" + epubAccess.getPid().replaceFirst(
				"dk-nota-", "") + ".xml";
		File dtbFile = null;
		try {
			dtbFile = editorAccess.getWorkspace().chooseFile(new File(
					epubAccess.getArchiveFileUrl().toURI().resolve(dtbFileName)),
					"Export [DTBook]", new String[] {"xml"}, "DTBook files",
					true);
		} catch (URISyntaxException e) {
			PluginWorkspaceProvider.getPluginWorkspace().showErrorMessage(
					"Unable to generate URI for output file", e);
		}
		if (dtbFile == null) return;
		ImageStoringResultsListener imageListener = new ImageStoringResultsListener(
				new ResultsView(epubAccess.getPid() + " - Export [DTBook]"));
		CreateDtbWorker createDtbWorker = new CreateDtbWorker(
				"DTBOOK CONVERSION", imageListener, epubAccess, dtbFile);
		createDtbWorker.execute();
	}

}