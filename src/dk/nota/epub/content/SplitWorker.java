package dk.nota.epub.content;

import java.net.URL;
import java.util.LinkedList;

import dk.nota.archive.ArchiveAccess;
import dk.nota.epub.EpubAccess;
import dk.nota.oxygen.AbstractWorkerWithResults;
import dk.nota.oxygen.EditorAccessProvider;
import dk.nota.oxygen.ResultsListener;
import dk.nota.xml.DocumentResult;
import dk.nota.xml.XmlAccessProvider;
import net.sf.saxon.s9api.XdmNode;

public class SplitWorker
		extends AbstractWorkerWithResults<DocumentResult,Object> {
	
	private LinkedList<URL> affectedEditorUrls;
	private EpubAccess epubAccess;
	private XdmNode opfDocument;
	
	public SplitWorker(EpubAccess epubAccess, XdmNode opfDocument,
			ResultsListener listener, LinkedList<URL> affectedEditorUrls) {
		super("SPLIT", listener);
		this.affectedEditorUrls = affectedEditorUrls;
		this.epubAccess = epubAccess;
		this.opfDocument = opfDocument;
	}

	@Override
	protected DocumentResult doInBackground() throws Exception {
		fireResultsUpdate("SPLIT STARTING");
		XdmNode concatDocument = XmlAccessProvider.getXmlAccess()
				.getDocument(epubAccess.makeOpfBasedUri("concat.xhtml"));
		Splitter splitter = new Splitter(concatDocument, opfDocument);
		splitter.addListener(getResultsListener());
		DocumentResult documentResult = new DocumentResult(splitter.call());
		ArchiveAccess archiveAccess = epubAccess.getArchiveAccess();
		documentResult.writeDocumentsToArchive(archiveAccess);
		return documentResult;
	}
	
	@Override
	protected void done() {
		super.done();
		affectedEditorUrls.forEach(
				url -> {
					if (url.getPath().endsWith("package.opf"))
						EditorAccessProvider.getEditorAccess().open(url);
				});
	}

}
