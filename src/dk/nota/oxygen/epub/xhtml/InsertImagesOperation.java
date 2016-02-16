package dk.nota.oxygen.epub.xhtml;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;

import dk.nota.oxygen.common.EditorAccess;
import dk.nota.oxygen.epub.common.EpubAccess;
import dk.nota.oxygen.epub.plugin.EpubPluginExtension;
import net.sf.saxon.s9api.SaxonApiException;
import ro.sync.ecss.extensions.api.ArgumentDescriptor;
import ro.sync.ecss.extensions.api.ArgumentsMap;
import ro.sync.ecss.extensions.api.AuthorOperationException;
import uk.co.jaimon.test.SimpleImageInfo;

public class InsertImagesOperation extends XhtmlEpubAuthorOperation {
	
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

	@Override
	protected void doOperation() throws AuthorOperationException {
		EditorAccess editorAccess = EpubPluginExtension.getEditorAccess();
		EpubAccess epubAccess = editorAccess.getEpubAccess();
		File[] imageFiles = getWorkspace().chooseFiles(null,
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
			getDocumentController().insertXMLFragment(fragment,
					getSelectionStart());
		} catch (IOException | SaxonApiException e) {
			throw new AuthorOperationException(e.toString());
		}
	}

	@Override
	public ArgumentDescriptor[] getArguments() {
		return null; // No arguments to return
	}

	@Override
	public String getDescription() {
		return "Inserts images along with the required figure elements";
	}

	@Override
	protected void parseArguments(ArgumentsMap arguments)
			throws IllegalArgumentException {
		// No arguments to parse
	}
	
	
	
}
