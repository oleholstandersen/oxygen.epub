package dk.nota.oxygen.xml;

import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.Configuration;
import net.sf.saxon.lib.FeatureKeys;
import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XPathCompiler;
import net.sf.saxon.s9api.XPathExecutable;
import net.sf.saxon.s9api.XPathSelector;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XsltCompiler;
import net.sf.saxon.s9api.XsltExecutable;
import net.sf.saxon.s9api.XsltTransformer;

public class XmlAccess {
	
	private Processor processor;
	private XPathCompiler xpathCompiler;
	private XsltCompiler xsltCompiler;
	
	public XmlAccess() {
		processor = new Processor(Configuration.makeLicensedConfiguration(
				getClass().getClassLoader(),
				"com.saxonica.config.EnterpriseConfiguration"));
		processor.setConfigurationProperty(FeatureKeys.LINE_NUMBERING, true);
		xpathCompiler = processor.newXPathCompiler();
		xsltCompiler = processor.newXsltCompiler();
	}
	
	public XdmNode getDocument(URL documentUrl) throws SaxonApiException {
		DocumentBuilder documentBuilder = getDocumentBuilder();
		return documentBuilder.build(getStreamSource(documentUrl));
	}
	
	public DocumentBuilder getDocumentBuilder() {
		return processor.newDocumentBuilder();
	}
	
	public XdmNode getFirstNodeByXpath(String xpath, XdmItem context)
			throws SaxonApiException {
		XPathSelector xpathSelector = getXpathSelector(xpath);
		xpathSelector.setContextItem(context);
		XdmItem item = xpathSelector.evaluateSingle();
		if (item instanceof XdmNode) return (XdmNode)item;
		return null;
	}
	
	public XsltTransformer getOutputTransformer() throws SaxonApiException {
		XsltTransformer outputTransformer = getXsltTransformer("output.xsl");
		outputTransformer.setDestination(getSerializer());
		return outputTransformer;
	}
	
	public Processor getProcessor() {
		return processor;
	}
	
	public Serializer getSerializer() {
		return processor.newSerializer();
	}
	
	public Serializer getSerializer(java.io.File file) {
		Serializer serializer = getSerializer();
		serializer.setOutputFile(file);
		return serializer;
	}
	
	public Serializer getSerializer(Writer writer) {
		Serializer serializer = getSerializer();
		serializer.setOutputWriter(writer);
		return serializer;
	}
	
	public StreamSource getStreamSource(URL documentUrl) {
		StreamSource source = new StreamSource(documentUrl.toString());
		source.setSystemId(documentUrl.toString());
		return source;
	}
	
	public XPathCompiler getXpathCompiler() {
		return xpathCompiler;
	}
	
	public XPathSelector getXpathSelector(String xpath)
			throws SaxonApiException {
		XPathExecutable xpathExecutable = xpathCompiler.compile(xpath);
		return xpathExecutable.load();
	}
	
	public StreamSource getXsltStreamSource(String xsltLocation) {
		StreamSource source;
		if (xsltLocation.contains(":")) {
			source = new StreamSource(xsltLocation);
			source.setSystemId(xsltLocation);
		} else {
			source = new StreamSource(getClass().getResourceAsStream(
					xsltLocation));
			source.setSystemId(getClass().getResource("/dk/nota/oxygen/xml/")
					.toString() + xsltLocation);
		}
		return source;
	}
	
	public XsltTransformer getXsltTransformer(Source xsltSource)
			throws SaxonApiException {
		xsltCompiler.setURIResolver((href, base) -> {
			try {
				URI uri = new URI(base.replaceFirst("/[^/]*?$", "/" + href))
						.normalize();
				return getXsltStreamSource(uri.toString());
			} catch (URISyntaxException e) {
				return null;
			}
		});
		XsltExecutable xsltExecutable = xsltCompiler.compile(xsltSource);
		return xsltExecutable.load();
	}
	
	public XsltTransformer getXsltTransformer(String xsltSourceFileName)
			throws SaxonApiException {
		return getXsltTransformer(getXsltStreamSource(xsltSourceFileName));
	}
	
	public static void main(String[] args) {
		XmlAccess test = new XmlAccess();
		System.out.println(test.getProcessor().getConfigurationProperty(
				FeatureKeys.LINE_NUMBERING));
	}
	
}
