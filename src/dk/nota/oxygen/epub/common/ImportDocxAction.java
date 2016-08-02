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
		if (editorAccess.getCurrentEditor().isModified()) {
			editorAccess.showErrorMessage("Unsaved changes in document: "
					+ "please review and save before trying again");
			return;
		}
		EpubAccess epubAccess = editorAccess.getEpubAccess();
		java.io.File[] docxFiles = editorAccess.getWorkspace().chooseFiles(
				null, "Import", new String[] {"docx"}, "Word documents");
		try {
			XsltTransformer docxImporter = epubAccess.getEditorTransformer(
					"docx-import.xsl");
			LinkedList<XdmItem> wordFolderUrls = new LinkedList<XdmItem>();
			for (java.io.File file : docxFiles) wordFolderUrls.add(
					new XdmAtomicValue("zip:" + file.toURI().toString() +
							"!/word/"));
			docxImporter.setParameter(new QName("WORD_FOLDER_URLS"),
					new XdmValue(wordFolderUrls));
			docxImporter.setDestination(epubAccess.getOutputTransformer());
			docxImporter.transform();
		} catch (SaxonApiException e) {
			editorAccess.showErrorMessage(e.toString());
		}
	}
}
