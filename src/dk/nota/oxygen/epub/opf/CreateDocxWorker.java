package dk.nota.oxygen.epub.opf;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.util.concurrent.ExecutionException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import dk.nota.oxygen.common.ImageStoringResultsListener;
import dk.nota.oxygen.epub.common.EpubAccess;
import net.sf.saxon.s9api.XdmDestination;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XsltTransformer;
import ro.sync.document.DocumentPositionedInfo;

public class CreateDocxWorker extends CreateDtbWorker {
	
	private URI tempDocxFolderUri;
	
	public CreateDocxWorker(ImageStoringResultsListener imageListener,
			EpubAccess epubAccess, File docxFile) {
		super("DOCX CONVERSION", imageListener, epubAccess, docxFile);
		tempDocxFolderUri = docxFile.toURI().resolve(docxFile.getName() +
				".tmp/");
	}

	@Override
	protected XdmNode doInBackground() throws Exception {
		XdmNode dtbDocument = super.doInBackground();
		fireResultsUpdate("CONVERTING TO DOCX...");
		XsltTransformer docxTransformer = getEpubAccess().getEpubXmlAccess()
				.getXsltTransformer("dtbook2docx/dtbook2docx.xsl");
		docxTransformer.setInitialContextNode(dtbDocument);
		docxTransformer.setDestination(new XdmDestination());
		docxTransformer.setBaseOutputURI(tempDocxFolderUri.toString());
		docxTransformer.transform();
		copyImages(tempDocxFolderUri.resolve("word/media/"));
		Path tempDocxFolderPath = Paths.get(tempDocxFolderUri);
		Path docxFilePath = getFile().toPath();
		fireResultsUpdate("ZIPPING FILES...");
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
								getResultsListener().writeException(e,
										DocumentPositionedInfo.SEVERITY_FATAL);
							}
				});
		}
		fireResultsUpdate("DELETING TEMPORARY FILES...");
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
		// Default behaviour: Write to results view that the task is done, or
		// print out exceptions
		try {
			// Catch exceptions from doInBackground()
			get();
			// Write to results view that update is done
			fireResultsUpdate("DOCX CONVERSION DONE");
		} catch (ExecutionException | InterruptedException e) {
			getResultsListener().writeException(
					e instanceof ExecutionException ? e.getCause() : e,
					DocumentPositionedInfo.SEVERITY_FATAL);
		}
	}

}
