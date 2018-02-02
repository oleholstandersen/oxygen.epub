package dk.nota.oxygen.epub.opf;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import dk.nota.oxygen.common.EditorAccess;
import dk.nota.oxygen.common.ResultsView;
import dk.nota.oxygen.common.ResultsViewImageListener;
import dk.nota.oxygen.epub.common.ArchiveSensitiveAction;
import dk.nota.oxygen.epub.common.EpubAccess;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.XdmDestination;
import net.sf.saxon.s9api.XsltTransformer;

public class CreateDaisyComicAction extends ArchiveSensitiveAction {

	public CreateDaisyComicAction() {
		super("DAISY comic");
	}

	@Override
	public void actionPerformed(EditorAccess editorAccess) {
		try {
			EpubAccess epubAccess = editorAccess.getEpubAccess();
			File outputDir = editorAccess.getWorkspace()
					.chooseDirectory();
			if (outputDir == null) return;
			ResultsViewImageListener imageListener =
					new ResultsViewImageListener(new ResultsView(
							epubAccess.getPid() + " - Create DAISY comic"));
			XsltTransformer concatTransformer = epubAccess.getConcatTransformer(
					imageListener, imageListener);
			XsltTransformer daisyComicTransformer = epubAccess.getEpubXmlAccess()
					.getXsltTransformer("comic-pages-to-daisy.xsl");
			concatTransformer.setDestination(daisyComicTransformer);
			daisyComicTransformer.setDestination(new XdmDestination());
			daisyComicTransformer.setErrorListener(imageListener);
			daisyComicTransformer.setMessageListener(imageListener);
			daisyComicTransformer.setParameter(new QName("OUTPUT_FOLDER_URL"),
					new XdmAtomicValue(outputDir.toURI().toString()));
			concatTransformer.transform();
			FileSystem epubFileSystem = epubAccess.getEpubAsFileSystem();
			for (String imagePathString : imageListener.getImagePaths()) {
				Path imagePath = epubFileSystem.getPath("/EPUB/",
						imagePathString);
				Files.copy(imagePath, Paths.get(outputDir.toURI().resolve(
						imagePath.getFileName().toString())));
			}
			epubFileSystem.close();
		} catch (IOException | SaxonApiException e) {
			editorAccess.showErrorMessage(e.toString());
		}
	}

}
