package dk.nota.epub.xml;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Set;

import dk.nota.archive.ArchiveAccess;
import net.sf.saxon.s9api.Axis;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmValue;

public class EpubDocumentMap {
	
	private LinkedHashMap<URI,XdmNode> resultMap =
			new LinkedHashMap<URI,XdmNode>();
	
	public EpubDocumentMap(XdmValue transformationResult) {
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
	
	public void writeDocumentsToArchive(ArchiveAccess archiveAccess)
			throws IOException, SaxonApiException {
		Serializer genericSerializer = EpubXmlAccessProvider.getEpubXmlAccess()
				.getSerializer();
		Serializer xhtmlSerializer = EpubXmlAccessProvider.getEpubXmlAccess()
				.getXhtmlSerializer();
		Serializer serializer;
		try (FileSystem archiveFileSystem = archiveAccess
				.getArchiveAsFileSystem()) {
			for (URI uri : getUris()) {
				Path path = archiveFileSystem.getPath(archiveAccess
						.relativizeUriToArchive(uri));
				serializer = path.endsWith(".xhtml") ? xhtmlSerializer :
					genericSerializer;
				try (OutputStream outputStream = Files.newOutputStream(path,
						StandardOpenOption.CREATE)) {
					
					serializer.setOutputStream(outputStream);
					serializer.serializeNode(getDocument(uri));
				}
			}
		} finally {
			genericSerializer.close();
			xhtmlSerializer.close();
		}
	}

}
