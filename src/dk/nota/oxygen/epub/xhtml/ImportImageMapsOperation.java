package dk.nota.oxygen.epub.xhtml;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.LinkedList;

import javax.xml.transform.stream.StreamSource;

import dk.nota.oxygen.common.AbstractAuthorOperation;
import dk.nota.oxygen.common.EditorAccess;
import dk.nota.oxygen.epub.plugin.EpubPluginExtension;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XsltTransformer;
import ro.sync.ecss.extensions.api.ArgumentDescriptor;
import ro.sync.ecss.extensions.api.ArgumentsMap;
import ro.sync.ecss.extensions.api.AuthorOperationException;

public class ImportImageMapsOperation extends AbstractAuthorOperation {

	@Override
	protected void doOperation() throws AuthorOperationException {
		try {
			EditorAccess editorAccess = EpubPluginExtension.getEditorAccess();
			File[] imageMapFiles = getWorkspace().chooseFiles(null, "Insert",
					new String[] { "html" }, "Image maps");
			if (imageMapFiles == null) return;
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
			imageMapImporter.setSource(new StreamSource(new StringReader(
						inputWriter.toString())));
			imageMapImporter.transform();
			String fragment = outputWriter.toString();
			getDocumentController().insertXMLFragment(fragment,
					getSelectionStart());
		} catch (IOException | SaxonApiException e) {
			throw new AuthorOperationException(e.getMessage());
		}
	}

	@Override
	public ArgumentDescriptor[] getArguments() {
		return null;
	}

	@Override
	public String getDescription() {
		return "Merges image maps into document at current position";
	}

	@Override
	protected void parseArguments(ArgumentsMap arguments)
			throws IllegalArgumentException {
	}

}
