package dk.nota.epub.actions;

import java.net.URI;

import dk.nota.epub.EpubAccess;
import dk.nota.epub.EpubAccessProvider;
import dk.nota.epub.EpubException;
import dk.nota.epub.content.NavigationUpdateWorker;
import dk.nota.oxygen.common.EditorAccess;
import dk.nota.oxygen.common.ResultsListener;
import dk.nota.oxygen.common.ResultsView;
import dk.nota.oxygen.epub.common.ArchiveSensitiveAction;
import net.sf.saxon.s9api.XdmNode;

public class TestNavigationUpdateAction extends ArchiveSensitiveAction {
	
	public TestNavigationUpdateAction() {
		super("Update navigation");
	}

	@Override
	public void actionPerformed(EditorAccess editorAccess) {
		URI archiveUri = URI.create(editorAccess.getCurrentEditorUrl()
				.toString().replaceFirst("^zip:", "").replaceFirst(
						"\\.epub!/*.*?$", ".epub"));
		try {
			EpubAccess epubAccess = EpubAccessProvider.getEpubAccess(
					archiveUri);
			XdmNode opfDocument = epubAccess.getContentAccess()
					.getOpfDocument();
			NavigationUpdateWorker navigationUpdateWorker =
					new NavigationUpdateWorker(epubAccess, opfDocument,
							new ResultsListener(new ResultsView(epubAccess
									.getPid() + " - Update navigation")));
			navigationUpdateWorker.execute();
		} catch (EpubException e) {
			e.printStackTrace();
		}
	}

}
