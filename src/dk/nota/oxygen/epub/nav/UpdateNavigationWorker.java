package dk.nota.oxygen.epub.nav;

import dk.nota.oxygen.common.AbstractConsoleWorker;
import dk.nota.oxygen.common.ConsoleWindow;
import dk.nota.oxygen.epub.common.EpubAccess;
import dk.nota.oxygen.xml.ConsoleListener;
import net.sf.saxon.s9api.XsltTransformer;

public class UpdateNavigationWorker extends AbstractConsoleWorker {
	
	private EpubAccess epubAccess;
	private ConsoleListener messageListener;
	private boolean success = false;
	
	public UpdateNavigationWorker(EpubAccess epubAccess,
			ConsoleWindow consoleWindow) {
		super(consoleWindow);
		this.epubAccess = epubAccess;
		this.messageListener = new ConsoleListener(consoleWindow);
	}

	@Override
	protected Object doInBackground() throws Exception {
		XsltTransformer navigationTransformer = epubAccess
				.getNavUpdateTransformer(messageListener, messageListener);
		XsltTransformer outputTransformer = epubAccess.getOutputTransformer(
				messageListener, messageListener);
		navigationTransformer.setDestination(outputTransformer);
		navigationTransformer.transform();
		success = true;
		return null;
	}
	
	@Override
	protected void done() {
		if (success) messageListener.writeToConsole("NAVIGATION UPDATE DONE");
	}

}
