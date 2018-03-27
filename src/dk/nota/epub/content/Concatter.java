package dk.nota.epub.content;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.LinkedList;

import javax.xml.transform.SourceLocator;

import dk.nota.epub.xml.EpubXmlAccessProvider;
import dk.nota.oxygen.xml.EpubXmlAccess;
import dk.nota.xml.AbstractTransformationListener;
import net.sf.saxon.s9api.Axis;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmSequenceIterator;
import net.sf.saxon.s9api.XdmValue;

public class Concatter extends AbstractContentTransformation {
	
	private LinkedList<URI> images = new LinkedList<URI>();
	private LinkedList<URI> originalDocuments = new LinkedList<URI>();
	
	public Concatter(XdmNode opfDocument, boolean updateOpf)
			throws SaxonApiException {
		super(EpubXmlAccessProvider.getEpubXmlAccess().getXsltTransformer(
				"/dk/nota/xml/xslt/epub-concat.xsl"), opfDocument);
		addListener(new ConcatListener());
		addParameter("UPDATE_OPF", new XdmAtomicValue(updateOpf));
	}
	
	public Concatter(LinkedHashMap<URI,XdmNode> documentMap,
			XdmNode opfDocument, boolean updateOpf) throws SaxonApiException {
		this(opfDocument, updateOpf);
		addParameter("CONTENT_DOCUMENTS", new XdmValue(documentMap.values()));
	}
	
	public LinkedList<URI> getImages() {
		return images;
	}
	
	public LinkedList<URI> getOriginalDocuments() {
		return originalDocuments;
	}
	
	private class ConcatListener extends AbstractTransformationListener {
		
		private ConcatListener() {
		}
		
		@Override
		public void message(XdmNode message, boolean terminate,
				SourceLocator sourceLocator) {
			// Get values of <nota:document> elements passed by xsl:message
			XdmSequenceIterator messageIterator = message.axisIterator(Axis
					.DESCENDANT_OR_SELF, new QName(EpubXmlAccess.NOTA_NAMESPACE,
							"document"));
			messageIterator.forEachRemaining(
					i -> originalDocuments.add(URI.create(i.getStringValue())));
			messageIterator = message.axisIterator(Axis.DESCENDANT_OR_SELF,
					new QName(EpubXmlAccess.NOTA_NAMESPACE, "image"));
			messageIterator.forEachRemaining(
					i -> images.add(URI.create(i.getStringValue())));
		}
	}
	
}
