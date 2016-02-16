package dk.nota.oxygen.epub.opf;

import dk.nota.oxygen.common.ConsoleWindow;
import dk.nota.oxygen.common.EditorAccess;
import dk.nota.oxygen.epub.common.ArchiveSensitiveAction;
import dk.nota.oxygen.epub.common.EpubAccess;
import dk.nota.oxygen.xml.ConsoleListener;

public class ConcatAction extends ArchiveSensitiveAction {

	public ConcatAction() {
		super("Concat");
	}
	
	@Override
	public void actionPerformed(EditorAccess editorAccess) {
		EpubAccess epubAccess = editorAccess.getEpubAccess();
		ConsoleListener messageListener =
				new ConsoleListener(new ConsoleWindow("Concat"));
		ConcatWorker concatWorker = new ConcatWorker(epubAccess,
				messageListener);
		concatWorker.execute();
	}

}
