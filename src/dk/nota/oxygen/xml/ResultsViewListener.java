package dk.nota.oxygen.xml;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.SourceLocator;
import javax.xml.transform.TransformerException;

import dk.nota.oxygen.common.ResultsView;
import net.sf.saxon.expr.instruct.TerminationException;
import net.sf.saxon.s9api.Axis;
import net.sf.saxon.s9api.MessageListener;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmSequenceIterator;
import ro.sync.document.DocumentPositionedInfo;

public class ResultsViewListener implements ErrorListener, MessageListener {

	private ResultsView resultsView;
	
	public ResultsViewListener(ResultsView consoleWindow) {
		this.resultsView = consoleWindow;
	}
	
	@Override
	public void error(TransformerException exception)
			throws TransformerException {
	}
	
	@Override
	public void fatalError(TransformerException exception)
			throws TransformerException {
		if (exception instanceof TerminationException) return;
		getResultsView().writeResult(new DocumentPositionedInfo(
				DocumentPositionedInfo.SEVERITY_FATAL, "ERROR: " + exception));
	}
	
	public ResultsView getResultsView() {
		return resultsView;
	}
	
	public void handleMessage(XdmNode message, boolean terminate,
			SourceLocator sourceLocator) {
		// Override this if necessary
	}
	
	@Override
	public final void message(XdmNode message, boolean terminate,
			SourceLocator sourceLocator) {
		XdmSequenceIterator messageIterator = message.axisIterator(Axis
				.DESCENDANT_OR_SELF, new QName(EpubXmlAccess.NOTA_NAMESPACE,
						"out"));
		XdmSequenceIterator idIterator = message.axisIterator(Axis
				.DESCENDANT_OR_SELF, new QName(EpubXmlAccess.NOTA_NAMESPACE,
						"systemid"));
		String systemId = "";
		if (idIterator.hasNext()) systemId = idIterator.next().getStringValue();
		while (messageIterator.hasNext()) getResultsView().writeResult(
				new DocumentPositionedInfo(
						terminate ? DocumentPositionedInfo.SEVERITY_FATAL :
						DocumentPositionedInfo.SEVERITY_INFO, messageIterator
						.next().getStringValue(), systemId));
		handleMessage(message, terminate, sourceLocator);
	}

	@Override
	public void warning(TransformerException exception)
			throws TransformerException {
	}
	
	public void writeToResultsView(String message) {
		getResultsView().writeResult(message);
	}
	
	public void writeToResultsView(String message, String systemId) {
		getResultsView().writeResult(message, systemId);
	}

}
