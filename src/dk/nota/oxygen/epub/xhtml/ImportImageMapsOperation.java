package dk.nota.oxygen.epub.xhtml;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.file.Files;
import java.util.HashMap;

import javax.xml.transform.stream.StreamSource;

import dk.nota.oxygen.common.AbstractAuthorOperation;
import dk.nota.oxygen.common.EditorAccess;
import dk.nota.oxygen.common.ResultsView;
import dk.nota.oxygen.common.ResultsViewImageListener;
import dk.nota.oxygen.epub.common.EpubAccess;
import dk.nota.oxygen.epub.plugin.EpubPluginExtension;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.XsltTransformer;
import ro.sync.ecss.extensions.api.ArgumentDescriptor;
import ro.sync.ecss.extensions.api.ArgumentsMap;
import ro.sync.ecss.extensions.api.AuthorOperationException;

public class ImportImageMapsOperation extends AbstractAuthorOperation {
	
	private int depth;
	private EditorAccess editorAccess;
	private EpubAccess epubAccess;
	private ResultsViewImageListener imageListener;
	
	private void insertImages() throws IOException, SaxonApiException {
		HashMap<String,String> fileTypes = new HashMap<String,String>();
		for (String imagePath : imageListener.getImagePaths()) {
			File file = new File(imagePath.substring(5));
			fileTypes.put("images/" + file.getName(), Files
					.probeContentType(file.toPath()));
			epubAccess.copyFileToImageFolder(file);
		}
		epubAccess.addItemReferencesToOpf(fileTypes, "image", false);
	}
	
	private String createFragment(File[] imageMapFiles) throws IOException,
			SaxonApiException {
		// Use a writer to create the input
		StringWriter inputWriter = new StringWriter();
		// Add XML declarations and root element
		inputWriter.write(
				"<?xml version=\"1.0\" encoding=\"iso-8859-1\"?>\n"
				+ "<html xmlns=\"http://www.w3.org/1999/xhtml\">");
		// Iterate over chosen files
		for (File imageMapFile : imageMapFiles) {
			BufferedReader reader = new BufferedReader(new FileReader(
					imageMapFile));
			// Skip XML declaration in individual files
			reader.skip(44);
			// Replace invalid nohref attributes, root element and doctype
			reader.lines().forEach(s -> inputWriter.write(s.replaceAll(
					"(nohref |</*html>|<!DOCTYPE .+?>)", "")));
			reader.close();
		}
		inputWriter.write("</html>"); // Close root element
		XsltTransformer imageMapImporter = editorAccess.getEpubAccess()
				.getXmlAccess().getXsltTransformer("import-image-maps.xsl");
		StringWriter outputWriter = new StringWriter();
		imageMapImporter.setDestination(editorAccess.getEpubAccess()
				.getXmlAccess().getSerializer(outputWriter));
		imageMapImporter.setParameter(new QName("INSERTION_DEPTH"),
				new XdmAtomicValue(depth));
		imageMapImporter.setParameter(new QName("OUTPUT_FOLDER_URL"),
				new XdmAtomicValue(imageMapFiles[0].toURI().toString()));
		// Add image listener to get list of images
		imageMapImporter.setErrorListener(imageListener);
		imageMapImporter.setMessageListener(imageListener);
		imageMapImporter.setSource(new StreamSource(new StringReader(
					inputWriter.toString())));
		imageMapImporter.transform();
		return outputWriter.toString();
	}

	@Override
	protected void doOperation() throws AuthorOperationException {
		try {
			editorAccess = EpubPluginExtension.getEditorAccess();
			epubAccess = editorAccess.getEpubAccess();
			imageListener = new ResultsViewImageListener(new ResultsView(
					epubAccess.getPid() + " - Import image maps"));
			File[] imageMapFiles = getWorkspace().chooseFiles(null, "Insert",
					new String[] { "html" }, "Image maps");
			if (imageMapFiles == null) return;
			String fragment = createFragment(imageMapFiles);
			insertImages();
			getDocumentController().insertXMLFragment(fragment,
					getSelectionStart());
		} catch (IOException | SaxonApiException e) {
			throw new AuthorOperationException(e.getMessage());
		}
	}

	@Override
	public ArgumentDescriptor[] getArguments() {
		return new ArgumentDescriptor[] {
				new ArgumentDescriptor("depth", ArgumentDescriptor
						.TYPE_STRING, "Depth at the insertion point")
		};
	}

	@Override
	public String getDescription() {
		return "Merges image maps into document at current position";
	}

	@Override
	protected void parseArguments(ArgumentsMap arguments)
			throws IllegalArgumentException {
		depth = Integer.parseInt((String)arguments.getArgumentValue("depth"));
	}

}
