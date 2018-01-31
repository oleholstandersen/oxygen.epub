package dk.nota.oxygen.epub.opf;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.LinkedList;

import de.schlichtherle.io.File;
import dk.nota.oxygen.common.AbstractResultsWorker;
import dk.nota.oxygen.common.ResultsView;
import dk.nota.oxygen.common.ResultsViewImageListener;
import dk.nota.oxygen.common.EditorAccess;
import dk.nota.oxygen.epub.common.EpubAccess;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.XdmDestination;
import net.sf.saxon.s9api.XsltTransformer;

public class CreateDtbWorker extends AbstractResultsWorker {
	
	private boolean copyImages = true;
	private java.io.File outputFile;
	private String dtbIdentifier;
	private EditorAccess editorAccess;
	private EpubAccess epubAccess;
	private ResultsViewImageListener imageListener;
	private boolean returnDtbDocument = false;
	private boolean success = false;
	
	public CreateDtbWorker(EditorAccess editorAccess, EpubAccess epubAccess,
			ResultsView resultsView, java.io.File outputFile,
			boolean returnDtbDocument) {
		super(resultsView);
		this.outputFile = outputFile;
		this.editorAccess = editorAccess;
		this.epubAccess = epubAccess;
		this.imageListener = new ResultsViewImageListener(resultsView);
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
		imageListener.writeToResultsView("COPYING IMAGE FILES...");
		new File(outputFolderUri).mkdirs();
		for (String imagePath : getImagePaths()) {
			File imageFile = epubAccess.getFileFromContentFolder(imagePath);
			File newImageFile = new File(outputFolderUri.resolve(imageFile
					.getName()));
			newImageFile.archiveCopyFrom(imageFile);
		}
		imageListener.writeToResultsView("IMAGE FILES COPIED");
	}

	@Override
	protected Object doInBackground() throws Exception {
		XsltTransformer concatTransformer = epubAccess.getConcatTransformer(
				imageListener, imageListener);
		XsltTransformer dtbConverter = epubAccess.getDtbConverter(
				imageListener, imageListener);
		if (returnDtbDocument) {
			dtbConverter.setDestination(new XdmDestination());
		} else dtbConverter.setDestination(epubAccess.getXmlAccess()
				.getSerializer(outputFile));
		if (dtbIdentifier != null) dtbConverter.setParameter(new QName(
				"IDENTIFIER"), new XdmAtomicValue(dtbIdentifier));
		dtbConverter.setParameter(new QName("NAV_DOCUMENT"), epubAccess
				.getNavigationDocument());
		dtbConverter.setParameter(new QName("OPF_DOCUMENT"), epubAccess
				.getOpfDocument());
		concatTransformer.setDestination(dtbConverter);
		concatTransformer.setParameter(new QName("UPDATE_EPUB"),
				new XdmAtomicValue(false));
		concatTransformer.transform();
		if (copyImages) copyImages(outputFile.getParentFile().toURI());
		success = true;
		if (returnDtbDocument) return ((XdmDestination)dtbConverter
				.getDestination()).getXdmNode();
		return null;
	}
	
	public EditorAccess getEditorAccess() {
		return editorAccess;
	}
	
	public LinkedList<String> getImagePaths() {
		return imageListener.getImagePaths();
	}
	
	public EpubAccess getEpubAccess() {
		return epubAccess;
	}
	
	public java.io.File getOutputFile() {
		return outputFile;
	}
	
	public void setDtbIdentifier(String identifier) {
		dtbIdentifier = identifier;
	}
	
	@Override
	protected void done() {
		if (success) {
			try {
				if (returnDtbDocument) {
					imageListener.writeToResultsView("DTBOOK CONVERSION DONE");
				} else {
					imageListener.writeToResultsView("DTBOOK CONVERSION DONE",
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
