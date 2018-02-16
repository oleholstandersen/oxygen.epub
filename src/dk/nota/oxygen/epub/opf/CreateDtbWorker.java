package dk.nota.oxygen.epub.opf;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;

import dk.nota.oxygen.common.ResultsView;
import dk.nota.oxygen.common.ResultsViewImageListener;
import dk.nota.oxygen.common.EditorAccess;
import dk.nota.oxygen.epub.common.AbstractEpubResultsWorker;
import dk.nota.oxygen.epub.common.EpubAccess;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.XdmDestination;
import net.sf.saxon.s9api.XsltTransformer;

public class CreateDtbWorker extends AbstractEpubResultsWorker {
	
	private boolean copyImages = true;
	private java.io.File outputFile;
	private String dtbIdentifier;
	private EditorAccess editorAccess;
	private boolean returnDtbDocument = false;
	
	public CreateDtbWorker(EditorAccess editorAccess, EpubAccess epubAccess,
			ResultsView resultsView, java.io.File outputFile,
			boolean returnDtbDocument) {
		super(resultsView, new ResultsViewImageListener(resultsView),
				epubAccess);
		this.outputFile = outputFile;
		this.editorAccess = editorAccess;
		this.returnDtbDocument = returnDtbDocument;
	}
	
	public CreateDtbWorker(EditorAccess editorAccess, EpubAccess epubAccess,
			ResultsView resultsView, java.io.File outputFile,
			boolean returnDtbDocument, boolean copyImages) {
		this(editorAccess, epubAccess, resultsView, outputFile,
				returnDtbDocument);
		this.copyImages = copyImages;
	}
	
	public void copyImages(URI outputFolderUri) throws IOException {
		getListener().writeToResultsView("COPYING IMAGE FILES...");
		Files.createDirectories(Paths.get(outputFolderUri));
		FileSystem epubFileSystem = getEpubAccess().getEpubAsFileSystem();
		for (String imagePathString : getImagePaths()) {
			Path imagePath = epubFileSystem.getPath("/EPUB/", imagePathString);
			Files.copy(imagePath, Paths.get(outputFolderUri.resolve(imagePath
					.getFileName().toString())));
		}
		epubFileSystem.close();
		getListener().writeToResultsView("IMAGE FILES COPIED");
	}

	@Override
	protected Object doInBackground() throws Exception {
		XsltTransformer concatTransformer = getEpubAccess()
				.getConcatTransformer(getListener(), getListener());
		XsltTransformer dtbConverter = getEpubAccess().getDtbConverter(
				getListener(), getListener());
		if (returnDtbDocument) {
			dtbConverter.setDestination(new XdmDestination());
		} else dtbConverter.setDestination(getEpubAccess().getEpubXmlAccess()
				.getSerializer(outputFile));
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
		if (copyImages) copyImages(outputFile.getParentFile().toURI());
		setSuccess();
		if (returnDtbDocument) return ((XdmDestination)dtbConverter
				.getDestination()).getXdmNode();
		return null;
	}
	
	public EditorAccess getEditorAccess() {
		return editorAccess;
	}
	
	public LinkedList<String> getImagePaths() {
		return ((ResultsViewImageListener)getListener()).getImagePaths();
	}
	
	public java.io.File getOutputFile() {
		return outputFile;
	}
	
	public void setDtbIdentifier(String identifier) {
		dtbIdentifier = identifier;
	}
	
	@Override
	protected void done() {
		if (getSuccess()) {
			try {
				if (returnDtbDocument) {
					getListener().writeToResultsView("DTBOOK CONVERSION DONE");
				} else {
					getListener().writeToResultsView("DTBOOK CONVERSION DONE",
							outputFile.toURI().toString());
					editorAccess.getWorkspace().open(outputFile.toURI()
							.toURL());
				}
			} catch (MalformedURLException e) {
				editorAccess.showErrorMessage(e.toString());
			}
		}
	}

}
