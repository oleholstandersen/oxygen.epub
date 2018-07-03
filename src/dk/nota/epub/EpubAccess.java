package dk.nota.epub;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.stream.Collectors;

import dk.nota.archive.ArchiveAccess;
import dk.nota.xml.DocumentResult;
import dk.nota.xml.XmlAccess;
import dk.nota.xml.XmlAccessProvider;
import net.sf.saxon.s9api.Axis;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmNodeKind;
import net.sf.saxon.s9api.XdmSequenceIterator;
import net.sf.saxon.s9api.XdmValue;
import net.sf.saxon.s9api.Xslt30Transformer;

/**
 * 
 * <p>EpubAccess is the main entry point for EPUB-related functionality. It is
 * instantiated by passing an EPUB file as a {@link java.net.URI}. EpubAccess
 * objects are more or less just collections of URIs referring to the various
 * components. The inner classes {@link dk.nota.epub.EpubAccess.ContentAccess}
 * and {@link dk.nota.epub.EpubAccess.NavigationAccess} allow access to OPF and
 * navigation documents, respectively.</p>
 * 
 */

public class EpubAccess {
	
	private ArchiveAccess epubArchiveAccess;
	private ContentAccess epubContentAccess;
	private URI epubArchiveUri;
	private XmlAccess xmlAccess;
	private URI navUri;
	private URI ncxUri;
	private URI opfUri;
	private String pid;
	
	/**
	 * Constructor. Based on the provided URI, associated URIs will be obtained
	 * from the relevant files.
	 * @param epubArchiveUri The archive location.
	 * @throws EpubException If for some reason we fail to erect the
	 * scaffolding around the main URI because of an IO or XML exception.
	 */
	
	public EpubAccess(URI epubArchiveUri) throws EpubException {
		this.epubArchiveUri = epubArchiveUri;
		epubArchiveAccess = new ArchiveAccess(epubArchiveUri);
		xmlAccess = XmlAccessProvider.getXmlAccess();
		try {
			setupUris();
		} catch (IOException | SaxonApiException e) {
			throw new EpubException("Unable to establish URIs for EPUB access",
					e);
		}
		epubContentAccess = new ContentAccess();
	}
	
	/**
	 * Create a copy of the file with the suffix .bak in the same directory.
	 * @return The backup File.
	 * @throws IOException If an error occurs during copying.
	 */
	
	public File backupArchive() throws IOException {
		Path archivePath = epubArchiveAccess.getArchivePath();
		return Files.copy(archivePath, archivePath.resolveSibling(archivePath
				.getFileName() + ".bak"), StandardCopyOption.REPLACE_EXISTING)
				.toFile();
	}
	
	private void setupUris() throws IOException, SaxonApiException {
		URI containerUri = epubArchiveAccess.makeArchiveBasedUri(
				"META-INF/container.xml");
		XdmNode metaDocument = xmlAccess.getDocument(containerUri);
		String opfReferenceXpath =
				"//info:rootfile[@media-type = 'application/oebps-package+xml']";
		XdmNode opfReferenceNode = xmlAccess.getFirstNodeByXpath(
				opfReferenceXpath, metaDocument);
		opfUri = epubArchiveAccess.makeArchiveBasedUri(opfReferenceNode
				.getAttributeValue(new QName("full-path")));
		XdmNode opfDocument = xmlAccess.getDocument(opfUri);
		XdmNode navReferenceNode = xmlAccess.getFirstNodeByXpath(
				"/opf:package/opf:manifest/opf:item[@properties eq 'nav']",
				opfDocument);
		XdmNode ncxReferenceNode = xmlAccess.getFirstNodeByXpath(
				"/opf:package/opf:manifest/opf:item"
				+ "[@media-type eq 'application/x-dtbncx+xml']",
				opfDocument);
		navUri = opfUri.resolve(navReferenceNode.getAttributeValue(new QName(
				"href")));
		ncxUri = opfUri.resolve(ncxReferenceNode.getAttributeValue(new QName(
				"href")));
		pid = xmlAccess.getFirstNodeByXpath(
				"/opf:package/opf:metadata/dc:identifier", opfDocument)
				.getStringValue();
	}
	
	/**
	 * Get the EPUB archive as an ArchiveAccess instance. This can then be used
	 * to manipulate the actual file.
	 * @return An ArchiveAccess instance of the archive.
	 */
	
	public ArchiveAccess getArchiveAccess() {
		return epubArchiveAccess;
	}
	
	/**
	 * Get the location of the archive used to construct this instance.
	 * @return The archive location.
	 */
	
	public URI getArchiveUri() {
		return epubArchiveUri;
	}
	
	/**
	 * Get a ContentAcess instance, allowing access to OPF content such as
	 * metadata and content references.
	 * @return A ContentAccess instance of the OPF.
	 */
	
