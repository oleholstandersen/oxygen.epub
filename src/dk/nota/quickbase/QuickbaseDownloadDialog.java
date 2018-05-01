package dk.nota.quickbase;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutionException;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;

import ro.sync.exml.workspace.api.PluginWorkspaceProvider;
import ro.sync.exml.workspace.api.standalone.ui.Button;

public class QuickbaseDownloadDialog extends JDialog
		implements PropertyChangeListener {
	
	private Button cancelButton;
	private DownloadFromQuickbaseWorker downloadFromQuickbaseWorker;
	private Button openButton;
	private URL outputFileUrl;
	private String pid;
	private JProgressBar progressBar;
	
	public QuickbaseDownloadDialog(
			DownloadFromQuickbaseWorker downloadFromQuickbaseWorker,
			String pid, File outputFile) {
		super((JFrame)PluginWorkspaceProvider.getPluginWorkspace()
				.getParentFrame(), "Downloading " + pid, false);
		this.downloadFromQuickbaseWorker = downloadFromQuickbaseWorker;
		this.pid = pid;
		try {
			this.outputFileUrl = outputFile.toURI().toURL();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		setLocationRelativeTo(getParent());
		getContentPane().setLayout(new FlowLayout());
		add(createProgressBar());
		add(createCancelButton());
		add(createOpenButton());
		addWindowStateListener(
				event -> {
					if (event.getNewState() == WindowEvent.WINDOW_CLOSING)
						downloadFromQuickbaseWorker.cancel(true);
				});
		pack();
	}
	
	private Button createCancelButton() {
		cancelButton = new Button("Cancel");
		cancelButton.addActionListener(
				event -> {
					downloadFromQuickbaseWorker.cancel(true);
				});
		return cancelButton;
	}
	
	private Button createOpenButton() {
		openButton = new Button("Open");
		openButton.addActionListener(
				event -> {
					dispose();
					PluginWorkspaceProvider.getPluginWorkspace().open(
							outputFileUrl);
				});
		openButton.setEnabled(false);
		return openButton;
	}
	
	private JProgressBar createProgressBar() {
		progressBar = new JProgressBar();
		progressBar.setPreferredSize(new Dimension(300, 25));
		return progressBar;
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent event) {
		switch (event.getPropertyName()) {
		case "progress":
			progressBar.setValue((Integer)event.getNewValue());
			break;
		case "state":
			if (event.getNewValue() == SwingWorker.StateValue.DONE) {
				updateOrDispose();
			}
		}
			
	}
	
	private void updateOrDispose() {
		try {
			if (downloadFromQuickbaseWorker.get())
				updateButtons();
			else dispose();
		} catch (ExecutionException | InterruptedException e) {
			dispose();
			PluginWorkspaceProvider.getPluginWorkspace().showErrorMessage(
					String.format("Failed to download %s:\n%s", pid, e
							.getCause().toString()));
		}
	}
	
	public void updateButtons() {
		cancelButton.setEnabled(false);
		openButton.setEnabled(true);
	}

}
