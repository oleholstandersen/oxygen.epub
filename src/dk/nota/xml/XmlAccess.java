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
	
	public static final String NAMESPACE_DC = "http://purl.org/dc/elements/1.1/";
	public static final String PREFIX_DC = "dc";
	public static final String NAMESPACE_EPUB = "http://www.idpf.org/2007/ops";
	public static final String PREFIX_EPUB = "epub";
	public static final String NAMESPACE_INFO = "urn:oasis:names:tc:opendocument:xmlns:container";
	public static final String PREFIX_INFO = "info";
	public static final String NAMESPACE_NCX = "http://www.daisy.org/z3986/2005/ncx/";
	public static final String PREFIX_NCX = "ncx";
	public static final String NAMESPACE_NOTA = "http://www.nota.dk/oxygen";
	public static final String PREFIX_NOTA = "nota";
	public static final String NAMESPACE_OPF = "http://www.idpf.org/2007/opf";
	public static final String PREFIX_OPF = "opf";
	public static final String NAMESPACE_HTML = "http://www.w3.org/1999/xhtml";
	public static final String PREFIX_HTML = "html";
	
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
	
	public Serializer getDtbSerializer() {
		Serializer serializer = getSerializer();
		serializer.setOutputProperty(Serializer.Property
				.SAXON_SUPPRESS_INDENTATION, "dd dt hd levelhd li p td th");
		return serializer;
	}
	
	public Serializer getXhtmlSerializer() {
		Serializer serializer = getSerializer();
		serializer.setOutputProperty(Serializer.Property.DOCTYPE_PUBLIC,
				"html");
		// By default we suppress indentation within elements which may contain
		// text and other inline content
		serializer.setOutputProperty(Serializer.Property
				.SAXON_SUPPRESS_INDENTATION, String.format(
						"{%1$s}caption "
						+ "{%1$s}dd {%1$s}dt "
						+ "{%1$s}figcaption "
						+ "{%1$s}h1 {%1$s}h2 {%1$s}h3 {%1$s}h4 {%1$s}h5 {%1$s}h6 "
						+ "{%1$s}li "
						+ "{%1$s}p "
						+ "{%1$s}td {%1$s}th", NAMESPACE_HTML));
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
		xpathCompiler.declareNamespace(PREFIX_DC, NAMESPACE_DC);
		xpathCompiler.declareNamespace(PREFIX_EPUB, NAMESPACE_EPUB);
		xpathCompiler.declareNamespace(PREFIX_INFO, NAMESPACE_INFO);
		xpathCompiler.declareNamespace(PREFIX_NCX, NAMESPACE_NCX);
		xpathCompiler.declareNamespace(PREFIX_NOTA, NAMESPACE_NOTA);
		xpathCompiler.declareNamespace(PREFIX_OPF, NAMESPACE_OPF);
		xpathCompiler.declareNamespace(PREFIX_HTML, NAMESPACE_HTML);
	}
	
}