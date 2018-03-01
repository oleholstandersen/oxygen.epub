package dk.nota.oxygen.epub.opf;

import dk.nota.oxygen.common.ResultsView;
import dk.nota.oxygen.common.EditorAccess;
import dk.nota.oxygen.common.ResultsListener;
import dk.nota.oxygen.epub.common.AbstractEpubWorkerWithResults;
import dk.nota.oxygen.epub.common.ArchiveSensitiveAction;
import dk.nota.oxygen.epub.common.EpubAccess;
import net.sf.saxon.s9api.XsltTransformer;

public class SplitAction extends ArchiveSensitiveAction {

	public SplitAction() {
		super("Split");
	}

	@Override
	public void actionPerformed(EditorAccess editorAccess) {
		EpubAccess epubAccess = editorAccess.getEpubAccess();
		ResultsListener resultsListener = new ResultsListener(new ResultsView(
				epubAccess.getPid() + " - Split"));
		AbstractEpubWorkerWithResults<Object,Object> splitWorker =
				new AbstractEpubWorkerWithResults<Object,Object>("SPLIT", resultsListener,
						epubAccess) {
					@Override
					protected Object doInBackground() throws Exception {
						XsltTransformer splitTransformer = getEpubAccess()
								.getSplitTransformer(resultsListener,
										resultsListener);
						XsltTransformer outputTransformer = getEpubAccess()
								.getOutputTransformer(resultsListener,
										resultsListener);
						splitTransformer.setDestination(outputTransformer);
						splitTransformer.transform();
						outputTransformer.close();
						return null;
					}
		};
		splitWorker.execute();
	}

}
