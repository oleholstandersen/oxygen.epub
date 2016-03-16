package dk.nota.oxygen.epub.nav;

import dk.nota.oxygen.common.ConsoleWindow;
import dk.nota.oxygen.common.EditorAccess;
import dk.nota.oxygen.epub.common.ArchiveSensitiveAction;
import dk.nota.oxygen.epub.common.EpubAccess;

public class UpdateNavigationAction extends ArchiveSensitiveAction {

	public UpdateNavigationAction() {
		super("Update navigation");
	}

	@Override
	public void actionPerformed(EditorAccess editorAccess) {
		EpubAccess epubAccess = editorAccess.getEpubAccess();
		UpdateNavigationWorker updateNavigationWorker =
				new UpdateNavigationWorker(epubAccess, new ConsoleWindow(
						"Update navigation"));
		updateNavigationWorker.execute();
	}

}
