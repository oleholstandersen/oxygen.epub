package dk.nota.oxygen.epub.opf;

import dk.nota.oxygen.common.ResultsView;
import java.net.URL;
import java.util.LinkedList;

import javax.xml.transform.SourceLocator;

import dk.nota.oxygen.common.EditorAccess;
import dk.nota.oxygen.common.ResultsListener;
import dk.nota.oxygen.epub.common.AbstractEpubWorkerWithResults;
import dk.nota.oxygen.epub.common.ArchiveSensitiveAction;
import dk.nota.oxygen.epub.common.EpubAccess;
import dk.nota.oxygen.xml.EpubXmlAccess;
import net.sf.saxon.s9api.Axis;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmSequenceIterator;
import net.sf.saxon.s9api.XsltTransformer;

public class ConcatAction extends ArchiveSensitiveAction {

	public ConcatAction() {
		super("Concat");
	}
	
	@Override
	public void actionPerformed(EditorAccess editorAccess) {
		EpubAccess epubAccess = editorAccess.getEpubAccess();
		LinkedList<String> documentUrls = new LinkedList<String>();
		ResultsListener resultsListener = new ResultsListener(new ResultsView(
				epubAccess.getPid() + " - Concat")) {
			@Override
			protected void handleMessage(XdmNode message, boolean terminate,
					SourceLocator sourceLocator) {
				XdmSequenceIterator messageIterator = message.axisIterator(Axis
						.DESCENDANT_OR_SELF, new QName(EpubXmlAccess
								.NOTA_NAMESPACE, "document"));
				while (messageIterator.hasNext()) documentUrls.add(
						messageIterator.next().getStringValue());
			}
		};
		AbstractEpubWorkerWithResults<Object,Object> concatWorker =
				new AbstractEpubWorkerWithResults<Object,Object>("CONCAT",
						resultsListener, epubAccess) {
					@Override
					protected Object doInBackground() throws Exception {
						XsltTransformer concatTransformer = getEpubAccess()
								.getConcatTransformer(resultsListener,
										resultsListener);
						concatTransformer.setParameter(new QName("UPDATE_EPUB"),
								new XdmAtomicValue(true));
						XsltTransformer outputTransformer = getEpubAccess()
								.getOutputTransformer(resultsListener,
										resultsListener);
						concatTransformer.setDestination(outputTransformer);
						concatTransformer.transform();
						outputTransformer.close();
						resultsListener.getResultsView().writeResult(
								"DELETING DOCUMENTS...");
						for (String documentUrl : documentUrls) {
							resultsListener.getResultsView().writeResult(
									"Deleting " + documentUrl);
							editorAccess.getWorkspace().delete(new URL(
									getEpubAccess().getContentFolderUrl(),
									documentUrl));
						}
						return null;
					}
		};
		concatWorker.execute();
	}

}
