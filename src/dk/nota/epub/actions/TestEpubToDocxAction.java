package dk.nota.epub.actions;

import java.io.File;
import java.net.URL;
import java.util.LinkedList;

import dk.nota.epub.EpubException;
import dk.nota.epub.conversion.EpubToDocxWorker;
import dk.nota.oxygen.EditorAccess;
import dk.nota.oxygen.ResultsListener;
import dk.nota.oxygen.ResultsView;
import net.sf.saxon.s9api.XdmNode;

public class TestEpubToDocxAction extends EpubAction {

	public TestEpubToDocxAction() {
		super("Docx", false);
	}

	@Override
	public void actionPerformed(EditorAccess editorAccess,
			LinkedList<URL> affectedEditorUrls) {
		XdmNode opfDocument;
		try {
			opfDocument = epubAccess.getContentAccess().getOpfDocument();
		} catch (EpubException e) {
			editorAccess.showErrorMessage("Unable to get OPF document", e);
			return;
		}
		String docxFileName = epubAccess.getPid().replaceFirst("dk-nota-", "")
				+ ".docx";
		File docxFile = null;
		docxFile = editorAccess.getPluginWorkspace().chooseFile(new File(
				epubAccess.getArchiveUri().resolve(docxFileName)),
				"Export [Docx]", new String[] { "docx" }, "Word documents",
				true);
		if (docxFile == null) return;
		EpubToDocxWorker epubToDocxWorker = new EpubToDocxWorker(epubAccess,
				opfDocument, new ResultsListener(new ResultsView(epubAccess
						.getPid() + " - Convert to Docx")), docxFile);
		epubToDocxWorker.execute();
	}

}
