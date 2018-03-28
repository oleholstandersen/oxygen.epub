package dk.nota.epub.content;

import java.net.URI;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;

import dk.nota.archive.ArchiveAccess;
import dk.nota.epub.EpubAccess;
import dk.nota.oxygen.AbstractWorkerWithResults;
import dk.nota.oxygen.EditorAccessProvider;
import dk.nota.oxygen.ResultsListener;
import dk.nota.xml.DocumentResult;
import net.sf.saxon.s9api.XdmNode;

public class ConcatWorker
		extends AbstractWorkerWithResults<DocumentResult,Object> {
	
	private LinkedList<URL> affectedEditorUrls;
	private EpubAccess epubAccess;
	private XdmNode opfDocument;

	public ConcatWorker(EpubAccess epubAccess, XdmNode opfDocument,
			ResultsListener listener, LinkedList<URL> affectedEditorUrls) {
		super("CONCAT", listener);
		this.affectedEditorUrls = affectedEditorUrls;
		this.epubAccess = epubAccess;
		this.opfDocument = opfDocument;
	}

	@Override
	protected DocumentResult doInBackground() throws Exception {
		fireResultsUpdate("CONCAT STARTING");
		Concatter concatter = new Concatter(opfDocument, true);
		concatter.addListener(getResultsListener());
		DocumentResult documentResult = new DocumentResult(concatter.call());
		ArchiveAccess archiveAccess = epubAccess.getArchiveAccess();
		try (FileSystem epubFileSystem = archiveAccess
				.getArchiveAsFileSystem()) {
			documentResult.writeDocumentsToArchive(archiveAccess, epubFileSystem);
			for (URI uri : concatter.getOriginalDocuments()) {
				Path path = epubFileSystem.getPath(archiveAccess
						.relativizeUriToArchive(uri));
				fireResultsUpdate("Deleting " + path.getFileName());
				Files.delete(path);
			}
		}
		return documentResult;
	}
	
	@Override
	protected void done() {
		super.done();
		affectedEditorUrls.forEach(
				url -> {
					if (url.getPath().endsWith("concat.xhtml") ||
							url.getPath().endsWith("package.opf"))
						EditorAccessProvider.getEditorAccess().open(url);
				});
	}

}
