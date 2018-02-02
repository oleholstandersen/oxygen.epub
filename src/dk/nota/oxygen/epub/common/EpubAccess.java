package dk.nota.oxygen.epub.common;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import javax.xml.transform.ErrorListener;

import de.schlichtherle.io.File;
import dk.nota.oxygen.common.ZipArchiveDetector;
import dk.nota.oxygen.xml.EpubXmlAccess;
import net.sf.saxon.s9api.MessageListener;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmValue;
import net.sf.saxon.s9api.XsltTransformer;

public class EpubAccess {
	
	private URL archiveContentUrl;
	private URL archiveFileUrl;
	private URL contentFolderUrl;
	private URL editorUrl;
	private URL navigationUrl;
	private URL opfUrl;
	private String pid;
	private EpubXmlAccess epubXmlAccess;
	
	public EpubAccess(URL editorUrl) throws IOException, SaxonApiException {
		epubXmlAccess = new EpubXmlAccess(this);
		determineUrls(editorUrl);
	}
	
	public void addItemReferencesToOpf(Map<String,String> fileTypes,
			String idBase, boolean addToSpine) throws SaxonApiException {
		XsltTransformer opfUpdater = getOpfTransformer("opf-update.xsl");
		LinkedList<XdmItem> hrefs = new LinkedList<XdmItem>();
		LinkedList<XdmItem> types = new LinkedList<XdmItem>();
		for (String fileName : fileTypes.keySet()) {
			hrefs.add(new XdmAtomicValue(fileName));
			types.add(new XdmAtomicValue(fileTypes.get(fileName)));
		}
		opfUpdater.setParameter(new QName("HREFS"), new XdmValue(hrefs));
		opfUpdater.setParameter(new QName("TYPES"), new XdmValue(types));
		opfUpdater.setParameter(new QName("ID_BASE"), new XdmAtomicValue(
				idBase));
		opfUpdater.setParameter(new QName("ADD_TO_SPINE"), new XdmAtomicValue(
				addToSpine));
		opfUpdater.setDestination(getOutputTransformer());
		opfUpdater.transform();
	}
	
	public void backupArchive() throws IOException {
		File archiveFile = getArchiveFile();
		archiveFile.copyTo(new File(archiveFile.getPath() + ".bak"));
	}
	
	public boolean copyFileToArchive(java.io.File file, String relativePath) {
		File zipFile = new ZipArchiveDetector().createFile(
				getArchiveFileUrl().getPath() + "/" + relativePath);
		return zipFile.archiveCopyFrom(file);
	}
	
	public boolean copyFileToImageFolder(java.io.File file) {
		return copyFileToArchive(file, "EPUB/images/" + file.getName());
	}
	
	private void determineUrls(URL editorUrl)
			throws IOException, SaxonApiException {
		this.editorUrl = editorUrl;
		archiveFileUrl = new URL(editorUrl.toString().replaceAll(
				"(^zip:|!.+$)", ""));
		archiveContentUrl = new URL(editorUrl.toString().replaceFirst("!/.+$",
				"!/"));
		XdmNode metaDocument = getEpubXmlAccess().getDocument(makeArchiveBasedUrl(
				"META-INF/container.xml"));
		String opfReferenceXpath =
				"//info:rootfile[@media-type = 'application/oebps-package+xml']";
		XdmNode opfReferenceNode = getEpubXmlAccess().getFirstNodeByXpath(
				opfReferenceXpath, metaDocument);
		opfUrl = new URL(getArchiveContentUrl(), opfReferenceNode
				.getAttributeValue(new QName("full-path")));
		contentFolderUrl = new URL(opfUrl, "./");
		XdmNode opfDocument = getOpfDocument();
		XdmNode navReferenceNode = getEpubXmlAccess().getFirstNodeByXpath(
				"/opf:package/opf:manifest/opf:item[@properties eq 'nav']",
				opfDocument);
		navigationUrl = new URL(contentFolderUrl, navReferenceNode
				.getAttributeValue(new QName("href")));
		pid = getEpubXmlAccess().getFirstNodeByXpath(
				"/opf:package/opf:metadata/dc:identifier", opfDocument)
				.getStringValue();
	}
	
	public URL getArchiveContentUrl() {
		return archiveContentUrl;
	}
	
	public File getArchiveFile() throws IOException {
		try {
			return new File(getArchiveFileUrl().toURI());
		} catch (URISyntaxException e) {
			throw new IOException(e.toString());
		}
	}
	
	public URL getArchiveFileUrl() {
		return archiveFileUrl;
	}
	
	public URL getArchiveUrlInternal() {
		return archiveContentUrl;
	}
	
	public XsltTransformer getConcatTransformer() throws SaxonApiException {
		return getOpfTransformer("concat.xsl");
	}
	
	public XsltTransformer getConcatTransformer(ErrorListener errorListener,
			MessageListener messageListener) throws SaxonApiException {
		return getOpfTransformer("concat.xsl", errorListener, messageListener);
	}
	
	public URL getContentFolderUrl() {
		return contentFolderUrl;
	}
	
