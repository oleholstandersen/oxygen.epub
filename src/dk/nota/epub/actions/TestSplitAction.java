package dk.nota.epub.actions;

import java.net.URL;
import java.util.LinkedList;

import dk.nota.epub.EpubException;
import dk.nota.epub.content.SplitWorker;
import dk.nota.oxygen.EditorAccess;
import dk.nota.oxygen.ResultsListener;
import dk.nota.oxygen.ResultsView;
import net.sf.saxon.s9api.XdmNode;

public class TestSplitAction extends EpubAction {
	
	public TestSplitAction() {
		super("Split");
	}

	@Override
	public void actionPerformed(EditorAccess editorAccess,
			LinkedList<URL> affectedEditorUrls) {
		try {
			XdmNode opfDocument = epubAccess.getContentAccess()
					.getOpfDocument();
			SplitWorker splitWorker = new SplitWorker(epubAccess,
					opfDocument, new ResultsListener(new ResultsView(
					epubAccess.getPid() + " - Split")), affectedEditorUrls);
			splitWorker.execute();
		} catch (EpubException e) {
			editorAccess.showErrorMessage("An error occurred", e);
		}
	}

}
