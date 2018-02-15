package dk.nota.oxygen.common;

import java.util.LinkedList;

import javax.xml.transform.SourceLocator;

import dk.nota.oxygen.xml.EpubXmlAccess;
import dk.nota.oxygen.xml.ResultsViewListener;
import net.sf.saxon.s9api.Axis;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmSequenceIterator;

public class ResultsViewDocumentListener extends ResultsViewListener {
	
	private LinkedList<String> documentPaths = new LinkedList<String>();
	
	public ResultsViewDocumentListener(ResultsView resultsView) {
		super(resultsView);
	}
	
	public LinkedList<String> getDocumentPaths() {
		return documentPaths;
	}
	
	@Override
	public void handleMessage(XdmNode message, boolean terminate,
			SourceLocator sourceLocator) {
		XdmSequenceIterator messageIterator = message.axisIterator(Axis
				.DESCENDANT_OR_SELF, new QName(EpubXmlAccess.NOTA_NAMESPACE,
						"document"));
		while (messageIterator.hasNext()) documentPaths.add(messageIterator
				.next().getStringValue());						
	}

}
