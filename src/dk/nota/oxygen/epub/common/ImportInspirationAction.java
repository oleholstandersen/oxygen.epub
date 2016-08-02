package dk.nota.oxygen.epub.common;

import java.awt.event.ActionEvent;
import java.util.LinkedList;

import javax.swing.AbstractAction;

import dk.nota.oxygen.common.ConsoleWindow;
import dk.nota.oxygen.common.EditorAccess;
import dk.nota.oxygen.epub.plugin.EpubPluginExtension;
import dk.nota.oxygen.xml.ConsoleListener;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmValue;
import net.sf.saxon.s9api.XsltTransformer;

public class ImportInspirationAction extends AbstractAction {
	
	public ImportInspirationAction() {
		super("Import Inspiration");
	}
	
	@Override
	public void actionPerformed(ActionEvent event) {
		EditorAccess editorAccess = EpubPluginExtension.getEditorAccess();
		EpubAccess epubAccess = editorAccess.getEpubAccess();
		java.io.File[] sourceFiles = editorAccess.getWorkspace().chooseFiles(
				null, "Import", new String[] {"docx", "kat"},
				"Inspiration source files");
		ConsoleListener listener = new ConsoleListener(new ConsoleWindow(
				"Import Inspiration"));
		try {
			XsltTransformer inspirationImporter = epubAccess
					.getEditorTransformer("docx-import.xsl");
			inspirationImporter.setErrorListener(listener);
			inspirationImporter.setMessageListener(listener);
			LinkedList<XdmItem> sourceFileUrls = new LinkedList<XdmItem>();
			for (java.io.File sourceFile : sourceFiles) {
				String url = sourceFile.getName().endsWith(".docx") ?
						"zip:" + sourceFile.toURI().toString() + "!/word/" :
						// ASCII encoding is for some reason required for
						// resolving DTD references in .kat files with
						// non-ASCII URIs
						sourceFile.toURI().toASCIIString();
				sourceFileUrls.add(new XdmAtomicValue(url));
			}
			inspirationImporter.setParameter(
					new QName("INSPIRATION_SOURCE_URLS"),
					new XdmValue(sourceFileUrls));
			inspirationImporter.setDestination(
					epubAccess.getOutputTransformer());
			inspirationImporter.transform();
		} catch (SaxonApiException e) {
			editorAccess.showErrorMessage(e.toString());
		}
	}
}
