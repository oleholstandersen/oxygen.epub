package dk.nota.oxygen.xml;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import dk.nota.oxygen.epub.common.EpubAccess;
import net.sf.saxon.s9api.ExtensionFunction;
import net.sf.saxon.s9api.ItemType;
import net.sf.saxon.s9api.OccurrenceIndicator;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.SequenceType;
import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.XdmValue;
import uk.co.jaimon.test.SimpleImageInfo;

public class EpubXmlAccess extends XmlAccess {
	
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
	
	private EpubAccess epubAccess;
	
	public EpubXmlAccess(EpubAccess epubAccess) {
		super();
		this.epubAccess = epubAccess;
		setupEpubXpathNamespaces();
		getProcessor().registerExtensionFunction(
				new ImageSizeExtensionFunction());
	}
	
	private void setupEpubXpathNamespaces() {
		getXpathCompiler().declareNamespace(DC_PREFIX, DC_NAMESPACE);
		getXpathCompiler().declareNamespace(EPUB_PREFIX, EPUB_NAMESPACE);
		getXpathCompiler().declareNamespace(INFO_PREFIX, INFO_NAMESPACE);
		getXpathCompiler().declareNamespace(NCX_PREFIX, NCX_NAMESPACE);
		getXpathCompiler().declareNamespace(NOTA_PREFIX, NOTA_NAMESPACE);
		getXpathCompiler().declareNamespace(OPF_PREFIX, OPF_NAMESPACE);
		getXpathCompiler().declareNamespace(XHTML_PREFIX, XHTML_NAMESPACE);
	}
	
	public class ImageSizeExtensionFunction implements ExtensionFunction {

		@Override
		public XdmValue call(XdmValue[] arguments) throws SaxonApiException {
			try {
				FileSystem epubFileSystem = epubAccess.getEpubAsFileSystem();
				SimpleImageInfo imageInfo = new SimpleImageInfo(Files
						.newInputStream(epubFileSystem.getPath(arguments[0]
								.toString().replaceFirst("^.*!/", ""))));
				epubFileSystem.close();
				return new XdmValue(new XdmAtomicValue(imageInfo
						.getWidth())).append(new XdmAtomicValue(imageInfo
								.getHeight()));
			} catch (IOException e) {
				System.err.print(String.format(
						"Image sizing failed due to %s: ", e.toString()));
				e.printStackTrace();
				return new XdmValue(new XdmAtomicValue(-1).append(
						new XdmAtomicValue(-1)));
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
