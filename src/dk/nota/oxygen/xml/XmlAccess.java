package dk.nota.oxygen.xml;

import java.io.File;
import java.net.URL;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

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

	public static final String DC_NAMESPACE = "http://purl.org/dc/elements/1.1/";
	public static final String DC_PREFIX = "dc";
	public static final String EPUB_NAMESPACE = "http://www.idpf.org/2007/ops";
	public static final String EPUB_PREFIX = "epub";
	public static final String INFO_NAMESPACE = "urn:oasis:names:tc:opendocument:xmlns:container";
	public static final String INFO_PREFIX = "info";
	public static final String NCX_NAMESPACE = "http://www.daisy.org/z3986/2005/ncx/";
	public static final String NCX_PREFIX = "ncx";
	public static final String NOTA_NAMESPACE = "http://www.nota.dk/oxygen";
	public static final String NOTA_PREFIX = "nota";
	public static final String OPF_NAMESPACE = "http://www.idpf.org/2007/opf";
	public static final String OPF_PREFIX = "opf";
	public static final String XHTML_NAMESPACE = "http://www.w3.org/1999/xhtml";
	public static final String XHTML_PREFIX = "xhtml"; 
	
	private Processor processor = new Processor(true);
	private XPathCompiler xpathCompiler;
	private XsltCompiler xsltCompiler;
	
	public XmlAccess() {
		processor.setConfigurationProperty(FeatureKeys.LINE_NUMBERING, true);
		xpathCompiler = processor.newXPathCompiler();
		xsltCompiler = processor.newXsltCompiler();
		setupXpathNamespaces();
	}
	
	public XdmNode getDocument(URL documentUrl) throws SaxonApiException {
		DocumentBuilder documentBuilder = processor.newDocumentBuilder();
		return documentBuilder.build(getStreamSource(documentUrl));
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
	
	public Serializer getSerializer(File file) {
		Serializer serializer = getSerializer();
		serializer.setOutputFile(file);
		return serializer;
	}
	
	public StreamSource getStreamSource(URL documentUrl) {
		StreamSource source = new StreamSource(documentUrl.toString());
		source.setSystemId(documentUrl.toString());
		return source;
	}
	
	public XPathSelector getXpathSelector(String xpath)
			throws SaxonApiException {
		XPathExecutable xpathExecutable = xpathCompiler.compile(xpath);
		return xpathExecutable.load();
	}
	
	public StreamSource getXsltStreamSource(String fileName) {
		StreamSource source = new StreamSource(getClass().getResourceAsStream(
				fileName));
		source.setSystemId(fileName);
		return source;
	}
	
	public XsltTransformer getXsltTransformer(Source xsltSource)
			throws SaxonApiException {
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
	
	private void setupXpathNamespaces() {
		xpathCompiler.declareNamespace(DC_PREFIX, DC_NAMESPACE);
		xpathCompiler.declareNamespace(EPUB_PREFIX, EPUB_NAMESPACE);
		xpathCompiler.declareNamespace(INFO_PREFIX, INFO_NAMESPACE);
		xpathCompiler.declareNamespace(NCX_PREFIX, NCX_NAMESPACE);
		xpathCompiler.declareNamespace(NOTA_PREFIX, NOTA_NAMESPACE);
		xpathCompiler.declareNamespace(OPF_PREFIX, OPF_NAMESPACE);
		xpathCompiler.declareNamespace(XHTML_PREFIX, XHTML_NAMESPACE);
	}
	
}
