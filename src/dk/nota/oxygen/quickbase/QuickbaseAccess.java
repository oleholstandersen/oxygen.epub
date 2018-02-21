package dk.nota.oxygen.quickbase;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import dk.nota.oxygen.epub.plugin.EpubPluginExtension;
import net.sf.saxon.s9api.Axis;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmNode;
import ro.sync.exml.workspace.api.options.WSOptionsStorage;
import ro.sync.util.editorvars.EditorVariables;

public class QuickbaseAccess {
	
	protected static final String QB_MAIN_URL = EditorVariables
			.expandEditorVariables("${quickbaseMainURL}", null);
	protected static final String QB_TABLE_URL = EditorVariables
			.expandEditorVariables("${quickbaseTableURL}", null);
	protected static final String QB_APP_TOKEN = EditorVariables
			.expandEditorVariables("${quickbaseAppToken}", null);
	
	private boolean connected = false;
	private CloseableHttpClient httpClient = HttpClients.createDefault();
	private String ticket;
	private String userEmail;
	private String userId;
	private XmlHttpResponseHandler xmlResponseHandler =
			new XmlHttpResponseHandler();
	
	public QuickbaseAccess(WSOptionsStorage optionsStorage) {
		String userEmail = optionsStorage.getOption(EpubPluginExtension
				.QB_EMAIL_OPTION, "");
		String password = optionsStorage.getOption(EpubPluginExtension
				.QB_PASSWORD_OPTION, "");
		this.userEmail = userEmail;
		if (optionsStorage.getOption(EpubPluginExtension.QB_ENABLED_OPTION,
				"false").equals("false")) return;
		try {
			connect(userEmail, password);
		} catch (IOException | SaxonApiException e) {
			e.printStackTrace();
		}
	}
	
	public void connect(String userEmail, char[] password)
			throws IOException, SaxonApiException {
		connect(userEmail, String.valueOf(password));
	}
	
	public void connect(String userEmail, String password)
			throws IOException, SaxonApiException {
		String callBody = String.format(
				"<username>%s</username>"
				+ "<password>%s</password>"
				+ "<hours>10</hours>", userEmail, password);
		HttpPost post = getApiCallAsPost(QB_MAIN_URL, "API_Authenticate",
				callBody);
		XdmNode response = httpClient.execute(post, xmlResponseHandler);
		ticket = response.axisIterator(Axis.DESCENDANT, new QName("ticket"))
				.next().getStringValue();
		userId = response.axisIterator(Axis.DESCENDANT, new QName("userid"))
				.next().getStringValue();
		this.userEmail = userEmail;
		connected = true;
	}
	
	public void disconnect() {
		ticket = null;
		connected = false;
	}
	
	private HttpPost getApiCallAsPost(String destination, String callName,
			String callBody) {
		HttpPost post = new HttpPost(destination);
		post.addHeader("Content-Type", "application/xml;charset=UTF-8");
		post.addHeader("QUICKBASE-ACTION", callName);
		post.setEntity(new StringEntity(String.format("<qdbapi>%s</qdbapi>",
				callBody), StandardCharsets.UTF_8));
		return post;
	}
	
	public CloseableHttpClient getHttpClient() {
		return httpClient;
	}
	
	public int getRidFromPid(String pid) throws QuickbaseException {
		return queryForSingleRecord(String.format("{'14'.EX.'%s'}", pid), 3)
				.getRid();
	}
	
	public String getTicket() {
		return ticket;
	}
	
	public String getUserEmail() {
		return userEmail;
	}
	
	public String getUserId() {
		return userId;
	}
	
	public String getUserIdByEmail(String email) throws QuickbaseException {
		if (!isConnected())
			throw new QuickbaseException("Not connected to QuickBase");
		String userInfoCall = String.format(
				"<ticket>%s</ticket>"
				+ "<email>%s</email>", ticket, email);
		try {
			HttpPost post = getApiCallAsPost(QB_MAIN_URL, "API_GetUserInfo",
					userInfoCall);
			XdmNode response = httpClient.execute(post, xmlResponseHandler);
			return ((XdmNode)response.axisIterator(Axis.DESCENDANT, new QName(
					"user")).next()).getAttributeValue(new QName("id"));
		} catch (IOException e) {
			throw new QuickbaseException("Unable to retrieve user ID", e);
		}
		
	}
	
