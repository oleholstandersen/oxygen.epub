package dk.nota.epub.conversion;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.LinkedList;

import dk.nota.archive.ArchiveAccess;
import dk.nota.epub.EpubAccess;
import dk.nota.epub.content.Concatter;
import dk.nota.xml.XmlAccessProvider;
import dk.nota.oxygen.AbstractWorkerWithResults;
import dk.nota.oxygen.EditorAccess;
import dk.nota.oxygen.EditorAccessProvider;
import dk.nota.oxygen.ResultsListener;
import dk.nota.xml.DocumentResult;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XdmNode;

public class EpubToDtbWorker
		extends AbstractWorkerWithResults<DocumentResult,Object> {
	
	private EpubAccess epubAccess;
	private XdmNode opfDocument;
	private URI outputUri;
	
	public EpubToDtbWorker(EpubAccess epubAccess, XdmNode opfDocument,
			ResultsListener listener, URI outputUri) {
		super("EPUB-TO-DTBOOK CONVERSION", listener);
		this.epubAccess = epubAccess;
		this.opfDocument = opfDocument;
		this.outputUri = outputUri;
	}
	
	protected void copyImages(URI outputFolderUri, LinkedList<URI> imageUris)
			throws IOException {
		Path outputFolderPath = Paths.get(outputFolderUri);
		ArchiveAccess archiveAccess = epubAccess.getArchiveAccess();
		try (FileSystem epubFileSystem = archiveAccess
				.getArchiveAsFileSystem()) {
			for (URI imageUri : imageUris) {
				Path imagePath = epubFileSystem.getPath(archiveAccess
						.relativizeUriToArchive(imageUri));
				Path newImagePath = outputFolderPath.resolve(imagePath
						.getFileName().toString());
				Files.copy(imagePath, newImagePath, StandardCopyOption
						.REPLACE_EXISTING);
			}
		}
	}

	@Override
	protected DocumentResult doInBackground() throws Exception {
		fireResultsUpdate("EPUB-TO-DTBOOK CONVERSION STARTING");
		Concatter concatter = new Concatter(opfDocument, false);
		concatter.addListener(getResultsListener());
		DocumentResult documentResult = new DocumentResult(concatter.call());
		XdmNode concatDocument = documentResult.getDocuments().iterator()
				.next();
		EpubToDtbConverter dtbConverter = new EpubToDtbConverter(concatDocument,
				opfDocument, outputUri);
		dtbConverter.addListener(getResultsListener());
		documentResult = new DocumentResult(dtbConverter.call());
		Serializer dtbSerializer = XmlAccessProvider.getXmlAccess()
				.getSerializer();
		dtbSerializer.setOutputProperty(Serializer.Property
				.SAXON_SUPPRESS_INDENTATION, "dd dt hd levelhd li p td th");
		copyImages(outputUri.resolve("./"), concatter.getImages());
		documentResult.writeDocuments(dtbSerializer);
		return documentResult;
	}
	
	@Override
	protected void done() {
		super.done();
		EditorAccess editorAccess = EditorAccessProvider.getEditorAccess();
		try {
			editorAccess.open(outputUri.toURL());
		} catch (MalformedURLException e) {
			editorAccess.showErrorMessage("Unable to convert URI to URL", e);
		}
	}

}
