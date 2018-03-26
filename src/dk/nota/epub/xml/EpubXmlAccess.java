package dk.nota.epub.xml;

import dk.nota.xml.XmlAccess;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.Serializer;

public class EpubXmlAccess extends XmlAccess {
	
	public static final String NAMESPACE_DC = "http://purl.org/dc/elements/1.1/";
	public static final String PREFIX_DC = "dc";
	public static final String NAMESPACE_EPUB = "http://www.idpf.org/2007/ops";
	public static final String PREFIX_EPUB = "epub";
	public static final String NAMESPACE_INFO = "urn:oasis:names:tc:opendocument:xmlns:container";
	public static final String PREFIX_INFO = "info";
	public static final String NAMESPACE_NCX = "http://www.daisy.org/z3986/2005/ncx/";
	public static final String PREFIX_NCX = "ncx";
	public static final String NAMESPACE_OPF = "http://www.idpf.org/2007/opf";
	public static final String PREFIX_OPF = "opf";
	public static final String NAMESPACE_HTML = "http://www.w3.org/1999/xhtml";
	public static final String PREFIX_HTML = "html";
	
	public EpubXmlAccess(Processor processor) {
		super(processor);
		setupEpubXpathNamespaces();
	}
	
	public Serializer getXhtmlSerializer() {
		Serializer serializer = super.getSerializer();
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
	
	private void setupEpubXpathNamespaces() {
		getXpathCompiler().declareNamespace(PREFIX_DC, NAMESPACE_DC);
		getXpathCompiler().declareNamespace(PREFIX_EPUB, NAMESPACE_EPUB);
		getXpathCompiler().declareNamespace(PREFIX_INFO, NAMESPACE_INFO);
		getXpathCompiler().declareNamespace(PREFIX_NCX, NAMESPACE_NCX);
		getXpathCompiler().declareNamespace(PREFIX_OPF, NAMESPACE_OPF);
		getXpathCompiler().declareNamespace(PREFIX_HTML, NAMESPACE_HTML);
	}

}
