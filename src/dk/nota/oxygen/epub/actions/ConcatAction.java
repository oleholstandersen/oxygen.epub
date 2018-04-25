package dk.nota.oxygen.epub.actions;

import java.net.URL;
import java.util.LinkedList;

import dk.nota.epub.EpubException;
import dk.nota.epub.content.ConcatWorker;
import dk.nota.oxygen.EditorAccess;
import dk.nota.oxygen.ResultsListener;
import dk.nota.oxygen.ResultsView;
import net.sf.saxon.s9api.XdmNode;

public class ConcatAction extends EpubAction {
	
	public ConcatAction() {
		super("Concat", true);
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
		ConcatWorker concatWorker = new ConcatWorker(epubAccess,
				opfDocument, new ResultsListener(new ResultsView(epubAccess
				.getPid() + " - Concat")), affectedEditorUrls);
		concatWorker.execute();
	}

}