package dk.nota.oxygen.epub.xhtml;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.StringWriter;
import java.math.BigInteger;

import javax.swing.AbstractAction;
import javax.swing.text.BadLocationException;

import dk.nota.oxygen.common.EditorAccess;
import dk.nota.oxygen.epub.common.EpubAccess;
import dk.nota.oxygen.epub.plugin.EpubPluginExtension;
import dk.nota.oxygen.xml.XmlAccess;
import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XsltTransformer;
import ro.sync.ecss.extensions.api.AuthorOperationException;
import ro.sync.exml.workspace.api.editor.page.WSEditorPage;
import ro.sync.exml.workspace.api.editor.page.author.WSAuthorEditorPage;
import ro.sync.exml.workspace.api.editor.page.text.xml.WSXMLTextEditorPage;
import ro.sync.exml.workspace.api.editor.page.text.xml.XPathException;

public class ImportCatListAction extends AbstractAction {
	
	private String countXpath =
			"count(ancestor-or-self::body|ancestor-or-self::section)";
	private XmlAccess xmlAccess;
	
	public ImportCatListAction() {
		super("Import cat list");
	}
	
	@Override
	public void actionPerformed(ActionEvent event) {
		EditorAccess editorAccess = EpubPluginExtension.getEditorAccess();
		EpubAccess epubAccess = editorAccess.getEpubAccess();
		xmlAccess = epubAccess.getXmlAccess();
		File catListFile = editorAccess.getWorkspace().chooseFile(
				"Import cat list", new String[] { "kat" }, "Cat lists");
		try {
			WSEditorPage editorPage = editorAccess.getCurrentEditor()
					.getCurrentPage();
			if (editorPage instanceof WSAuthorEditorPage)
				insert(catListFile, (WSAuthorEditorPage)editorPage);
			else if (editorPage instanceof WSXMLTextEditorPage)
				insert(catListFile, (WSXMLTextEditorPage)editorPage);
		} catch (AuthorOperationException | BadLocationException |
				SaxonApiException e) {
			editorAccess.showErrorMessage(e.toString());
		}
	}
	
	private int getDepth(WSAuthorEditorPage authorPage) {
		try {
			// Evaluating count() returns a BigInteger (xs:integer)
			BigInteger xpathResult = (BigInteger)authorPage.getAuthorAccess()
					.getDocumentController().evaluateXPath(countXpath, true,
							true, true)[0];
			return xpathResult.intValue();
		} catch (AuthorOperationException e) {
			return 2; // Make 2 the default depth
		}
	}
	
	private int getDepth(WSXMLTextEditorPage textPage) {
		try {
			// Evaluating count() returns a BigInteger (xs:integer)
			BigInteger xpathResult = (BigInteger)textPage.evaluateXPath(
					countXpath)[0];
			return xpathResult.intValue();
		} catch (XPathException e) {
			return 2; // Make 2 the default depth
		}
	}
	
	private String getFragment(File catListFile, int depth)
			throws SaxonApiException {
		StringWriter resultWriter = new StringWriter();
		Serializer resultSerializer = xmlAccess.getSerializer(resultWriter);
		resultSerializer.setOutputProperty(Serializer.Property
				.OMIT_XML_DECLARATION, "yes");
		DocumentBuilder documentBuilder = xmlAccess.getDocumentBuilder();
		documentBuilder.setBaseURI(catListFile.toURI());
		XdmNode documentNode = documentBuilder.build(catListFile);
		XsltTransformer catListTransformer = xmlAccess.getXsltTransformer(
				"import-cat-list.xsl");
		catListTransformer.setDestination(resultSerializer);
		catListTransformer.setInitialContextNode(documentNode);
		catListTransformer.setParameter(new QName("DEPTH"), new XdmAtomicValue(
				++depth));
		catListTransformer.transform();
		return resultWriter.toString();
	}
	
	private void insert(File catListFile, WSAuthorEditorPage authorPage)
			throws AuthorOperationException, SaxonApiException {
		String fragment = getFragment(catListFile, getDepth(authorPage));
		authorPage.getAuthorAccess().getDocumentController().insertXMLFragment(
				fragment, authorPage.getCaretOffset());
	}
	
	private void insert(File catListFile, WSXMLTextEditorPage textPage)
			throws BadLocationException, SaxonApiException {
		String fragment = getFragment(catListFile, getDepth(textPage));
		textPage.getDocument().insertString(textPage.getCaretOffset(), fragment
				.replaceAll("\\s+xmlns=\".+?\"", ""), null);
	}

}
