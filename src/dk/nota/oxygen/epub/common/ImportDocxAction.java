package dk.nota.oxygen.epub.common;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import dk.nota.oxygen.common.EditorAccess;
import dk.nota.oxygen.epub.plugin.EpubPluginExtension;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XsltTransformer;

public class ImportDocxAction extends AbstractAction {
	
	private boolean split;
	
	public ImportDocxAction(boolean createNewDocument) {
		super("Import Docx");
		this.split = createNewDocument;
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
			XsltTransformer docxImporter = epubAccess.getDocxImporter(
					docxFiles, split);
			docxImporter.setDestination(epubAccess.getOutputTransformer());
			docxImporter.transform();
		} catch (SaxonApiException e) {
			editorAccess.showErrorMessage(e.toString());
		}
	}
}
