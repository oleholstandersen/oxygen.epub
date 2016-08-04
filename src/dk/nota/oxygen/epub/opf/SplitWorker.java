package dk.nota.oxygen.epub.opf;

import dk.nota.oxygen.common.AbstractConsoleWorker;
import dk.nota.oxygen.common.ConsoleWindow;
import dk.nota.oxygen.epub.common.EpubAccess;
import dk.nota.oxygen.xml.ConsoleListener;
import net.sf.saxon.s9api.XsltTransformer;

public class SplitWorker extends AbstractConsoleWorker {
	
	private EpubAccess epubAccess;
	private ConsoleListener messageListener;
	private boolean success = false;
	
	public SplitWorker(EpubAccess epubAccess, ConsoleWindow consoleWindow) {
		super(consoleWindow);
		this.epubAccess = epubAccess;
		this.messageListener = new ConsoleListener(consoleWindow);
	}

	@Override
	protected Object doInBackground() throws Exception {
		XsltTransformer splitTransformer = epubAccess.getSplitTransformer(
				messageListener, messageListener);
		XsltTransformer outputTransformer = epubAccess.getOutputTransformer(
				messageListener, messageListener);
		splitTransformer.setDestination(outputTransformer);
		splitTransformer.transform();
		success = true;
		return null;
	}
	
	@Override
	protected void done() {
		if (success) messageListener.writeToConsole("SPLIT DONE");
	}

}