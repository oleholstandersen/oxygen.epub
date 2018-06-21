package dk.nota.dcs;

import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.impl.client.AbstractResponseHandler;
import org.json.JSONException;
import org.json.JSONObject;

public class JsonHttpResponseHandler extends AbstractResponseHandler<JSONObject> {

	@Override
	public JSONObject handleEntity(HttpEntity entity) throws IOException {
		if (entity == null)
			throw new ClientProtocolException("HTTP response has no content");
		String jsonString = IOUtils.toString(entity.getContent());
		try {
			JSONObject json = new JSONObject(jsonString);
			return json;
		} catch (JSONException e) {
			throw new IOException(e);
		}
	}

}