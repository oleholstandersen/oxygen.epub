package dk.nota.oxygen.quickbase;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.swing.SwingWorker;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;

import dk.nota.oxygen.epub.plugin.EpubPluginExtension;

public class DownloadFromQuickbaseWorker extends SwingWorker<Path,Double> {
	
	private HttpEntity httpEntity;
	private Path outputPath;
	private String pid;
	
	public DownloadFromQuickbaseWorker(String pid, File outputFile) {
		outputPath = outputFile.toPath();
		this.pid = pid;
	}

	@Override
	protected Path doInBackground() throws Exception {
		QuickbaseAccess quickbaseAccess = EpubPluginExtension
				.getQuickbaseAccess();
		QuickbaseRecord record = quickbaseAccess.queryByPid(pid, 18);
		HttpGet get = new HttpGet(record.getEpubFileUrl() + "?ticket="
				+ quickbaseAccess.getTicket());
		HttpResponse response = quickbaseAccess.getHttpClient().execute(get);
		if (response.getEntity() == null)
			throw new ClientProtocolException("HTTP response is null");
		double fileSize = Double.parseDouble(response.getFirstHeader(
				"Content-Length").getValue());
		double createdFileSize = 0;
		try (
			InputStream inputStream = response.getEntity().getContent();
			OutputStream outputStream = Files.newOutputStream(outputPath);
		) {
			byte[] bytes = new byte[102400];
			int bytesRead = inputStream.read(bytes);
			while (bytesRead != -1) {
				outputStream.write(bytes, 0, bytesRead);
				bytesRead = inputStream.read(bytes);
				createdFileSize += bytesRead;
				setProgress((int)Math.ceil(
						(createdFileSize / fileSize) * 100));
			}
			return outputPath;
		}
	}
	
	private void releaseHttpResources() {
		EntityUtils.consumeQuietly(httpEntity);
	}
	
	public boolean stopDownload() throws IOException {
		releaseHttpResources();
		Files.deleteIfExists(outputPath);
		return cancel(false);
	}
	
	@Override
	protected void done() {
		releaseHttpResources();
	}

}
