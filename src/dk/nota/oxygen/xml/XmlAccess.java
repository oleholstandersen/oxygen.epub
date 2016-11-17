package dk.nota.oxygen.xml;

import java.io.IOException;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import dk.nota.oxygen.common.ZipArchiveDetector;
import net.sf.saxon.Configuration;
import net.sf.saxon.lib.FeatureKeys;
import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.ExtensionFunction;
import net.sf.saxon.s9api.ItemType;
import net.sf.saxon.s9api.OccurrenceIndicator;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.SequenceType;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XPathCompiler;
import net.sf.saxon.s9api.XPathExecutable;
import net.sf.saxon.s9api.XPathSelector;
import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmValue;
import net.sf.saxon.s9api.XsltCompiler;
import net.sf.saxon.s9api.XsltExecutable;
import net.sf.saxon.s9api.XsltTransformer;
import uk.co.jaimon.test.SimpleImageInfo;

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
	
	private Processor processor;
	private XPathCompiler xpathCompiler;
	private XsltCompiler xsltCompiler;
	
	public XmlAccess() {
		processor = new Processor(Configuration.makeLicensedConfiguration(
				getClass().getClassLoader(),
				"com.saxonica.config.EnterpriseConfiguration"));
		processor.registerExtensionFunction(new ImageSizeExtensionFunction());
		processor.setConfigurationProperty(FeatureKeys.LINE_NUMBERING, true);
		xpathCompiler = processor.newXPathCompiler();
		xsltCompiler = processor.newXsltCompiler();
		setupXpathNamespaces();
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
		String xsltBase = xsltSource.getSystemId().replaceFirst("/[^/]+?$",
				"/");
		xsltCompiler.setURIResolver((href, base) -> getXsltStreamSource(
				xsltBase + href));
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
	
	public class ImageSizeExtensionFunction implements ExtensionFunction {

		@Override
		public XdmValue call(XdmValue[] arguments) throws SaxonApiException {
			try {
				// TrueZip doesn't handle the zip protocol in URIs, so use jar
				URI imageUri = new URI(arguments[0].toString().replaceFirst(
						"^zip:", "jar:"));
				// Make SimpleImageInfo work by creating a temporary "proper"
				// instance of java.io.File and copying the file which may be
				// zipped
				java.io.File tempImageFile = java.io.File.createTempFile("image",
						null);
				new ZipArchiveDetector().createFile(imageUri).archiveCopyTo(
						tempImageFile);
				SimpleImageInfo imageInfo = new SimpleImageInfo(tempImageFile);
				return new XdmValue(new XdmAtomicValue(imageInfo.getWidth()))
						.append(new XdmAtomicValue(imageInfo.getHeight()));
			} catch (IOException | URISyntaxException e) {
				return new XdmValue(new XdmAtomicValue(-1)).append(
						new XdmAtomicValue(-1));
			}
		}

		@Override
		public SequenceType[] getArgumentTypes() {
			return new SequenceType[] {
				SequenceType.makeSequenceType(ItemType.STRING,
						OccurrenceIndicator.ONE)
			};
		}
		
		@Override
		public QName getName() {
			return new QName(NOTA_PREFIX, NOTA_NAMESPACE, "get-image-size");
		}

		@Override
		public SequenceType getResultType() {
			return SequenceType.makeSequenceType(ItemType.INTEGER,
					OccurrenceIndicator.ONE_OR_MORE);
		}
		
	}
	
}
