package dk.nota.oxygen.epub.opf;

import java.io.File;
import java.net.URISyntaxException;
import dk.nota.oxygen.common.ConsoleWindow;
import dk.nota.oxygen.common.EditorAccess;
import dk.nota.oxygen.epub.common.ArchiveSensitiveAction;
import dk.nota.oxygen.epub.common.EpubAccess;
import net.sf.saxon.s9api.SaxonApiException;

public class CreateInspirationOutputAction extends ArchiveSensitiveAction {
	
	private InspirationOutputType outputType;

	public CreateInspirationOutputAction(String name,
			InspirationOutputType outputType) {
		super(name);
		this.outputType = outputType;
	}
	
	@Override
	public void actionPerformed(EditorAccess editorAccess) {
		EpubAccess epubAccess = editorAccess.getEpubAccess();
		try {
			String identifier = outputType.getPrefix() + epubAccess.getPid()
					.substring(4);
			String processName = String.format("Create DTBook [%s]", outputType
					.getName());
			File outputFile = null;
			switch (outputType) {
			case PRINT:
				outputFile = editorAccess.getWorkspace().chooseDirectory();
				break;
			case PROOF:
				outputFile = editorAccess.getWorkspace().chooseFile(new File(
						epubAccess.getArchiveFileUrl().toURI().resolve(
						identifier + ".htm")), processName,
						new String[] {"htm", "html", "xhtml"}, "HTML files",
						true);
				break;
			default:
				outputFile = editorAccess.getWorkspace().chooseFile(new File(
						epubAccess.getArchiveFileUrl().toURI().resolve(
						identifier + ".xml")), processName, new String[] {"xml"},
						"DTBook files", true);
			}
			if (outputFile == null) return;
			CreateInspirationOutputWorker outputWorker =
				new CreateInspirationOutputWorker(editorAccess, epubAccess,
						new ConsoleWindow(processName), outputFile,
						outputType);
			outputWorker.setDtbIdentifier(identifier);
			outputWorker.execute();
		} catch (SaxonApiException | URISyntaxException e) {
			editorAccess.showErrorMessage(e.toString());
		}
	}

}
