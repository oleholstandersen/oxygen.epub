package dk.nota.oxygen.epub.nav;

import dk.nota.oxygen.common.ResultsView;

import dk.nota.oxygen.common.EditorAccess;
import dk.nota.oxygen.common.ResultsListener;
import dk.nota.oxygen.epub.common.AbstractEpubWorkerWithResults;
import dk.nota.oxygen.epub.common.ArchiveSensitiveAction;
import dk.nota.oxygen.epub.common.EpubAccess;
import net.sf.saxon.s9api.XsltTransformer;

public class UpdateNavigationAction extends ArchiveSensitiveAction {

	public UpdateNavigationAction() {
		super("Update navigation");
	}

	@Override
	public void actionPerformed(EditorAccess editorAccess) {
		EpubAccess epubAccess = editorAccess.getEpubAccess();
		ResultsListener resultsListener = new ResultsListener(
				new ResultsView(epubAccess.getPid() + " - Update navigation"));
		AbstractEpubWorkerWithResults<Object,Object> updateNavigationWorker =
				new AbstractEpubWorkerWithResults<Object,Object>(
						"NAVIGATION UPDATE", resultsListener, epubAccess) {
					@Override
					protected Object doInBackground() throws Exception {
						XsltTransformer navigationTransformer = getEpubAccess()
								.getNavUpdateTransformer(resultsListener,
										resultsListener);
						XsltTransformer outputTransformer = getEpubAccess()
								.getOutputTransformer(resultsListener,
										resultsListener);
						navigationTransformer.setDestination(outputTransformer);
						navigationTransformer.transform();
						outputTransformer.close();
						return null;
				}
		};
		updateNavigationWorker.execute();
	}

}
