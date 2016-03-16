package dk.nota.oxygen.epub.opf;

import javax.swing.SwingWorker;

import dk.nota.oxygen.common.ConsoleWindow;
import dk.nota.oxygen.epub.common.EpubAccess;
import dk.nota.oxygen.xml.ConsoleListener;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.XsltTransformer;

public class ConcatWorker extends SwingWorker<Object,Object> {
	
	private EpubAccess epubAccess;
	private ConsoleListener messageListener;
	
	public ConcatWorker(EpubAccess epubAccess, ConsoleWindow consoleWindow) {
		this.epubAccess = epubAccess;
		this.messageListener = new ConsoleListener(consoleWindow);
	}

	@Override
	protected Object doInBackground() throws Exception {
		XsltTransformer concatTransformer = epubAccess.getConcatTransformer(
				messageListener, messageListener);
		concatTransformer.setParameter(new QName("UPDATE_OPF"),
				new XdmAtomicValue(true));
		XsltTransformer outputTransformer = epubAccess.getOutputTransformer(
				messageListener, messageListener);
		concatTransformer.setDestination(outputTransformer);
		concatTransformer.transform();
		return null;
	}
	
	@Override
	protected void done() {
		messageListener.writeToConsole("DONE");
	}

}
