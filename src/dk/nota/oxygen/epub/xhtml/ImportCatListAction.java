package dk.nota.oxygen.epub.xhtml;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.StringWriter;

import javax.swing.AbstractAction;
import javax.swing.text.BadLocationException;

import dk.nota.oxygen.common.EditorAccess;
import dk.nota.oxygen.epub.common.EpubAccess;
import dk.nota.oxygen.epub.plugin.EpubPluginExtension;
import dk.nota.oxygen.xml.XmlAccess;
import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XsltTransformer;
import ro.sync.ecss.extensions.api.AuthorOperationException;
import ro.sync.exml.workspace.api.editor.page.WSEditorPage;
import ro.sync.exml.workspace.api.editor.page.author.WSAuthorEditorPage;
import ro.sync.exml.workspace.api.editor.page.text.WSTextEditorPage;

public class ImportCatListAction extends AbstractAction {
	
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
			String fragment = getFragment(catListFile);
			WSEditorPage editorPage = editorAccess.getCurrentEditor()
					.getCurrentPage();
			if (editorPage instanceof WSAuthorEditorPage)
				insertInAuthor(fragment, (WSAuthorEditorPage)editorPage);
			else if (editorPage instanceof WSTextEditorPage)
				insertInText(fragment.replaceAll("\\s+xmlns=\".+?\"", ""),
						(WSTextEditorPage)editorPage);
		} catch (AuthorOperationException | BadLocationException |
				SaxonApiException e) {
			editorAccess.showErrorMessage(e.toString());
		}
	}
	
	private String getFragment(File catListFile) throws SaxonApiException {
		StringWriter resultWriter = new StringWriter();
		Serializer resultSerializer = xmlAccess.getSerializer(resultWriter);
		resultSerializer.setOutputProperty(Serializer.Property
				.OMIT_XML_DECLARATION, "yes");
		DocumentBuilder documentBuilder = xmlAccess.getDocumentBuilder();
		XsltTransformer catListTransformer = xmlAccess.getXsltTransformer(
				"import-cat-list.xsl");
		catListTransformer.setDestination(resultSerializer);
		documentBuilder.setBaseURI(catListFile.toURI());
		XdmNode documentNode = documentBuilder.build(catListFile);
		catListTransformer.setInitialContextNode(documentNode);
		catListTransformer.transform();
		return resultWriter.toString();
	}
	
	private void insertInAuthor(String fragment, WSAuthorEditorPage page)
			throws AuthorOperationException {
		page.getAuthorAccess().getDocumentController().insertXMLFragment(
				fragment, page.getCaretOffset());
	}
	
	private void insertInText(String fragment, WSTextEditorPage page)
			throws BadLocationException {
		page.getDocument().insertString(page.getCaretOffset(), fragment, null);
	}

}
