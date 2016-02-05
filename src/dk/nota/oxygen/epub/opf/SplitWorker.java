package dk.nota.oxygen.epub.opf;

import javax.swing.SwingWorker;

import dk.nota.oxygen.epub.common.EpubAccess;
import dk.nota.oxygen.xml.ConsoleListener;
import net.sf.saxon.s9api.XsltTransformer;

public class SplitWorker extends SwingWorker<Object,Object> {
	
	private EpubAccess epubAccess;
	private ConsoleListener messageListener;
	
	public SplitWorker(EpubAccess epubAccess,
			ConsoleListener messageListener) {
		this.epubAccess = epubAccess;
		this.messageListener = messageListener;
	}

	@Override
	protected Object doInBackground() throws Exception {
		XsltTransformer splitTransformer = epubAccess.getSplitTransformer(
				messageListener, messageListener);
		XsltTransformer outputTransformer = epubAccess.getOutputTransformer(
				messageListener, messageListener);
		splitTransformer.setDestination(outputTransformer);
		splitTransformer.transform();
		return null;
	}
	
	@Override
	protected void done() {
		messageListener.getConsoleWindow().writeToConsole("DONE");
	}

}