package dk.nota.epub.content;

import dk.nota.epub.EpubAccess;
import dk.nota.oxygen.AbstractWorkerWithResults;
import dk.nota.oxygen.ResultsListener;
import dk.nota.xml.DocumentResult;
import net.sf.saxon.s9api.XdmNode;

public abstract class AbstractContentWorkerWithResults
		extends AbstractWorkerWithResults<DocumentResult,Object> {
	
	public AbstractContentWorkerWithResults(String title, EpubAccess epubAccess,
			XdmNode opfDocument, ResultsListener resultsListener) {
		super(title, resultsListener);
	}

}
