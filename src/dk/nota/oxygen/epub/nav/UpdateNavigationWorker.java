package dk.nota.oxygen.epub.nav;

import javax.swing.SwingWorker;

import dk.nota.oxygen.common.ConsoleWindow;
import dk.nota.oxygen.epub.common.EpubAccess;
import dk.nota.oxygen.xml.ConsoleListener;
import net.sf.saxon.s9api.XsltTransformer;

public class UpdateNavigationWorker extends SwingWorker<Object,Object> {
	
	private EpubAccess epubAccess;
	private ConsoleListener messageListener;
	
	public UpdateNavigationWorker(EpubAccess epubAccess,
			ConsoleWindow consoleWindow) {
		this.epubAccess = epubAccess;
		this.messageListener = new ConsoleListener(consoleWindow);
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
		messageListener.writeToConsole("DONE");
	}

}
