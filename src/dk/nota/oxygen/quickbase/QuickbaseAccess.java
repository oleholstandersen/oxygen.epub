package dk.nota.oxygen.quickbase;

import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.transform.stream.StreamSource;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;

import dk.nota.oxygen.epub.plugin.EpubPluginExtension;
import dk.nota.oxygen.xml.XmlAccess;
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
	
	private HttpClient httpClient = new HttpClient();
	private String ticket;
	private String userEmail;
	private String userId;
	
	public QuickbaseAccess(WSOptionsStorage optionsStorage) {
		String userEmail = optionsStorage.getOption(EpubPluginExtension
				.QB_EMAIL_OPTION, "");
		String password = optionsStorage.getOption(EpubPluginExtension
				.QB_PASSWORD_OPTION, "");
		this.userEmail = userEmail;
		if (optionsStorage.getOption(EpubPluginExtension.QB_ENABLED_OPTION,
				"false").equals("false")) return;
		try {
			authenticate(userEmail, password);
		} catch (IOException | SaxonApiException e) {
			e.printStackTrace();
		}
	}
	
	public void authenticate(String userEmail, char[] password)
			throws HttpException, IOException, SaxonApiException {
		authenticate(userEmail, String.valueOf(password));
	}
	
	public void authenticate(String userEmail, String password)
			throws HttpException, IOException, SaxonApiException {
		String callBody = String.format(
				"<username>%s</username>"
				+ "<password>%s</password>"
				+ "<hours>10</hours>", userEmail, password);
		PostMethod post = getApiCallAsPost(QB_MAIN_URL, "API_Authenticate",
				callBody);
		getHttpClient().executeMethod(post);
		XdmNode response = getResponseAsNode(post.getResponseBodyAsString());
		ticket = response.axisIterator(Axis.DESCENDANT, new QName("ticket"))
				.next().getStringValue();
		userId = response.axisIterator(Axis.DESCENDANT, new QName("userid"))
				.next().getStringValue();
		this.userEmail = userEmail;
		post.releaseConnection();
	}
	
	private PostMethod getApiCallAsPost(String destination, String callName,
			String callBody) throws UnsupportedEncodingException {
		PostMethod post = new PostMethod(destination);
		post.addRequestHeader("Content-Type", "application/xml");
		post.addRequestHeader("QUICKBASE-ACTION", callName);
		post.setRequestEntity(new StringRequestEntity(String.format(
				"<qdbapi>%s</qdbapi>", callBody), "application/xml", "UTF-8"));
		return post;
	}
	
	public HttpClient getHttpClient() {
		return httpClient;
	}
	
	public int getRidFromPid(String pid) throws HttpException, IOException,
			SaxonApiException {
		return queryForSingleRecord(String.format("{'14'.EX.'%s'}", pid), 3)
				.getRid();
	}
	
	public XdmNode getResponseAsNode(String response) throws SaxonApiException {
		XmlAccess xmlAccess = new XmlAccess();
		return xmlAccess.getDocumentBuilder().build(new StreamSource(
				new StringReader(response)));
	}
	
	public String getTicket() {
		return ticket;
	}
	
	public String getUserEmail() {
		return userEmail;
	}
	
	public String getUserId() throws HttpException, IOException,
			SaxonApiException {
		return userId;
	}
	
	public String getUserIdByEmail(String email) throws HttpException,
			IOException, SaxonApiException {
		String userInfoCall = String.format(
				"<ticket>%s</ticket>"
				+ "<email>%s</email>", ticket, email);
		PostMethod post = getApiCallAsPost(QB_MAIN_URL, "API_GetUserInfo",
				userInfoCall);
		getHttpClient().executeMethod(post);
		XdmNode response = getResponseAsNode(post.getResponseBodyAsString());
		return ((XdmNode)response.axisIterator(Axis.DESCENDANT, new QName(
				"user")).next()).getAttributeValue(new QName("id"));
	}
	
	public Map<String,QuickbaseRecord> query(String query, int... fieldIds)
			throws HttpException, IOException, SaxonApiException {
		return query(query, "", "", fieldIds);
	}
	
	public Map<String,QuickbaseRecord> query(String query, String sorting,
			String options, int... fieldIds) throws HttpException, IOException,
			SaxonApiException {
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
		PostMethod post = getApiCallAsPost(QB_TABLE_URL, "API_DoQuery",
				queryCall);
		getHttpClient().executeMethod(post);
		System.out.println(post.getResponseBodyAsString());
		XdmNode response = getResponseAsNode(post.getResponseBodyAsString());
		response.axisIterator(Axis.DESCENDANT, new QName("record"))
			.forEachRemaining(
					record -> {
						QuickbaseRecord quickbaseRecord = new QuickbaseRecord();
						quickbaseRecord.parseRecordNode((XdmNode)record);
						records.put(quickbaseRecord.getPid(), quickbaseRecord);
					});
		post.releaseConnection();
		return records;
	}
	
	public QuickbaseRecord queryByPid(String pid, int... fieldIds)
			throws HttpException, IOException, SaxonApiException {
		pid = pid.startsWith("dk-nota-") ? pid : "dk-nota-" + pid;
		return queryForSingleRecord(String.format("{'14'.EX.'%s'}", pid), fieldIds);
	}
	
	public QuickbaseRecord queryForSingleRecord(String query, int... fieldIds)
			throws HttpException, IOException, SaxonApiException {
		return query(query, fieldIds).values().iterator().next();
	}
	
	public boolean updateFields(int rid, Map<Integer,String> updateMap)
			throws HttpException, IOException {
		String updateCallBody = String.format(
				"<apptoken>%s</apptoken>"
				+ "<ticket>%s</ticket>"
				+ "<rid>%s</rid>",
				QB_APP_TOKEN, ticket, rid);
		for (int key : updateMap.keySet())
			updateCallBody += String.format("<field fid='%s'>%s</field>", key,
					updateMap.get(key));
		PostMethod post = getApiCallAsPost(QB_TABLE_URL, "API_EditRecord",
				updateCallBody);
		getHttpClient().executeMethod(post);
		post.releaseConnection();
		return true;
	}
	
	public boolean updateFields(String pid, Map<Integer,String> updateMap)
			throws HttpException, IOException, SaxonApiException {
		pid = pid.startsWith("dk-nota-") ? pid : "dk-nota-" + pid;
		return updateFields(getRidFromPid(pid), updateMap);
	}

}
