package dk.nota.epub.content;

import java.io.OutputStream;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import dk.nota.archive.ArchiveAccess;
import dk.nota.epub.EpubAccess;
import dk.nota.epub.xml.EpubDocumentMap;
import dk.nota.epub.xml.EpubXmlAccessProvider;
import dk.nota.oxygen.common.AbstractWorkerWithResults;
import dk.nota.oxygen.common.ResultsListener;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XdmNode;

public class ConcatWorker extends AbstractWorkerWithResults<Object,Object> {
	
	private EpubAccess epubAccess;
	private XdmNode opfDocument;

	public ConcatWorker(EpubAccess epubAccess, XdmNode opfDocument,
			ResultsListener listener) {
		super("CONCAT", listener);
		this.epubAccess = epubAccess;
		this.opfDocument = opfDocument;
	}

	@Override
	protected Object doInBackground() throws Exception {
		fireResultsUpdate("CONCAT STARTING");
		Concatter concatter = new Concatter(opfDocument, true);
		concatter.addListener(getResultsListener());
		EpubDocumentMap epubDocumentMap = new EpubDocumentMap(concatter.call());
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
			for (URI uri : concatter.getOriginalDocuments()) {
				Path path = epubFileSystem.getPath(archiveAccess
						.relativizeUriToArchive(uri));
				fireResultsUpdate("Deleting " + path.getFileName());
				Files.delete(path);
			}
		} finally {
			genericSerializer.close();
			xhtmlSerializer.close();
		}
		return null;
	}

}
