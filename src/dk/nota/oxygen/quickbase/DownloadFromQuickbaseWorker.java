package dk.nota.oxygen.quickbase;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import javax.swing.SwingWorker;

import org.apache.commons.httpclient.methods.GetMethod;

import dk.nota.oxygen.epub.plugin.EpubPluginExtension;
import net.sf.saxon.s9api.SaxonApiException;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;

public class DownloadFromQuickbaseWorker extends SwingWorker<File,File> {
	
	
	private boolean openAfterDownload;
	private File outputFile;
	private String pid;
	
	public DownloadFromQuickbaseWorker(File outputFile,
			boolean openAfterDownload) {
		this.openAfterDownload = openAfterDownload;
		this.outputFile = outputFile;
	}

	@Override
	protected File doInBackground() throws Exception {
		try {
			pid = outputFile.getName().replaceFirst("\\..*$", "");
			QuickbaseAccess quickbaseAccess = EpubPluginExtension
					.getQuickbaseAccess();
			QuickbaseRecord quickbaseRecord = quickbaseAccess.queryByPid(pid,
					18);
			GetMethod get = new GetMethod(quickbaseRecord.getEpubFileUrl()
					+ "?ticket=" + quickbaseAccess.getTicket());
			quickbaseAccess.getHttpClient().executeMethod(get);
			Files.copy(get.getResponseBodyAsStream(), outputFile.toPath(),
					StandardCopyOption.REPLACE_EXISTING);
			Files.copy(outputFile.toPath(), outputFile.toPath().resolveSibling(
					pid + ".epub.orig"), StandardCopyOption.REPLACE_EXISTING);
			return outputFile;
		} catch (IOException | SaxonApiException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	@Override
	protected void done() {
		try {
			if (openAfterDownload)
				PluginWorkspaceProvider.getPluginWorkspace().open(
						outputFile.toURI().toURL());
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}

}
