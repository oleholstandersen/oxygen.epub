package dk.nota.quickbase;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.swing.SwingWorker;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;

import dk.nota.oxygen.epub.plugin.EpubPluginExtension;

public class DownloadFromQuickbaseWorker extends SwingWorker<Boolean,Double> {
	
	private Path outputPath;
	private String pid;
	
	public DownloadFromQuickbaseWorker(String pid, File outputFile) {
		outputPath = outputFile.toPath();
		this.pid = pid;
	}

	@Override
	protected Boolean doInBackground() throws Exception {
		QuickbaseAccess quickbaseAccess = EpubPluginExtension
				.getQuickbaseAccess();
		QuickbaseRecord record = quickbaseAccess.queryByPid(pid, 18);
		if (record == null)
			throw new QuickbaseException("No QuickBase record for " + pid);
		HttpGet get = new HttpGet(record.getEpubFileUrl() + "?ticket="
				+ quickbaseAccess.getTicket());
		HttpResponse response = quickbaseAccess.getHttpClient().execute(get);
		if (response.getEntity() == null)
			throw new ClientProtocolException("HTTP response is null");
		double fileSize = Double.parseDouble(response.getFirstHeader(
				"Content-Length").getValue());
		double createdFileSize = 0;
		// Create a directory for the backup file
		Files.createDirectories(outputPath.resolveSibling("fraEkstern/"));
		// It's probably overkill, but timestamp the backup just in case
		String timeStamp = LocalDateTime.now().format(DateTimeFormatter
				.ofPattern("yyyyMMddHHmmss"));
		String backupFileName = outputPath.getFileName().toString()
				.replaceFirst("(\\..*?$)", ".original." + timeStamp + "$1");
		Path backupPath = Files.createFile(outputPath.resolveSibling(
				"fraEkstern/" + backupFileName));
		try (
			InputStream inputStream = response.getEntity().getContent();
			OutputStream outputStream = Files.newOutputStream(outputPath);
			OutputStream backupStream = Files.newOutputStream(backupPath)
		) {
			byte[] bytes = new byte[102400];
			int bytesRead = inputStream.read(bytes);
			while (bytesRead != -1) {
				outputStream.write(bytes, 0, bytesRead);
				backupStream.write(bytes, 0, bytesRead);
				bytesRead = inputStream.read(bytes);
				createdFileSize += bytesRead;
				setProgress((int)Math.ceil(
						(createdFileSize / fileSize) * 100));
			}
			return true;
		} finally {
			EntityUtils.consumeQuietly(response.getEntity());
		}
	}
	
	@Override
	protected void done() {
		try {
			if (isCancelled()) Files.delete(outputPath);
		} catch (IOException e) {
			System.err.print(String.format("Unable to delete %s: ",
					outputPath));
			e.printStackTrace();
		}
	}

}
