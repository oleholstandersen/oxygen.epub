package dk.nota.epub.actions;

import java.io.File;
import java.net.URL;
import java.util.LinkedList;

import dk.nota.epub.EpubException;
import dk.nota.epub.conversion.DaisyComicWorker;
import dk.nota.oxygen.EditorAccess;
import dk.nota.oxygen.ResultsListener;
import dk.nota.oxygen.ResultsView;
import net.sf.saxon.s9api.XdmNode;

public class DaisyComicAction extends EpubAction {

	public DaisyComicAction() {
		super("DAISY comic", false);
	}

	@Override
	public void actionPerformed(EditorAccess editorAccess,
			LinkedList<URL> affectedEditorUrls) {
		File outputDirectory = editorAccess.getPluginWorkspace()
				.chooseDirectory();
		if (outputDirectory == null) return;
		XdmNode opfDocument;
		try {
			opfDocument = epubAccess.getContentAccess().getOpfDocument();
		} catch (EpubException e) {
			editorAccess.showErrorMessage("Unable to get OPF document", e);
			return;
		}
		DaisyComicWorker daisyComicWorker = new DaisyComicWorker(epubAccess,
				opfDocument, new ResultsListener(new ResultsView(epubAccess
				.getPid() + " - Create DAISY comic")), outputDirectory.toURI());
		daisyComicWorker.execute();
	}

}
