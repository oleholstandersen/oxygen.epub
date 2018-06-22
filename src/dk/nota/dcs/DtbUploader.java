package dk.nota.dcs;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONException;
import org.json.JSONObject;

import dk.nota.oxygen.options.OptionsProvider;
import dk.nota.xml.XmlAccess;
import dk.nota.xml.XmlAccessProvider;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmNode;

public class DtbUploader {
	
	public static final String DCS_SERVER_DEFAULT =
			"http://http-dcsarchive.beta.dbb.dk";
	
	private long dcsId;
	private Path documentPath;
	private CloseableHttpClient httpClient = HttpClients.createDefault();
	private String pid;
	private Path uploadPath;
	private String server = OptionsProvider.getOptionValue(OptionsProvider
			.DCS_SERVER_OPTION);
	
	public DtbUploader(URI documentUri) throws SaxonApiException {
		documentPath = Paths.get(documentUri);
		pid = getPidFromDocument(documentUri);
		uploadPath = documentPath.getParent();
	}
	
	public void checkDirectory() throws DcsException, IOException {
		// Check if directory is located on N
		if (!uploadPath.getRoot().equals(Paths.get("N:/")))
			throw new DcsException(String.format("%s is not on N", uploadPath));
		// TODO: Consider not using the forEach() consumer, as it makes a mess
		// of exception handling
		Files.walk(uploadPath, 1).forEach(
				path -> {
					if (path == uploadPath) return;
					try {
						if (Files.isDirectory(path))
							throw new DcsException(String.format(
									"%s has a subdirectory (%s)",
									uploadPath, uploadPath.relativize(path)));
						String mimeType = Files.probeContentType(path);
						if (mimeType.equals("text/xml")) {
							if (!path.equals(documentPath))
								throw new DcsException(String.format(
										"Additional XML file encountered (%s)",
										path.getFileName()));
						} else if (!mimeType.startsWith("image/"))
							throw new DcsException(String.format(
									"%s has invalid MIME type (%s)",
									path.getFileName(), mimeType));
					} catch (DcsException | IOException e) {
						throw new RuntimeException(e);
					}
				});
	}
	
	public boolean createTitle() throws IOException, JSONException {
		HttpPost post = getHttpPost("/titles/createtitle");
		JSONObject json = new JSONObject();
		json.put("TitleNo",  pid);
		json.put("MaterialFormatCode", "DTB");
		json.put("MaterialTypeCode", "ETXT");
		json.put("MetadataFromDBBDokSys", false);
		json.put("OriginCode", "DDS");
		json.put("SourcePath", "\\\\dbb.dk\\networkdrive" + uploadPath
				.toString().substring(2));
		post.setEntity(new StringEntity(json.toString(),
				StandardCharsets.UTF_8));
		json = httpClient.execute(post, new JsonHttpResponseHandler());
		return json.getInt("Status") == 0;
	}
	
	public long getDcsId() {
		return dcsId;
	}
	
	private HttpPost getHttpPost(String endpoint) {
		HttpPost post = new HttpPost(server + endpoint);
		post.addHeader("Content-Type", "application/json");
		return post;
	}
	
	private String getPidFromDocument(URI documentUri)
			throws SaxonApiException {
		String pid;
		XmlAccess xmlAccess = XmlAccessProvider.getXmlAccess();
		XdmNode document = xmlAccess.getDocument(documentUri);
		pid = xmlAccess.getFirstNodeByXpath(
				"//meta[@name = ('dc:identifier','dc:Identifier')]", document)
				.getAttributeValue(new QName("content"))
				.replaceFirst("^(dk-nota-|DK-NOTA-)", "");
		return pid;
	}
	
	public void setDcsId() throws IOException, JSONException {
		HttpPost post = getHttpPost("/titles/gettitle");
		JSONObject json = new JSONObject();
		json.put("TitleNo", pid);
		post.setEntity(new StringEntity(json.toString(),
				StandardCharsets.UTF_8));
		json = httpClient.execute(post, new JsonHttpResponseHandler());
		if (json.get("Value") == JSONObject.NULL) dcsId = 0;
		else dcsId = json.getJSONObject("Value").getLong("ID");
	}
	
	public boolean updateTitle() throws IOException, JSONException {
		HttpPost post = getHttpPost("/titles/updatetitleformat");
		JSONObject json = new JSONObject();
		json.put("TitleId", dcsId);
		json.put("MaterialFormatCode", "DTB");
		json.put("SourcePath", "\\\\dbb.dk\\networkdrive" + uploadPath
				.toString().substring(2));
		post.setEntity(new StringEntity(json.toString(),
				StandardCharsets.UTF_8));
		json = httpClient.execute(post, new JsonHttpResponseHandler());
		return json.getInt("Status") == 0;
	}

}