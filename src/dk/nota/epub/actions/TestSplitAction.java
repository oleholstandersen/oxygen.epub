package dk.nota.epub.actions;

import java.net.URI;

import dk.nota.epub.EpubAccess;
import dk.nota.epub.EpubAccessProvider;
import dk.nota.epub.EpubException;
import dk.nota.epub.content.SplitWorker;
import dk.nota.oxygen.common.EditorAccess;
import dk.nota.oxygen.common.ResultsListener;
import dk.nota.oxygen.common.ResultsView;
import dk.nota.oxygen.epub.common.ArchiveSensitiveAction;
import net.sf.saxon.s9api.XdmNode;

public class TestSplitAction extends ArchiveSensitiveAction {
	
	public TestSplitAction() {
		super("Split");
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
			SplitWorker splitWorker = new SplitWorker(epubAccess,
					opfDocument, new ResultsListener(new ResultsView(
							epubAccess.getPid() + " - Split")));
			splitWorker.execute();
		} catch (EpubException e) {
			e.printStackTrace();
		}
	}

}
