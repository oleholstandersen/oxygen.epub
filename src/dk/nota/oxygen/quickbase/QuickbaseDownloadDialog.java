package dk.nota.oxygen.quickbase;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;

import dk.nota.oxygen.common.EditorAccess;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;
import ro.sync.exml.workspace.api.standalone.ui.Button;

public class QuickbaseDownloadDialog extends JDialog
		implements PropertyChangeListener {
	
	private Button cancelButton;
	private DownloadFromQuickbaseWorker downloadFromQuickbaseWorker;
	private EditorAccess editorAccess;
	private Button openButton;
	private URL outputFileUrl;
	private JProgressBar progressBar;
	
	public QuickbaseDownloadDialog(
			DownloadFromQuickbaseWorker downloadFromQuickbaseWorker,
			EditorAccess editorAccess, String pid, File outputFile) {
		super((JFrame)PluginWorkspaceProvider.getPluginWorkspace()
				.getParentFrame(), "Downloading " + pid, true);
		this.downloadFromQuickbaseWorker = downloadFromQuickbaseWorker;
		this.editorAccess = editorAccess;
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
					if (event.getNewState() == WindowEvent.WINDOW_CLOSED) {
						try {
							downloadFromQuickbaseWorker.stopDownload();
						} catch (IOException e) {
							editorAccess.showErrorMessage(e.toString());
						}
					}
				});
		pack();
	}
	
	private Button createCancelButton() {
		cancelButton = new Button("Cancel");
		cancelButton.addActionListener(
				event -> {
					try {
						downloadFromQuickbaseWorker.stopDownload();
					} catch (IOException e) {
						editorAccess.showErrorMessage(e.toString());
					}
					dispose();
				});
		return cancelButton;
	}
	
	private Button createOpenButton() {
		openButton = new Button("Open");
		openButton.addActionListener(
				event -> {
					dispose();
					editorAccess.open(outputFileUrl);
				});
		openButton.setEnabled(false);
		return openButton;
	}
	
	private JProgressBar createProgressBar() {
		progressBar = new JProgressBar();
		progressBar.setPreferredSize(new Dimension(300, 25));
		progressBar.setIndeterminate(true);
		return progressBar;
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent event) {
		switch (event.getPropertyName()) {
		case "progress":
			progressBar.setValue((Integer)event.getNewValue());
			break;
		case "state":
			switch ((SwingWorker.StateValue)event.getNewValue()) {
			case STARTED:
				progressBar.setIndeterminate(false);
				break;
			case DONE:
				progressBar.setValue(100); // Just to be sure
				updateButtons();
			}
		}
			
	}
	
	public void updateButtons() {
		cancelButton.setEnabled(false);
		openButton.setEnabled(true);
	}

}
