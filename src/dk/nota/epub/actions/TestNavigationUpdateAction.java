package dk.nota.epub.actions;

import java.net.URL;
import java.util.LinkedList;

import dk.nota.epub.EpubException;
import dk.nota.epub.content.NavigationUpdateWorker;
import dk.nota.oxygen.EditorAccess;
import dk.nota.oxygen.ResultsListener;
import dk.nota.oxygen.ResultsView;
import net.sf.saxon.s9api.XdmNode;

public class TestNavigationUpdateAction extends EpubAction {
	
	public TestNavigationUpdateAction() {
		super("Update navigation");
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
		NavigationUpdateWorker navigationUpdateWorker =
				new NavigationUpdateWorker(epubAccess, opfDocument,
				new ResultsListener(new ResultsView(epubAccess.getPid() +
				" - Update navigation")), affectedEditorUrls);
		navigationUpdateWorker.execute();
	}

}
