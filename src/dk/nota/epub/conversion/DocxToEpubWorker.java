package dk.nota.epub.conversion;

import java.io.File;
import java.net.URL;
import java.util.LinkedList;

import dk.nota.epub.EpubAccess;
import dk.nota.oxygen.AbstractWorkerWithResults;
import dk.nota.oxygen.EditorAccessProvider;
import dk.nota.oxygen.ResultsListener;
import dk.nota.xml.DocumentResult;
import net.sf.saxon.s9api.XdmNode;

public class DocxToEpubWorker
		extends AbstractWorkerWithResults<DocumentResult,Object> {
	
	private LinkedList<URL> affectedEditorUrls;
	private EpubAccess epubAccess;
	private XdmNode opfDocument;
	private File[] sourceFiles;

	public DocxToEpubWorker(EpubAccess epubAccess, XdmNode opfDocument,
			ResultsListener listener, File[] sourceFiles,
			LinkedList<URL> affectedEditorUrls) {
		super("DOCX-TO-EPUB CONVERSION", listener);
		this.affectedEditorUrls = affectedEditorUrls;
		this.epubAccess = epubAccess;
		this.opfDocument = opfDocument;
		this.sourceFiles = sourceFiles;
	}

	@Override
	protected DocumentResult doInBackground() throws Exception {
		fireResultsUpdate("DOCX-TO-EPUB CONVERSION STARTING");
		DocxToEpubConverter docxToEpubConverter = new DocxToEpubConverter(
				opfDocument, sourceFiles);
		DocumentResult documentResult = new DocumentResult(docxToEpubConverter
				.call());
		documentResult.writeDocumentsToArchive(epubAccess.getArchiveAccess());
		return documentResult;
	}
	
	@Override
	protected void done() {
		super.done();
		affectedEditorUrls.forEach(
				url -> EditorAccessProvider.getEditorAccess().open(url));
	}

}
