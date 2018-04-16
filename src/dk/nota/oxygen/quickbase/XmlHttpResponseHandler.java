package dk.nota.oxygen.quickbase;

import java.io.IOException;

import javax.xml.transform.stream.StreamSource;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.impl.client.AbstractResponseHandler;

import dk.nota.xml.XmlAccess;
import dk.nota.xml.XmlAccessProvider;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmNode;

public class XmlHttpResponseHandler extends AbstractResponseHandler<XdmNode> {
	
	private XmlAccess xmlAccess = XmlAccessProvider.getXmlAccess();

	@Override
	public XdmNode handleEntity(HttpEntity entity) throws IOException {
		if (entity == null)
			throw new ClientProtocolException("HTTP response has no content");
		try {
			return xmlAccess.getDocumentBuilder().build(new StreamSource(entity
					.getContent()));
		} catch (UnsupportedOperationException e) {
			throw e;
		} catch (SaxonApiException e) {
			throw new IOException("S9API unable to generate result node", e);
		}
	}

}
