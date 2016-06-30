package dk.nota.oxygen.epub.xhtml;

import java.awt.event.ActionEvent;
import java.io.File;
import java.net.MalformedURLException;
import java.util.LinkedList;

import javax.swing.AbstractAction;

import dk.nota.oxygen.common.EditorAccess;
import dk.nota.oxygen.epub.common.EpubAccess;
import dk.nota.oxygen.epub.plugin.EpubPluginExtension;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmNode;

public class ImportCatListAction extends AbstractAction {
	
	public ImportCatListAction() {
		super("Import cat list");
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		EditorAccess editorAccess = EpubPluginExtension.getEditorAccess();
		EpubAccess epubAccess = editorAccess.getEpubAccess();
		File[] catListFiles = editorAccess.getWorkspace().chooseFiles(null,
				"Import cat list", new String[] { "kat" }, "Cat lists");
		LinkedList<XdmNode> catListDocuments = new LinkedList<XdmNode>();
		try {
			for (File catListFile : catListFiles) {
				catListDocuments.add(epubAccess.getXmlAccess().getDocument(
						catListFile.toURI().toURL()));
			}
		} catch (MalformedURLException | SaxonApiException e) {
			editorAccess.showErrorMessage(e.toString());
		}
		for (XdmNode catListDocument : catListDocuments) {
			editorAccess.showInformationMessage(catListDocument.toString());
		}
	}

}
