package dk.nota.oxygen.epub.opf;

import java.io.File;
import java.net.URISyntaxException;

import dk.nota.oxygen.common.EditorAccess;
import dk.nota.oxygen.common.ResultsView;
import dk.nota.oxygen.epub.common.ArchiveSensitiveAction;
import dk.nota.oxygen.epub.common.EpubAccess;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;

public class CreateInspirationOutputAction extends ArchiveSensitiveAction {
	
	private InspOutputType outputType;

	public CreateInspirationOutputAction(String name,
			InspOutputType outputType) {
		super(name);
		this.outputType = outputType;
	}
	
	@Override
	public void actionPerformed(EditorAccess editorAccess) {
		EpubAccess epubAccess = editorAccess.getEpubAccess();
		String identifier = outputType.getPrefix() + epubAccess.getPid()
				.substring(4);
		String processName = String.format("Export [%s]", outputType
				.getName());
		File outputFile = null;
		try {
			switch (outputType) {
			case INSP_PRINT:
				outputFile = editorAccess.getWorkspace().chooseDirectory();
				break;
			case INSP_PROOF:
				outputFile = editorAccess.getWorkspace().chooseFile(new File(
						epubAccess.getArchiveFileUrl().toURI().resolve(
						identifier + ".htm")), processName,
						new String[] { "htm", "html", "xhtml" }, "HTML files",
						true);
				break;
			default:
				outputFile = editorAccess.getWorkspace().chooseFile(new File(
						epubAccess.getArchiveFileUrl().toURI().resolve(
						identifier + ".xml")), processName, new String[] {
								"xml" }, "DTBook files", true);
			}
		} catch (URISyntaxException e) {
			PluginWorkspaceProvider.getPluginWorkspace().showErrorMessage(
					"Unable to generate URI for output file(s)", e);
		}
		if (outputFile == null) return;
		CreateDtbListener createDtbListener = new CreateDtbListener(new ResultsView(
				epubAccess.getPid() + " - " + processName));
		CreateInspirationOutputWorker outputWorker =
			new CreateInspirationOutputWorker(createDtbListener, epubAccess,
					outputFile, outputType);
		outputWorker.setDtbIdentifier(identifier);
		outputWorker.execute();
		
	}

}
