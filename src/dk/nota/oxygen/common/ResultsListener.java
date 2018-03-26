package dk.nota.oxygen.common;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.SourceLocator;
import javax.xml.transform.TransformerException;

import dk.nota.oxygen.xml.EpubXmlAccess;
import dk.nota.xml.TransformationListener;
import net.sf.saxon.s9api.Axis;
import net.sf.saxon.s9api.MessageListener;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmSequenceIterator;
import ro.sync.document.DocumentPositionedInfo;

public class ResultsListener implements ErrorListener, MessageListener,
		PropertyChangeListener, TransformationListener {
	
	public final static String UPDATE_RESULTS_PROPERTY =
			"dk.nota.oxygen.results.update";
	
	private ResultsView resultsView;
	
	public ResultsListener(ResultsView resultsView) {
		this.resultsView = resultsView;
	}
	
	@Override
	public void error(TransformerException exception)
			throws TransformerException {
		writeException(exception, DocumentPositionedInfo.SEVERITY_ERROR);
	}

	@Override
	public void fatalError(TransformerException exception)
			throws TransformerException {
		writeException(exception, DocumentPositionedInfo.SEVERITY_FATAL);
	}
	
	public ResultsView getResultsView() {
		return resultsView;
	}
	
	protected void handleMessage(XdmNode message, boolean terminate,
			SourceLocator sourceLocator) {
		// Subclasses can override this method if they need to do more with the
		// message received by message()
	}
	
	protected void handlePropertyChange(PropertyChangeEvent event) {
		
	}

	@Override
	public final void message(XdmNode message, boolean terminate,
			SourceLocator sourceLocator) {
		// Messages meant for display are text-node children of <nota:out>
		// elements within <xsl:message>
		XdmSequenceIterator messageIterator = message.axisIterator(Axis
				.DESCENDANT_OR_SELF, new QName(EpubXmlAccess.NOTA_NAMESPACE,
						"out"));
		// Stylesheets may or may not include the system ID of input documents
		// in a <nota:systemid> element
		XdmSequenceIterator idIterator = message.axisIterator(Axis
				.DESCENDANT_OR_SELF, new QName(EpubXmlAccess.NOTA_NAMESPACE,
						"systemid"));
		String systemId = "";
		if (idIterator.hasNext()) systemId = idIterator.next().getStringValue();
		while (messageIterator.hasNext())
			resultsView.writeResult(new DocumentPositionedInfo(
						terminate ? DocumentPositionedInfo.SEVERITY_FATAL :
						DocumentPositionedInfo.SEVERITY_INFO, messageIterator
						.next().getStringValue(), systemId));
		handleMessage(message, terminate, sourceLocator);
	}

	@Override
	public void propertyChange(PropertyChangeEvent event) {
		// Print the NEW values of relevant properties
		if (event.getPropertyName().equals(UPDATE_RESULTS_PROPERTY))
			resultsView.writeResult(event.getNewValue().toString());
		handlePropertyChange(event);
	}

	@Override
	public void warning(TransformerException exception)
			throws TransformerException {
		// XSLT warnings are probably not of any interest to end users, hence
		// we write them to System.err
		exception.printStackTrace();
	}
	
	public void writeException(Throwable throwable, int severity) {
		resultsView.writeResult(new DocumentPositionedInfo(severity,
				throwable.toString()));
	}
	
	public void writeException(TransformerException exception,
			int severity) {
		if (exception.getLocator() != null) {
			String systemId = exception.getLocator().getSystemId();
			int line = exception.getLocator().getLineNumber();
			int column = exception.getLocator().getColumnNumber();
			resultsView.writeResult(new DocumentPositionedInfo(severity,
					exception.toString(), systemId, line, column));
		} else resultsView.writeResult(new DocumentPositionedInfo(severity,
				exception.toString()));
	}

}