	public boolean isConnected() {
		return connected;
	}
	
	public Map<String,QuickbaseRecord> query(String query, int... fieldIds)
			throws QuickbaseException {
		return query(query, "", "", fieldIds);
	}
	
	public Map<String,QuickbaseRecord> query(String query, String sorting,
			String options, int... fieldIds) throws QuickbaseException {
		if (!isConnected())
			throw new QuickbaseException("Not connected to QuickBase");
		HashMap<String,QuickbaseRecord> records =
				new HashMap<String,QuickbaseRecord>();
		String queryCall = String.format(
				"<apptoken>%s</apptoken>"
				+ "<ticket>%s</ticket>"
				+ "<query>%s</query>"
				+ "<slist>%s</slist>"
				+ "<options>%s</options>"
				+ "<fmt>structured</fmt>"
				+ "<includeRids>1</includeRids>",
				QB_APP_TOKEN, getTicket(), query, sorting, options);
		if (fieldIds.length > 0) {
			// Always return field 14 (production ID)
			queryCall += "<clist>14.";
			for (int i = 0; i < fieldIds.length; i ++)
				// Skip field 14 if it has been specified
				queryCall += (fieldIds[i] == 14 ? "" : fieldIds[i])
					+ (i < fieldIds.length - 1 ? "." : "</clist>");
		} else queryCall += "<clist>a</clist>";
		HttpPost post = getApiCallAsPost(QB_TABLE_URL, "API_DoQuery",
				queryCall);
		try {
			XdmNode response = httpClient.execute(post, xmlResponseHandler);
			response.axisIterator(Axis.DESCENDANT, new QName("record"))
			.forEachRemaining(
					record -> {
						QuickbaseRecord quickbaseRecord = new QuickbaseRecord();
						quickbaseRecord.parseRecordNode((XdmNode)record);
						records.put(quickbaseRecord.getPid(), quickbaseRecord);
					});
		return records;
		} catch (IOException e) {
			throw new QuickbaseException(
					"Query failed due to IO or XML error", e);
		}
	}
	
	public QuickbaseRecord queryByPid(String pid, int... fieldIds)
			throws QuickbaseException {
		pid = pid.startsWith("dk-nota-") ? pid : "dk-nota-" + pid;
		return queryForSingleRecord(String.format("{'14'.EX.'%s'}", pid),
				fieldIds);
	}
	
	public QuickbaseRecord queryForSingleRecord(String query, int... fieldIds)
			throws QuickbaseException {
		Map<String,QuickbaseRecord> records = query(query, fieldIds);
		if (records.size() == 0) return null;
		return records.values().iterator().next();
	}
	
	public boolean updateFields(int rid, Map<Integer,String> updateMap)
			throws QuickbaseException {
		if (!isConnected())
			throw new QuickbaseException("Not connected to QuickBase");
		String updateCallBody = String.format(
				"<apptoken>%s</apptoken>"
				+ "<ticket>%s</ticket>"
				+ "<rid>%s</rid>",
				QB_APP_TOKEN, ticket, rid);
		for (int key : updateMap.keySet())
			updateCallBody += String.format("<field fid='%s'>%s</field>", key,
					updateMap.get(key));
		HttpPost post = getApiCallAsPost(QB_TABLE_URL, "API_EditRecord",
				updateCallBody);
		try {
			XdmNode response = getHttpClient().execute(post, xmlResponseHandler);
			int updatedCount = Integer.parseInt(((XdmNode)response.axisIterator(
					Axis.DESCENDANT, new QName("num_fields_changed")).next())
					.getStringValue());
			return (updateMap.keySet().size() == updatedCount);
		} catch (IOException e) {
			throw new QuickbaseException("Update failed due to IO or XML error", e);
		}
	}
	
	public boolean updateFields(String pid, Map<Integer,String> updateMap)
			throws QuickbaseException {
		pid = pid.startsWith("dk-nota-") ? pid : "dk-nota-" + pid;
		return updateFields(getRidFromPid(pid), updateMap);
	}

}
