package dk.nota.epub.content;

import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.LinkedList;

import dk.nota.archive.ArchiveAccess;
import dk.nota.epub.EpubAccess;
import dk.nota.epub.xml.EpubXmlAccessProvider;
import dk.nota.oxygen.AbstractWorkerWithResults;
import dk.nota.oxygen.EditorAccessProvider;
import dk.nota.oxygen.ResultsListener;
import dk.nota.xml.DocumentTransformationResult;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XdmNode;

public class SplitWorker
		extends AbstractWorkerWithResults<DocumentTransformationResult,Object> {
	
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
	protected DocumentTransformationResult doInBackground() throws Exception {
		fireResultsUpdate("SPLIT STARTING");
		XdmNode concatDocument = EpubXmlAccessProvider.getEpubXmlAccess()
				.getDocument(epubAccess.makeOpfBasedUri("concat.xhtml"));
		Splitter splitter = new Splitter(concatDocument, opfDocument);
		splitter.addListener(getResultsListener());
		DocumentTransformationResult epubDocumentMap = new DocumentTransformationResult(splitter.call());
		ArchiveAccess archiveAccess = epubAccess.getArchiveAccess();
		Serializer genericSerializer = EpubXmlAccessProvider.getEpubXmlAccess()
				.getSerializer();
		Serializer xhtmlSerializer = EpubXmlAccessProvider.getEpubXmlAccess()
				.getXhtmlSerializer();
		Serializer serializer;
		try (FileSystem epubFileSystem = archiveAccess
				.getArchiveAsFileSystem()) {
			for (URI uri : epubDocumentMap.getUris()) {
				Path path = epubFileSystem.getPath(archiveAccess
						.relativizeUriToArchive(uri));
				fireResultsUpdate("Writing " + path.getFileName());
				serializer = path.endsWith(".xhtml") ? xhtmlSerializer :
					genericSerializer;
				try (OutputStream outputStream = Files.newOutputStream(path,
						StandardOpenOption.CREATE)) {
					serializer.setOutputStream(outputStream);
					serializer.serializeNode(epubDocumentMap.getDocument(uri));
				}
			}
		} finally {
			genericSerializer.close();
			xhtmlSerializer.close();
		}
		return epubDocumentMap;
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
