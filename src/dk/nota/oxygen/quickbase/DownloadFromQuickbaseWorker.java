package dk.nota.oxygen.quickbase;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import javax.swing.SwingWorker;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;

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
		pid = outputFile.getName().replaceFirst("\\..*$", "");
	}

	@Override
	protected File doInBackground() throws Exception {
		try {
			QuickbaseAccess quickbaseAccess = EpubPluginExtension
					.getQuickbaseAccess();
			QuickbaseRecord quickbaseRecord = quickbaseAccess.queryByPid(pid,
					18);
			HttpGet get = new HttpGet(quickbaseRecord.getEpubFileUrl()
					+ "?ticket=" + quickbaseAccess.getTicket());
			HttpResponse response = quickbaseAccess.getHttpClient().execute(get);
			if (response.getEntity() == null)
				throw new ClientProtocolException("HTTP response is null");
			Files.copy(response.getEntity().getContent(), outputFile.toPath(),
					StandardCopyOption.REPLACE_EXISTING);
			EntityUtils.consume(response.getEntity());
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
