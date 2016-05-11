package dk.nota.oxygen.xml;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.SourceLocator;
import javax.xml.transform.TransformerException;

import dk.nota.oxygen.common.ConsoleWindow;
import net.sf.saxon.expr.instruct.TerminationException;
import net.sf.saxon.s9api.Axis;
import net.sf.saxon.s9api.MessageListener;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmSequenceIterator;

public class ConsoleListener implements ErrorListener, MessageListener {

	private ConsoleWindow consoleWindow;
	
	public ConsoleListener(ConsoleWindow consoleWindow) {
		this.consoleWindow = consoleWindow;
	}
	
	@Override
	public void error(TransformerException exception)
			throws TransformerException {
	}
	
	@Override
	public void fatalError(TransformerException exception)
			throws TransformerException {
		if (exception instanceof TerminationException) return;
		writeToConsole("ERROR: " + exception);
	}
	
	public ConsoleWindow getConsoleWindow() {
		return consoleWindow;
	}
	
	public void handleMessage(XdmNode message, boolean terminate,
			SourceLocator sourceLocator) {
		// Override this if necessary
	}
	
	@Override
	public final void message(XdmNode message, boolean terminate,
			SourceLocator sourceLocator) {
		XdmSequenceIterator messageIterator = message.axisIterator(Axis
				.DESCENDANT_OR_SELF, new QName(XmlAccess.NOTA_NAMESPACE,
						"out"));
		while (messageIterator.hasNext()) writeToConsole(messageIterator.next()
				.getStringValue());
		handleMessage(message, terminate, sourceLocator);
	}

	@Override
	public void warning(TransformerException exception)
			throws TransformerException {
	}
	
	public void writeToConsole(String string) {
		getConsoleWindow().writeToConsole(string);
	}

}
