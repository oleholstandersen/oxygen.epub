package dk.nota.oxygen.quickbase;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import net.sf.saxon.s9api.Axis;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XdmNode;
import ro.sync.exml.workspace.api.PluginWorkspace;
import ro.sync.exml.workspace.api.options.WSOptionsStorage;
import ro.sync.exml.workspace.api.util.UtilAccess;

public class QuickbaseAccess {
	
	public static final String QB_ENABLED_OPTION = "dk.nota.oxygen.quickbase.enabled";
	public static final String QB_EMAIL_OPTION = "dk.nota.oxygen.quickbase.userid";
	public static final String QB_PASSWORD_OPTION = "dk.nota.oxygen.quickbase.password";
	public static final String QB_TOKEN_OPTION = "dk.nota.oxygen.quickbase.token";
	public static final String QB_URL_MAIN_OPTION = "dk.nota.oxygen.quickbase.url.main";
	public static final String QB_URL_TABLE_OPTION = "dk.nota.oxygen.quickbase.url.table";
	
	private boolean connected = false;
	private CloseableHttpClient httpClient = HttpClients.createDefault();
	private HashSet<QuickbaseAccessListener> listeners =
			new HashSet<QuickbaseAccessListener>();
	private String mainUrl;
	private String tableUrl;
	private String ticket;
	private String token;
	private String userEmail;
	private String userId;
	private XmlHttpResponseHandler xmlResponseHandler =
			new XmlHttpResponseHandler();
	
	public QuickbaseAccess(PluginWorkspace pluginWorkspace) {
		WSOptionsStorage optionsStorage = pluginWorkspace.getOptionsStorage();
		UtilAccess utilAccess = pluginWorkspace.getUtilAccess();
		userEmail = optionsStorage.getOption(QB_EMAIL_OPTION, "");
		mainUrl = optionsStorage.getOption(QB_URL_MAIN_OPTION, "");
		tableUrl = optionsStorage.getOption(QB_URL_TABLE_OPTION, "");
		token = utilAccess.decrypt(optionsStorage.getOption(QB_TOKEN_OPTION,
				""));
		String password = utilAccess.decrypt(optionsStorage.getOption(
				QB_PASSWORD_OPTION, ""));
		try {
			connect(userEmail, password);
		} catch (QuickbaseException e) {
			pluginWorkspace.showErrorMessage(e.getMessage(), e);
		}
	}
	
	public void addListener(QuickbaseAccessListener listener) {
		listeners.add(listener);
	}
	
	public void connect(String userEmail, char[] password)
			throws QuickbaseException {
		connect(userEmail, String.valueOf(password));
	}
	
	public void connect(String userEmail, String password)
			throws QuickbaseException {
		String callBody = String.format(
				"<username>%s</username>"
				+ "<password>%s</password>"
				+ "<hours>10</hours>", userEmail, password);
		HttpPost post = getApiCallAsPost(mainUrl, "API_Authenticate",
				callBody);
		try {
			XdmNode response = httpClient.execute(post, xmlResponseHandler);
			ticket = response.axisIterator(Axis.DESCENDANT, new QName(
					"ticket")).next().getStringValue();
			if (ticket == null) {
				disconnect();
				throw new QuickbaseException("Connection failed: no ticket");
			}
			this.userEmail = userEmail;
			userId = response.axisIterator(Axis.DESCENDANT, new QName(
					"userid")).next().getStringValue();
			connected = true;
			for (QuickbaseAccessListener listener : listeners)
				listener.connected(this);
		} catch (IOException e) {
			disconnect();
			throw new QuickbaseException("Connection failed due to IO error", e);
		}
		
	}
	
	public void disconnect() {
		ticket = null;
		connected = false;
		for (QuickbaseAccessListener listener : listeners)
			listener.disconnected(this);
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
	
	public Set<QuickbaseAccessListener> getListeners() {
		return listeners;
	}
	
	public String getMainUrl() {
		return mainUrl;
	}
	
	public int getRidFromPid(String pid) throws QuickbaseException {
		return queryForSingleRecord(String.format("{'14'.EX.'%s'}", pid), 3)
				.getRid();
	}
	
	public String getTableUrl() {
		return tableUrl;
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
			HttpPost post = getApiCallAsPost(mainUrl, "API_GetUserInfo",
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
				token, getTicket(), query, sorting, options);
		if (fieldIds.length > 0) {
			// Always return field 14 (production ID)
			queryCall += "<clist>14.";
			for (int i = 0; i < fieldIds.length; i ++)
				// Skip field 14 if it has been specified
				queryCall += (fieldIds[i] == 14 ? "" : fieldIds[i])
					+ (i < fieldIds.length - 1 ? "." : "</clist>");
		} else queryCall += "<clist>a</clist>";
		HttpPost post = getApiCallAsPost(tableUrl, "API_DoQuery", queryCall);
		try {
			XdmNode response = httpClient.execute(post, xmlResponseHandler);
			response.axisIterator(Axis.DESCENDANT, new QName("record"))
				.forEachRemaining(
						record -> {
							QuickbaseRecord quickbaseRecord =
									new QuickbaseRecord();
							quickbaseRecord.parseRecordNode((XdmNode)record);
							records.put(quickbaseRecord.getPid(),
									quickbaseRecord);
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
				token, ticket, rid);
		for (int key : updateMap.keySet())
			updateCallBody += String.format("<field fid='%s'>%s</field>", key,
					updateMap.get(key));
		HttpPost post = getApiCallAsPost(tableUrl, "API_EditRecord",
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
