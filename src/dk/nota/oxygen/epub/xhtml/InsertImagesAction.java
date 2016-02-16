package dk.nota.oxygen.epub.xhtml;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;

import dk.nota.oxygen.common.EditorAccess;
import dk.nota.oxygen.epub.common.EpubAccess;
import dk.nota.oxygen.epub.plugin.EpubPluginExtension;
import net.sf.saxon.s9api.SaxonApiException;
import ro.sync.ecss.extensions.api.AuthorDocumentController;
import ro.sync.ecss.extensions.api.AuthorOperationException;
import ro.sync.exml.editor.xmleditor.operations.context.RelativeInsertPosition;
import ro.sync.exml.workspace.api.editor.WSEditor;
import ro.sync.exml.workspace.api.editor.page.WSEditorPage;
import ro.sync.exml.workspace.api.editor.page.author.WSAuthorEditorPage;
import ro.sync.exml.workspace.api.editor.page.text.xml.TextDocumentController;
import ro.sync.exml.workspace.api.editor.page.text.xml.TextOperationException;
import ro.sync.exml.workspace.api.editor.page.text.xml.WSXMLTextEditorPage;
import uk.co.jaimon.test.SimpleImageInfo;

public class InsertImagesAction extends AbstractAction {
	
	public InsertImagesAction() {
		super("Insert images");
		putValue(Action.LARGE_ICON_KEY, new ImageIcon(getClass().getResource(
				"/images/Image20.png")));
		putValue(Action.SMALL_ICON, new ImageIcon(getClass().getResource(
				"/images/Image16.gif")));
	}

	@Override
	public void actionPerformed(ActionEvent actionEvent) {
		EditorAccess editorAccess = EpubPluginExtension.getEditorAccess();
		EpubAccess epubAccess = editorAccess.getEpubAccess();
		File[] imageFiles = editorAccess.getWorkspace().chooseFiles(null,
				"Insert", new String[] { "gif", "jpg", "jpeg", "png" },
				"Image files");
		if (imageFiles == null) return;
		HashMap<String,String> fileTypes = new HashMap<String,String>();
		try {
			for (File file : imageFiles) {
				fileTypes.put("images/" + file.getName(), Files
						.probeContentType(file.toPath()));
				epubAccess.copyFileToImageFolder(file);
			}
			epubAccess.addItemsToEpub(fileTypes, "image", false);
			String fragment = createFragment(imageFiles);
			insertFragment(fragment, editorAccess);
		} catch (AuthorOperationException | IOException | SaxonApiException |
				TextOperationException e) {
			editorAccess.showErrorMessage(e.toString());
		}
	}
	
	private String createFragment(File[] imageFiles) throws IOException {
		String fragment = "";
		String imgBase = "<img xmlns='http://www.w3.org/1999/xhtml' "
				+ "lang='da' xml:lang='da' alt='Illustration' "
				+ "src='%s' height='%s' width='%s'/>";
		String figureBase = "<figure xmlns='http://www.w3.org/1999/xhtml' "
				+ "class='image'>%s</figure>";
		String seriesBase = "<figure xmlns='http://www.w3.org/1999/xhtml' "
				+ "class='image-series'>%s</figure>";
		for (File imageFile : imageFiles) {
			SimpleImageInfo info = new SimpleImageInfo(imageFile);
			String imgFragment = String.format(imgBase, "images/" + imageFile
					.getName(), info.getHeight(), info.getWidth());
			String figureFragment = String.format(figureBase, imgFragment);
			fragment += figureFragment;
		}
		if (imageFiles.length > 1) return String.format(seriesBase, fragment);
		return fragment;
	}
	
	private void insertFragment(String fragment, EditorAccess editorAccess)
			throws AuthorOperationException, TextOperationException {
		WSEditor editor = editorAccess.getCurrentEditor();
		WSEditorPage editorPage = editor.getCurrentPage();
		if (editorPage instanceof WSAuthorEditorPage) {
			AuthorDocumentController documentController = ((WSAuthorEditorPage)
					editorPage).getAuthorAccess().getDocumentController();
			documentController.insertXMLFragment(fragment,
					((WSAuthorEditorPage)editorPage).getCaretOffset());
		} else if (editorPage instanceof WSXMLTextEditorPage) {
			TextDocumentController documentController = ((WSXMLTextEditorPage)
					editorPage).getDocumentController();
			documentController.insertXMLFragment(fragment, "self::node()",
					RelativeInsertPosition.INSERT_LOCATION_AFTER);
		}
	}

}
