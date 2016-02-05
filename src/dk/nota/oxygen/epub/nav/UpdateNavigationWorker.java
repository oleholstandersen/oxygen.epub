package dk.nota.oxygen.epub.nav;

import javax.swing.SwingWorker;

import dk.nota.oxygen.epub.common.EpubAccess;
import dk.nota.oxygen.xml.ConsoleListener;
import net.sf.saxon.s9api.XsltTransformer;

public class UpdateNavigationWorker extends SwingWorker<Object,Object> {
	
	private EpubAccess epubAccess;
	private ConsoleListener messageListener;
	
	public UpdateNavigationWorker(EpubAccess epubAccess,
			ConsoleListener messageListener) {
		this.epubAccess = epubAccess;
		this.messageListener = messageListener;
	}

	@Override
	protected Object doInBackground() throws Exception {
		XsltTransformer navigationTransformer = epubAccess
				.getNavigationTransformer(messageListener, messageListener);
		XsltTransformer outputTransformer = epubAccess.getOutputTransformer(
				messageListener, messageListener);
		navigationTransformer.setDestination(outputTransformer);
		navigationTransformer.transform();
		return null;
	}
	
	@Override
	protected void done() {
		messageListener.getConsoleWindow().writeToConsole("DONE");
	}

}
