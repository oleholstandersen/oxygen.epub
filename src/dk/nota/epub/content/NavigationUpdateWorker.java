package dk.nota.epub.content;

import java.net.URL;
import java.util.LinkedList;

import dk.nota.archive.ArchiveAccess;
import dk.nota.epub.EpubAccess;
import dk.nota.oxygen.AbstractWorkerWithResults;
import dk.nota.oxygen.EditorAccessProvider;
import dk.nota.oxygen.ResultsListener;
import dk.nota.xml.DocumentResult;
import net.sf.saxon.s9api.XdmNode;

public class NavigationUpdateWorker
		extends AbstractWorkerWithResults<DocumentResult,Object> {
	
	private LinkedList<URL> affectedEditorUrls;
	private EpubAccess epubAccess;
	private XdmNode opfDocument;

	public NavigationUpdateWorker(EpubAccess epubAccess, XdmNode opfDocument,
			ResultsListener resultsListener,
			LinkedList<URL> affectedEditorUrls) {
		super("NAVIGATION UPDATE", resultsListener);
		this.affectedEditorUrls = affectedEditorUrls;
		this.epubAccess = epubAccess;
		this.opfDocument = opfDocument;
	}

	@Override
	protected DocumentResult doInBackground() throws Exception {
		fireResultsUpdate("NAVIGATION UPDATE STARTING");
		NavigationUpdater navigationUpdater = new NavigationUpdater(
				opfDocument);
		navigationUpdater.addListener(getResultsListener());
		DocumentResult documentResult = new DocumentResult(navigationUpdater
				.call());
		ArchiveAccess archiveAccess = epubAccess.getArchiveAccess();
		documentResult.writeDocumentsToArchive(archiveAccess);
		return documentResult;
	}
	
	@Override
	protected void done() {
		super.done();
		affectedEditorUrls.forEach(
				url -> EditorAccessProvider.getEditorAccess().open(url));
	}

}