	public ContentAccess getContentAccess() {
		return epubContentAccess;
	}
	
	/**
	 * Get the PID optained from the dc:identifier element in the OPF.
	 * @return The PID.
	 */
	
	public String getPid() {
		return pid;
	}
	
	/**
	 * <p>Set the PID of this instance.</p>
	 * <p><em>Note</em>: The PID in this regard is just the value of a field
	 * in this EpubAccess object. It is not written to any actual
	 * files(s) in the physical archive.</p>
	 */
	
	public void setPid(String pid) {
		this.pid = pid;
	}
	
	/**
	 * A convenience method for generating an absolute URI based on a
	 * relative path within the archive. This method is useful because the
	 * generic methods provided by the {@link java.net.URI} class are unable
	 * to handle zip-based URIs (zip:file://.../file.epub!/path/to/file).
	 * @param path An internal archive path with no leading slash.
	 * @return An absolute URI with an archive component.
	 */
	
	public URI makeOpfBasedUri(String path) {
		// Because URI.resolve() fails for URIs with the zip: protocol, we need
		// a convenience method for generating an absolute URI based on a
		// path relative to the OPF document
		String opfUriString = opfUri.toString();
		int lastDelimeterIndex = opfUriString.lastIndexOf('/') + 1;
		path = opfUriString.substring(0, lastDelimeterIndex) + path;
		return URI.create(path).normalize();
	}
	
	/**
	 * A convenience method for relativizing an absolute URI again the archive
	 * URI, resulting in a simple {@class java.lang.String} representation of
	 * just the internal path.
	 * @param uri Some absolute URI with an archive component corresponding to
	 * the archive URI.
	 * @return The internal archive path with no leading slash.
	 */
	
	public String relativizeUriToOpf(URI uri) {
		return uri.toString().substring(opfUri.toString().lastIndexOf('/') + 1);
	}
	
	/**
	 * 
	 * This class allows access to the contents of the OPF document.
	 *
	 */
	
	public class ContentAccess {
		
		private ContentAccess() {
		}
		
		/**
		 * 
		 * @param opfDocument
		 * @return A {@link java.util.LinkedHashMap} containing the document
		 * URIs as keys and their contents, in the form of
		 * {@link net.sf.saxon.s9api.XdmNode} instances, as values. The map
		 * is linked in order to preserve spine order.
		 * @throws EpubException 
		 */
		
		public LinkedHashMap<URI,XdmNode> getContentDocuments(
				XdmNode opfDocument) throws EpubException {
			// TODO: Refactor, create indepent methods for manifest and spine
			LinkedHashMap<URI,XdmNode> contentDocumentsMap =
					new LinkedHashMap<URI,XdmNode>();
			XdmNode manifest = (XdmNode)opfDocument.axisIterator(Axis
					.DESCENDANT, new QName(XmlAccess.NAMESPACE_OPF,
							"manifest")).next();
			XdmNode spine = (XdmNode)opfDocument.axisIterator(Axis.DESCENDANT,
					new QName(XmlAccess.NAMESPACE_OPF, "spine")).next();
			XdmSequenceIterator iterator = spine.axisIterator(Axis.CHILD,
					new QName(XmlAccess.NAMESPACE_OPF, "itemref"));
			while (iterator.hasNext()) {
				String id = ((XdmNode)iterator.next()).getAttributeValue(
						new QName("idref"));
				XdmNode item = getItemFromManifest(id, manifest);
				String ref = item.getAttributeValue(new QName("href"));
				URI uri = makeOpfBasedUri(ref);
				try {
					XdmNode document = xmlAccess.getDocument(uri);
					contentDocumentsMap.put(uri, document);
				} catch (SaxonApiException e) {
					throw new EpubException("Unable to get document " + ref, e);
				}
			}
			return contentDocumentsMap;
		}
		
		public Iterable<URI> getContentDocumentUris() throws EpubException {
			return getContentDocumentUris(getOpfDocument());
		}
		
		public Iterable<URI> getContentDocumentUris(XdmNode opfDocument)
				throws EpubException {
			XdmNode manifest = (XdmNode)opfDocument.axisIterator(Axis
					.DESCENDANT, new QName(XmlAccess.NAMESPACE_OPF,
							"manifest")).next();
			XdmNode spine = (XdmNode)opfDocument.axisIterator(Axis.DESCENDANT,
					new QName(XmlAccess.NAMESPACE_OPF, "spine")).next();
			LinkedList<URI> uris = new LinkedList<URI>();
			XdmSequenceIterator iterator = spine.axisIterator(Axis.CHILD,
					new QName(XmlAccess.NAMESPACE_OPF, "itemref"));
			while (iterator.hasNext()) {
				String id = ((XdmNode)iterator.next()).getAttributeValue(
						new QName("idref"));
				XdmNode item = getItemFromManifest(id, manifest);
				uris.add(makeOpfBasedUri(item.getAttributeValue(new QName(
								"href"))));
			}
			return uris;
		}
		
