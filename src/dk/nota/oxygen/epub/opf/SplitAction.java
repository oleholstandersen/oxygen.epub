package dk.nota.oxygen.epub.opf;

import dk.nota.oxygen.common.ConsoleWindow;
import dk.nota.oxygen.common.EditorAccess;
import dk.nota.oxygen.epub.common.ArchiveSensitiveAction;
import dk.nota.oxygen.epub.common.EpubAccess;
import dk.nota.oxygen.xml.ConsoleListener;

public class SplitAction extends ArchiveSensitiveAction {

	public SplitAction() {
		super("Split");
	}

	@Override
	public void actionPerformed(EditorAccess editorAccess) {
		ConsoleListener messageListener = 
				new ConsoleListener(new ConsoleWindow("Split"));
		EpubAccess epubAccess = editorAccess.getEpubAccess();
		SplitWorker splitWorker = new SplitWorker(epubAccess, messageListener);
		splitWorker.execute();
	}

}
