package dk.nota.oxygen.epub.opf;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import dk.nota.oxygen.common.ResultsView;
import dk.nota.oxygen.common.EditorAccess;
import dk.nota.oxygen.epub.common.EpubAccess;
import net.sf.saxon.s9api.XdmDestination;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XsltTransformer;

public class CreateDocxWorker extends CreateDtbWorker {
	
	private File docxFile;
	private String docxFileName;
	private URI tempDocxFolderUri;
	
	public CreateDocxWorker(EditorAccess editorAccess, EpubAccess epubAccess,
			ResultsView resultsView, File docxFile) {
		super(editorAccess, epubAccess, resultsView, docxFile, true, false);
		this.docxFile = docxFile;
		docxFileName = docxFile.getName();
		tempDocxFolderUri = docxFile.toURI().resolve(docxFileName + ".tmp/");
	}

	@Override
	protected Object doInBackground() throws Exception {
		XdmNode dtbDocument = (XdmNode)super.doInBackground();
		getResultsView().writeResult("CONVERTING TO DOCX...");
		XsltTransformer docxTransformer = getEpubAccess().getEpubXmlAccess()
				.getXsltTransformer("dtbook2docx/dtbook2docx.xsl");
		docxTransformer.setInitialContextNode(dtbDocument);
		docxTransformer.setDestination(new XdmDestination());
		docxTransformer.setBaseOutputURI(tempDocxFolderUri.toString());
		docxTransformer.transform();
		copyImages(tempDocxFolderUri.resolve("word/media/"));
		Path tempDocxFolderPath = Paths.get(tempDocxFolderUri);
		Path docxFilePath = tempDocxFolderPath.resolve("../" + docxFileName);
		try (ZipOutputStream zipOutputStream = new ZipOutputStream(
				Files.newOutputStream(docxFilePath))) {
					Files.walk(tempDocxFolderPath)
						.filter(path -> !Files.isDirectory(path))
						.forEach(path -> {
							ZipEntry zipEntry = new ZipEntry(tempDocxFolderPath
									.relativize(path).toString());
							try {
								zipOutputStream.putNextEntry(zipEntry);
								zipOutputStream.write(Files.readAllBytes(path));
								zipOutputStream.closeEntry();
								Files.delete(path);
							} catch (Exception e) {
								getEditorAccess().showErrorMessage(e.toString());
							}
				});
		}
		Files.walkFileTree(tempDocxFolderPath, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult postVisitDirectory(Path folder,
					IOException exception) throws IOException {
				Files.delete(folder);
				return FileVisitResult.CONTINUE;
			}
		});
		return null;
	}
	
	@Override
	protected void done() {
		getResultsView().writeResult("DOCX CONVERSION DONE", docxFile.toURI()
				.toString());
	}

}
