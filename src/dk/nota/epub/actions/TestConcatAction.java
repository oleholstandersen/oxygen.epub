package dk.nota.epub.actions;

import java.net.URL;
import java.util.LinkedList;

import dk.nota.epub.EpubException;
import dk.nota.epub.content.ConcatWorker;
import dk.nota.oxygen.EditorAccess;
import dk.nota.oxygen.common.ResultsListener;
import dk.nota.oxygen.common.ResultsView;
import net.sf.saxon.s9api.XdmNode;

public class TestConcatAction extends EpubAction {
	
	public TestConcatAction() {
		super("Concat");
	}

	@Override
	public void actionPerformed(EditorAccess editorAccess,
			LinkedList<URL> affectedEditorUrls) {
		try {
			XdmNode opfDocument = epubAccess.getContentAccess()
					.getOpfDocument();
			ConcatWorker concatWorker = new ConcatWorker(epubAccess,
					opfDocument, new ResultsListener(new ResultsView(epubAccess
					.getPid() + " - Concat")), affectedEditorUrls);
			concatWorker.execute();
		} catch (EpubException e) {
			editorAccess.showErrorMessage("An error occurred", e);
		}
	}

}
