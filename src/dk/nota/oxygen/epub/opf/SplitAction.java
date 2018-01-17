package dk.nota.oxygen.epub.opf;

import dk.nota.oxygen.common.ResultsView;
import dk.nota.oxygen.common.EditorAccess;
import dk.nota.oxygen.epub.common.ArchiveSensitiveAction;
import dk.nota.oxygen.epub.common.EpubAccess;

public class SplitAction extends ArchiveSensitiveAction {

	public SplitAction() {
		super("Split");
	}

	@Override
	public void actionPerformed(EditorAccess editorAccess) {
		EpubAccess epubAccess = editorAccess.getEpubAccess();
		SplitWorker splitWorker = new SplitWorker(epubAccess,
				new ResultsView(epubAccess.getPid() + " - Split"));
		splitWorker.execute();
	}

}
