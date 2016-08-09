package dk.nota.oxygen.common;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JLabel;
import javax.swing.SwingWorker;
import javax.swing.WindowConstants;

import ro.sync.exml.workspace.api.standalone.ui.OKCancelDialog;

public abstract class AbstractConsoleWorker extends SwingWorker<Object,Object> {
	
	private ConsoleWindow consoleWindow;
	
	public AbstractConsoleWorker(ConsoleWindow consoleWindow) {
		consoleWindow.setDefaultCloseOperation(WindowConstants
				.DO_NOTHING_ON_CLOSE);
		consoleWindow.addWindowListener(new ConsoleWindowListener());
		this.consoleWindow = consoleWindow;
	}

	@Override
	protected abstract Object doInBackground() throws Exception;
	
	public ConsoleWindow getConsoleWindow() {
		return consoleWindow;
	}
	
	private class ConsoleWindowListener extends WindowAdapter {
		
		public void windowClosing(WindowEvent event) {
			if (!isDone()) {
				OKCancelDialog okCancelDialog = new OKCancelDialog(
						consoleWindow, "Warning", true);
				okCancelDialog.add(new JLabel("Process not done:"
						+ " close the window and end the process?"));
				okCancelDialog.pack();
				okCancelDialog.setLocation(consoleWindow.getWidth() / 2 -
						okCancelDialog.getWidth() / 2, consoleWindow.getHeight()
						/ 2 - okCancelDialog.getHeight() / 2);
				okCancelDialog.setVisible(true);
				int result = okCancelDialog.getResult();
				if (result == OKCancelDialog.RESULT_OK) {
					cancel(true);
					consoleWindow.dispose();
				}
			} else consoleWindow.dispose();
			
		}
		
	}

}
