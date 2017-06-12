package dk.nota.oxygen.epub.common;

import java.awt.event.ActionEvent;
import java.util.LinkedList;

import javax.swing.AbstractAction;
import dk.nota.oxygen.common.EditorAccess;
import dk.nota.oxygen.epub.plugin.EpubPluginExtension;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmValue;
import net.sf.saxon.s9api.XsltTransformer;

public class ImportDocxAction extends AbstractAction {
	
	public ImportDocxAction() {
		super("Import Docx");
	}
	
	@Override
	public void actionPerformed(ActionEvent event) {
		EditorAccess editorAccess = EpubPluginExtension.getEditorAccess();
		EpubAccess epubAccess = editorAccess.getEpubAccess();
		java.io.File[] sourceFiles = editorAccess.getWorkspace().chooseFiles(
				null, "Import", new String[] {"docx", "kat"},
				"Word documents, cat lists");
		if (sourceFiles == null) return;
		try {
			XsltTransformer inspirationImporter = epubAccess
					.getEditorTransformer("import-docx.xsl");
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
			inspirationImporter.setParameter(new QName("SOURCE_URLS"),
					new XdmValue(sourceFileUrls));
			inspirationImporter.setDestination(
					epubAccess.getOutputTransformer());
			inspirationImporter.transform();
		} catch (SaxonApiException e) {
			editorAccess.showErrorMessage(e.toString());
		}
	}
}
