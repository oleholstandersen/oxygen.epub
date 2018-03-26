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
import dk.nota.epub.xml.EpubDocumentMap;
import dk.nota.epub.xml.EpubXmlAccessProvider;
import dk.nota.oxygen.EditorAccessProvider;
import dk.nota.oxygen.common.AbstractWorkerWithResults;
import dk.nota.oxygen.common.ResultsListener;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XdmNode;

public class NavigationUpdateWorker
		extends AbstractWorkerWithResults<EpubDocumentMap,Object> {
	
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
	protected EpubDocumentMap doInBackground() throws Exception {
		fireResultsUpdate("NAVIGATION UPDATE STARTING");
		NavigationUpdater navigationUpdater = new NavigationUpdater(
				opfDocument);
		navigationUpdater.addListener(getResultsListener());
		EpubDocumentMap epubDocumentMap = new EpubDocumentMap(navigationUpdater
				.call());
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
				serializer = path.endsWith(".xhtml") ? xhtmlSerializer :
					genericSerializer;
				fireResultsUpdate("Writing " + path.getFileName());
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
				url -> EditorAccessProvider.getEditorAccess().open(url));
	}

}