		private XdmNode getItemFromManifest(String itemId, XdmNode manifest)
				throws EpubException {
			XdmSequenceIterator iterator = manifest.axisIterator(Axis.CHILD, 
					new QName(XmlAccess.NAMESPACE_OPF, "item"));
			while (iterator.hasNext()) {
				XdmNode node = (XdmNode)iterator.next();
				if (node.getAttributeValue(new QName("id")).equals(itemId))
					return node;
			}
			return null;
		}
		
		public HashMap<String,String> getDublinCoreMetadata()
				throws EpubException {
			return getDublinCoreMetadata(getOpfDocument());
		}
		
		public HashMap<String,String> getDublinCoreMetadata(
				XdmNode opfDocument) {
			HashMap<String,String> metadataMap = new HashMap<String,String>();
			XdmNode metadata = (XdmNode)opfDocument.axisIterator(Axis
					.DESCENDANT, new QName(XmlAccess.NAMESPACE_OPF,
							"metadata")).next();
			metadata.axisIterator(Axis.CHILD).forEachRemaining(
					item -> {
						if (!(item instanceof XdmNode)) return;
						XdmNode node = (XdmNode)item;
						if (node.getNodeKind() != XdmNodeKind.ELEMENT) return;
						if (node.getNodeName().getNamespaceURI().equals(
								XmlAccess.NAMESPACE_DC))
							metadataMap.put(node.getNodeName().getLocalName()
									.toLowerCase(), node.getStringValue());
					});
			return metadataMap;
		}
		
		public XdmNode getOpfDocument() throws EpubException {
			try {
				return xmlAccess.getDocument(opfUri);
			} catch (SaxonApiException e) {
				throw new EpubException("Unable to get OPF document", e);
			}
		}
		
		public void updateOpf(Map<String,String> additions,
				Collection<String> removals, String idBase, boolean addToSpine)
				throws EpubException {
			Xslt30Transformer opfUpdater;
			try {
				opfUpdater = xmlAccess.getXsltTransformer(
								"/dk/nota/xml/xslt/epub-opf-update.xsl");
			} catch (SaxonApiException e) {
				throw new EpubException("Unable to load OPF updater", e);
			}
			HashMap<QName,XdmValue> parameters = new HashMap<QName,XdmValue>();
			parameters.put(new QName("ADDITION_REFS"), new XdmValue(
					additions.keySet().stream().map(XdmAtomicValue::new)
						.collect(Collectors.toList())));
			parameters.put(new QName("ADDITION_TYPES"), new XdmValue(
					additions.values().stream().map(XdmAtomicValue::new)
						.collect(Collectors.toList())));
			parameters.put(new QName("REMOVAL_REFS"), new XdmValue(
					removals.stream().map(XdmAtomicValue::new).collect(
							Collectors.toList())));
			parameters.put(new QName("ID_BASE"), new XdmAtomicValue(idBase));
			parameters.put(new QName("ADD_TO_SPINE"), new XdmAtomicValue(
					addToSpine));
			try {
				opfUpdater.setStylesheetParameters(parameters);
			} catch (SaxonApiException e) {
				throw new EpubException(
						"Unable to set parameters for OPF updater", e);
			}
			opfUpdater.setGlobalContextItem(getOpfDocument());
			DocumentResult documentResult;
			try {
				documentResult = new DocumentResult(opfUpdater.callTemplate(
						new QName("OPF")));
			} catch (SaxonApiException e) {
				throw new EpubException("Unable to transform OPF document", e);
			}
			try {
				documentResult.writeDocumentsToArchive(getArchiveAccess());
			} catch (IOException | SaxonApiException e) {
				throw new EpubException(
						"An error occurred while writing OPF document", e);
			}
		}
		
		public void updateOpfWithImages(Map<String,String> additions)
				throws EpubException {
			updateOpf(additions, new LinkedList<String>(), "image", false);
		}
		
	}
	
	public class NavigationAccess {
		
		private NavigationAccess() {
		}
		
		public XdmNode getXhtmlNavDocument() throws EpubException {
			try {
				return xmlAccess.getDocument(navUri);
			} catch (SaxonApiException e) {
				throw new EpubException(
						"Unable to get XHTML navigation document", e);
			}
		}
		
		public XdmNode getNcxNavDocument() throws EpubException {
			try {
				return xmlAccess.getDocument(ncxUri);
			} catch (SaxonApiException e) {
				throw new EpubException(
						"Unable to get NCX navigation document", e);
			}
		}
		
	}

}
