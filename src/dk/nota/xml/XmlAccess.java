package dk.nota.xml;

import java.net.URI;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XPathCompiler;
import net.sf.saxon.s9api.XPathExecutable;
import net.sf.saxon.s9api.XPathSelector;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.Xslt30Transformer;
import net.sf.saxon.s9api.XsltCompiler;
import net.sf.saxon.s9api.XsltExecutable;

public class XmlAccess {
	
	public static final String NAMESPACE_NOTA = "http://www.nota.dk/oxygen";
	public static final String PREFIX_NOTA = "nota";
	
	private Processor processor;
	private XPathCompiler xpathCompiler;
	private XsltCompiler xsltCompiler;
	
	public XmlAccess(Processor processor) {
		this.processor = processor;
		xpathCompiler = processor.newXPathCompiler();
		xsltCompiler = processor.newXsltCompiler();
		setupXpathNamespaces();
	}
	
	public XdmNode getDocument(URI documentUri) throws SaxonApiException {
		DocumentBuilder documentBuilder = getDocumentBuilder();
		return documentBuilder.build(getStreamSource(documentUri));
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
	
	public Processor getProcessor() {
		return processor;
	}
	
	public Serializer getSerializer() {
		Serializer serializer = processor.newSerializer();
		serializer.setOutputProperty(Serializer.Property.INDENT, "yes");
		serializer.setOutputProperty(Serializer.Property.METHOD, "xml");
		serializer.setOutputProperty(Serializer.Property.SAXON_INDENT_SPACES,
				"4");
		return serializer;
	}
	
	public StreamSource getStreamSource(URI documentUri) {
		StreamSource source = new StreamSource(documentUri.toString());
		source.setSystemId(documentUri.toString());
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
			source.setSystemId(xsltLocation);
		}
		return source;
	}
	
	public Xslt30Transformer getXsltTransformer(Source xsltSource)
			throws SaxonApiException {
		xsltCompiler.setURIResolver(
				(href, base) -> {
					String uriString = URI.create(base.replaceFirst(
							"/[^/]*?$", "/" + href)).normalize().toString();
					return getXsltStreamSource(uriString.substring(7));
				});
		XsltExecutable xsltExecutable = xsltCompiler.compile(xsltSource);
		return xsltExecutable.load30();
	}
	
	public Xslt30Transformer getXsltTransformer(String xsltSourceFileName)
			throws SaxonApiException {
		return getXsltTransformer(getXsltStreamSource(xsltSourceFileName));
	}
	
	private void setupXpathNamespaces() {
		xpathCompiler.declareNamespace(PREFIX_NOTA, NAMESPACE_NOTA);
	}
	
}