	public XsltTransformer getDtbConverter() throws SaxonApiException {
		XsltTransformer dtbConverter = getEpubXmlAccess().getXsltTransformer(
				"xhtml-to-dtb.xsl");
		dtbConverter.setParameter(new QName("OPF_DOCUMENT"), getOpfDocument());
		return dtbConverter;
	}
	
	public XsltTransformer getDtbConverter(ErrorListener errorListener,
			MessageListener messageListener) throws SaxonApiException {
		XsltTransformer dtbConverter = getDtbConverter();
		dtbConverter.setErrorListener(errorListener);
		dtbConverter.setMessageListener(messageListener);
		return dtbConverter;
	}
	
	public XsltTransformer getEditorTransformer(String xsltFileName)
			throws SaxonApiException {
		XsltTransformer editorTransformer = getEpubXmlAccess().getXsltTransformer(
				xsltFileName);
		editorTransformer.setSource(getEpubXmlAccess().getStreamSource(
				getEditorUrl()));
		return editorTransformer;
	}
	
	public URL getEditorUrl() {
		return editorUrl;
	}
	
	public FileSystem getEpubAsFileSystem() throws IOException {
		Map<String, String> environment = new HashMap<String,String>();
        environment.put("create", "true");
        return FileSystems.newFileSystem(URI.create("jar:"
				+ getArchiveFileUrl()), environment);
	}
	
	public EpubXmlAccess getEpubXmlAccess() {
		return epubXmlAccess;
	}
	
	public File getFileFromContentFolder(String filePath) throws IOException {
		try {
			return new ZipArchiveDetector().createFile(getArchiveFileUrl()
						.toURI().getPath() + "/EPUB/" + filePath);
		} catch (URISyntaxException e) {
			throw new IOException(e.getMessage());
		}
	}
	
	public XdmNode getNavigationDocument() throws SaxonApiException {
		return getEpubXmlAccess().getDocument(navigationUrl);
	}
	
	public XsltTransformer getNavigationTransformer()
			throws SaxonApiException {
		return getOpfTransformer("navigation-update.xsl");
	}
	
	public XsltTransformer getNavUpdateTransformer(ErrorListener errorListener,
			MessageListener messageListener) throws SaxonApiException {
		return getOpfTransformer("navigation-update.xsl", errorListener,
				messageListener);
	}
	
	public URL getNavigationUrl() {
		return navigationUrl;
	}
	
	public XsltTransformer getOpfTransformer(String xsltFileName)
			throws SaxonApiException {
		XsltTransformer opfTransformer = getEpubXmlAccess().getXsltTransformer(
				xsltFileName);
		opfTransformer.setSource(getEpubXmlAccess().getStreamSource(opfUrl));
		opfTransformer.setParameter(new QName("CONTENT_FOLDER_URL"),
				new XdmAtomicValue(getContentFolderUrl().toString()));
		return opfTransformer;
	}
	
	public XsltTransformer getOpfTransformer(String xsltFileName,
			ErrorListener errorListener, MessageListener messageListener)
					throws SaxonApiException {
		XsltTransformer opfTransformer = getOpfTransformer(xsltFileName);
		opfTransformer.setErrorListener(errorListener);
		opfTransformer.setMessageListener(messageListener);
		return opfTransformer;
	}
	
	public XdmNode getOpfDocument() throws SaxonApiException {
		return getEpubXmlAccess().getDocument(getOpfUrl());
	}
	
	public URL getOpfUrl() {
		return opfUrl;
	}
	
	public XsltTransformer getOutputTransformer() throws SaxonApiException {
		return getEpubXmlAccess().getOutputTransformer();
	}
	
	public XsltTransformer getOutputTransformer(ErrorListener errorListener,
			MessageListener messageListener) throws SaxonApiException {
		XsltTransformer outputTransformer = getOutputTransformer();
		outputTransformer.setErrorListener(errorListener);
		outputTransformer.setMessageListener(messageListener);
		return outputTransformer;
	}
	
	public String getPid() {
		return pid;
	}
	
	public XsltTransformer getSplitTransformer() throws SaxonApiException {
		return getOpfTransformer("split.xsl");
	}
	
	public XsltTransformer getSplitTransformer(ErrorListener errorListener,
			MessageListener messageListener) throws SaxonApiException {
		return getOpfTransformer("split.xsl", errorListener, messageListener);
	}
	
	public void reloadContentDocuments() throws IOException, SaxonApiException {
		LinkedHashMap<String,String> fileTypes =
				new LinkedHashMap<String,String>();
		FileSystem epubFileSystem = getEpubAsFileSystem();
		Files.walk(epubFileSystem.getPath("/EPUB"), 1)
			.sorted()
			.forEach(path -> {
					if (!Files.isDirectory(path) && path.getFileName()
							.toString().startsWith("dk-nota-")) {
						fileTypes.put(path.getFileName().toString(),
								"application/xhtml+xml");
					}
				});
		addItemReferencesToOpf(fileTypes, "document", true);
		epubFileSystem.close();
	}
	
	public URL makeArchiveBasedUrl(String relativePath)
			throws MalformedURLException {
		return new URL(getArchiveContentUrl(), relativePath);
	}
	
}
