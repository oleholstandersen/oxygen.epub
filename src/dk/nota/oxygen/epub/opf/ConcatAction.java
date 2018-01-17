package dk.nota.oxygen.epub.opf;

import dk.nota.oxygen.common.ResultsView;
import dk.nota.oxygen.common.EditorAccess;
import dk.nota.oxygen.epub.common.ArchiveSensitiveAction;
import dk.nota.oxygen.epub.common.EpubAccess;

public class ConcatAction extends ArchiveSensitiveAction {

	public ConcatAction() {
		super("Concat");
	}
	
	@Override
	public void actionPerformed(EditorAccess editorAccess) {
		EpubAccess epubAccess = editorAccess.getEpubAccess();
		ConcatWorker concatWorker = new ConcatWorker(editorAccess, epubAccess,
				new ResultsView(epubAccess.getPid() + " - Concat"));
		concatWorker.execute();
	}

}
