package dk.nota.oxygen.epub.opf;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.ExecutionException;

import dk.nota.oxygen.common.ImageStoringResultsListener;
import dk.nota.oxygen.epub.common.AbstractEpubWorkerWithResults;
import dk.nota.oxygen.epub.common.EpubAccess;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.XdmDestination;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XsltTransformer;
import ro.sync.document.DocumentPositionedInfo;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;

public class CreateDtbWorker
		extends AbstractEpubWorkerWithResults<XdmNode,Object> {
	
	private File outputFile;
	private String dtbIdentifier;
	
	public CreateDtbWorker(String title, ImageStoringResultsListener imageListener,
			EpubAccess epubAccess, File outputFile) {
		super(title, imageListener, epubAccess);
		this.outputFile = outputFile;
	}
	
	public void copyImages(URI outputFolderUri) throws IOException {
		fireResultsUpdate("COPYING IMAGES...");
		Files.createDirectories(Paths.get(outputFolderUri));
		FileSystem epubFileSystem = getEpubAccess().getEpubAsFileSystem();
		for (String imageUrl : ((ImageStoringResultsListener)
				getResultsListener()).getImageUrls()) {
			Path imagePath = epubFileSystem.getPath("/EPUB/", imageUrl);
			Files.copy(imagePath, Paths.get(outputFolderUri.resolve(imagePath
					.getFileName().toString())), StandardCopyOption
					.REPLACE_EXISTING);
		}
		epubFileSystem.close();
	}

	@Override
	protected XdmNode doInBackground() throws Exception {
		XsltTransformer concatTransformer = getEpubAccess()
				.getConcatTransformer(getResultsListener(), getResultsListener());
		XsltTransformer dtbConverter = getEpubAccess().getDtbConverter(
				getResultsListener(), getResultsListener());
		dtbConverter.setDestination(new XdmDestination());
		if (dtbIdentifier != null) dtbConverter.setParameter(new QName(
				"IDENTIFIER"), new XdmAtomicValue(dtbIdentifier));
		dtbConverter.setParameter(new QName("NAV_DOCUMENT"), getEpubAccess()
				.getNavigationDocument());
		dtbConverter.setParameter(new QName("OPF_DOCUMENT"), getEpubAccess()
				.getOpfDocument());
		concatTransformer.setDestination(dtbConverter);
		concatTransformer.setParameter(new QName("UPDATE_EPUB"),
				new XdmAtomicValue(false));
		concatTransformer.transform();
		return ((XdmDestination)dtbConverter.getDestination()).getXdmNode();
	}
	
	@Override
	protected void done() {
		// The following operations ought not to be executed on the EDT;
		// however, it makes sense to have doInBackground() return the DTBook
		// document in the current setup, which makes this the logical place
		// for creating the file and copying images for the time being.
		// TODO: Create classes - Concatter, DtbConverter - with just the
		// relevant functionality and use the workers to combine them, rather
		// than relying on SwingWorker output
		Serializer serializer = getEpubAccess().getEpubXmlAccess()
				.getSerializer(outputFile);
		try {
			// Serialize output of doInBackground() to file
			serializer.serializeNode(get());
			serializer.close();
			// Copy images
			copyImages(outputFile.getParentFile().toURI());
			// Notify listener that we're done
			fireResultsUpdate("DTBOOK CONVERSION DONE");
			// Open DTBook file in editor
			PluginWorkspaceProvider.getPluginWorkspace().open(outputFile.toURI()
					.toURL());
		} catch (ExecutionException | InterruptedException |
				SaxonApiException e) {
			getResultsListener().writeException(
					e instanceof ExecutionException ? e.getCause() : e,
					DocumentPositionedInfo.SEVERITY_FATAL);
		} catch (MalformedURLException e) {
			PluginWorkspaceProvider.getPluginWorkspace().showErrorMessage(
					"Unable to open DTBook file due to malformed URL", e);
		} catch (IOException e) {
			getResultsListener().writeException(e,
					DocumentPositionedInfo.SEVERITY_ERROR);
		}
	}
	
	public File getFile() {
		return outputFile;
	}
	
	public void setDtbIdentifier(String identifier) {
		dtbIdentifier = identifier;
	}

}
