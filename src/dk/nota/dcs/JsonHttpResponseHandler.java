package dk.nota.dcs;

import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.impl.client.AbstractResponseHandler;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class JsonHttpResponseHandler extends AbstractResponseHandler<JSONObject> {

	@Override
	public JSONObject handleEntity(HttpEntity entity) throws IOException {
		if (entity == null)
			throw new ClientProtocolException("HTTP response has no content");
		JSONParser jsonParser = new JSONParser();
		try {
			JSONObject json = (JSONObject)jsonParser.parse(
					new InputStreamReader(entity.getContent()));
			return json;
		} catch (ParseException | UnsupportedOperationException e) {
			throw new IOException("Unable to parse JSON response", e);
		}
	}

}