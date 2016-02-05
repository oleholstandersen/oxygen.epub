package dk.nota.oxygen.epub.common;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.LinkedList;
import java.util.Map;

import javax.xml.transform.ErrorListener;

import de.schlichtherle.io.File;
import dk.nota.oxygen.xml.XmlAccess;
import net.sf.saxon.s9api.MessageListener;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmValue;
import net.sf.saxon.s9api.XsltTransformer;
import ro.sync.exml.workspace.api.editor.WSEditor;

public class EpubAccess {
	
	private URL archiveContentUrl;
	private URL archiveFileUrl;
	private URL contentFolderUrl;
	private URL editorUrl;
	private URL opfUrl;
	private XmlAccess xmlAccess = new XmlAccess();
	
	public EpubAccess(WSEditor editor) throws IOException, SaxonApiException {
		determineUrls(editor.getEditorLocation());
	}
	
	public void addItemsToEpub(Map<String,String> fileTypes, String idBase,
			boolean addToSpine) throws SaxonApiException {
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
		Files.copy(getArchiveFile().toPath(), new File(archiveFileUrl.getPath()
				+ ".bak").toPath(), StandardCopyOption.REPLACE_EXISTING);
	}
	
	public void copyFileToArchive(java.io.File file, String relativeFolder) {
		File archiveFile = getArchiveFile();
		File zipFile = new File(archiveFile, relativeFolder + file.getName());
		zipFile.archiveCopyFrom(file);
	}
	
	public void copyFileToContentFolder(java.io.File file) {
		copyFileToArchive(file, "EPUB/");
	}
	
	public void copyFileToImageFolder(java.io.File file) {
		copyFileToArchive(file, "EPUB/images/");
	}
	
	private void determineUrls(URL editorUrl)
			throws IOException, SaxonApiException {
		this.editorUrl = editorUrl;
		archiveFileUrl = new URL(editorUrl.toString().replaceAll("(^zip:|!.+$)",
				""));
		archiveContentUrl = new URL(editorUrl.toString().replaceFirst("!/.+$",
				"!/"));
		XdmNode metaDocument = xmlAccess.getDocument(makeArchiveBasedUrl(
				"META-INF/container.xml"));
		String opfReferenceXpath =
				"//info:rootfile[@media-type = 'application/oebps-package+xml']";
		XdmNode opfReferenceNode = xmlAccess.getFirstNodeByXpath(metaDocument,
				opfReferenceXpath);
		opfUrl = new URL(archiveContentUrl, opfReferenceNode.getAttributeValue(
				new QName("full-path")));
		contentFolderUrl = new URL(opfUrl, "./");
	}
	
	public void doNavigationUpdate(MessageListener messageListener) {
		
	}
	
	public File getArchiveFile() {
		return new File(archiveFileUrl.getPath());
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
	
	public XsltTransformer getDocxImporter(java.io.File[] files, boolean split)
			throws SaxonApiException {
		XsltTransformer docxImporter = getEditorTransformer("docx-import.xsl");
		LinkedList<XdmItem> wordFolderUrls = new LinkedList<XdmItem>();
		for (java.io.File file : files) wordFolderUrls.add(new XdmAtomicValue(
				"zip:" + file.toURI().toString() + "!/word/"));
		docxImporter.setParameter(new QName("WORD_FOLDER_URLS"),
				new XdmValue(wordFolderUrls));
		docxImporter.setParameter(new QName("SPLIT_DOCUMENTS"),
				new XdmAtomicValue(split));
		return docxImporter;
	}
	
	public XsltTransformer getEditorTransformer(String xsltFileName)
			throws SaxonApiException {
		XsltTransformer editorTransformer = xmlAccess.getXsltTransformer(
				xsltFileName);
		editorTransformer.setSource(xmlAccess.getStreamSource(editorUrl));
		return editorTransformer;
	}
	
	public XsltTransformer getNavigationTransformer()
			throws SaxonApiException {
		return getOpfTransformer("navigation-update.xsl");
	}
	
	public XsltTransformer getNavigationTransformer(ErrorListener errorListener,
			MessageListener messageListener) throws SaxonApiException {
		return getOpfTransformer("navigation-update.xsl", errorListener,
				messageListener);
	}
	
	public XsltTransformer getOpfTransformer(String xsltFileName)
			throws SaxonApiException {
		XsltTransformer opfTransformer = xmlAccess.getXsltTransformer(
				xsltFileName);
		opfTransformer.setSource(xmlAccess.getStreamSource(opfUrl));
		opfTransformer.setParameter(new QName("CONTENT_FOLDER_URL"),
				new XdmAtomicValue(contentFolderUrl.toString()));
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
	
	public URL getOpfUrl() {
		return opfUrl;
	}
	
	public XsltTransformer getOutputTransformer() throws SaxonApiException {
		return xmlAccess.getOutputTransformer();
	}
	
	public XsltTransformer getOutputTransformer(ErrorListener errorListener,
			MessageListener messageListener) throws SaxonApiException {
		XsltTransformer outputTransformer = getOutputTransformer();
		outputTransformer.setErrorListener(errorListener);
		outputTransformer.setMessageListener(messageListener);
		return outputTransformer;
	}
	
	public XsltTransformer getSplitTransformer() throws SaxonApiException {
		return getOpfTransformer("split.xsl");
	}
	
	public XsltTransformer getSplitTransformer(ErrorListener errorListener,
			MessageListener messageListener) throws SaxonApiException {
		return getOpfTransformer("split.xsl", errorListener, messageListener);
	}
	
	public XmlAccess getXmlAccess() {
		return xmlAccess;
	}
	
	public URL makeArchiveBasedUrl(String relativePath)
			throws MalformedURLException {
		return new URL(archiveContentUrl, relativePath);
	}
	
}
