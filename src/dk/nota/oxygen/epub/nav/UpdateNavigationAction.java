package dk.nota.oxygen.epub.nav;

import dk.nota.oxygen.common.ConsoleWindow;
import dk.nota.oxygen.common.EditorAccess;
import dk.nota.oxygen.epub.common.ArchiveSensitiveAction;
import dk.nota.oxygen.epub.common.EpubAccess;
import dk.nota.oxygen.xml.ConsoleListener;

public class UpdateNavigationAction extends ArchiveSensitiveAction {

	public UpdateNavigationAction() {
		super("Update navigation");
	}

	@Override
	public void actionPerformed(EditorAccess editorAccess) {
		ConsoleListener messageListener =
				new ConsoleListener(new ConsoleWindow(
						"Update navigation"));
		EpubAccess epubAccess = editorAccess.getEpubAccess();
		UpdateNavigationWorker updateNavigationWorker =
				new UpdateNavigationWorker(epubAccess, messageListener);
		updateNavigationWorker.execute();
	}

}
