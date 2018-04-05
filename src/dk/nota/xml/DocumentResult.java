package dk.nota.xml;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Set;

import dk.nota.archive.ArchiveAccess;
import dk.nota.xml.XmlAccessProvider;
import net.sf.saxon.s9api.Axis;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmValue;

public class DocumentResult {
	
	private LinkedHashMap<URI,XdmNode> resultMap =
			new LinkedHashMap<URI,XdmNode>();
	
	public DocumentResult(XdmValue transformationResult) {
		transformationResult.iterator().forEachRemaining(
				item -> {
					if (item instanceof XdmNode) {
						XdmNode documentNode = (XdmNode)item;
						URI uri = URI.create(documentNode.getAttributeValue(
								new QName("uri")));
						XdmNode content = (XdmNode)documentNode.axisIterator(
								Axis.CHILD).next();
						resultMap.put(uri, content);
					}
				});
	}
	
	public XdmNode getDocument(URI documentUri) {
		return resultMap.get(documentUri);
	}
	
	public Collection<XdmNode> getDocuments() {
		return resultMap.values();
	}
	
	public Set<URI> getUris() {
		return resultMap.keySet();
	}
	
	public void writeDocuments(Serializer serializer) throws IOException,
			SaxonApiException {
		for (URI uri : getUris()) {
			Path path = Paths.get(uri);
			Files.createDirectories(path.resolve("../"));
			try (OutputStream outputStream = Files.newOutputStream(path)) {
				serializer.setOutputStream(outputStream);
				serializer.serializeNode(getDocument(uri));
			}
		}
	}
	
	public void writeDocumentsToArchive(ArchiveAccess archiveAccess)
			throws IOException, SaxonApiException {
		try (FileSystem archiveFileSystem = archiveAccess
				.getArchiveAsFileSystem()) {
			writeDocumentsToArchive(archiveAccess, archiveFileSystem);
		}
	}
	
	public void writeDocumentsToArchive(ArchiveAccess archiveAccess,
			FileSystem archiveFileSystem) throws IOException, SaxonApiException {
		Serializer genericSerializer = XmlAccessProvider.getXmlAccess()
				.getSerializer();
		Serializer xhtmlSerializer = XmlAccessProvider.getXmlAccess()
				.getXhtmlSerializer();
		for (URI uri : getUris()) {
			archiveAccess.serializeNodeToArchive(getDocument(uri), uri,
					uri.toString().endsWith(".xhtml") ? xhtmlSerializer :
					genericSerializer, archiveFileSystem);
		}
	}

}
