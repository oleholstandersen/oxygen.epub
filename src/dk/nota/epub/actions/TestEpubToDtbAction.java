package dk.nota.epub.actions;

import java.io.File;
import java.net.URL;
import java.util.LinkedList;

import dk.nota.epub.EpubException;
import dk.nota.epub.conversion.EpubToDtbWorker;
import dk.nota.oxygen.EditorAccess;
import dk.nota.oxygen.ResultsListener;
import dk.nota.oxygen.ResultsView;
import net.sf.saxon.s9api.XdmNode;

public class TestEpubToDtbAction extends EpubAction {

	public TestEpubToDtbAction() {
		super("DTBook 1.1.0");
	}

	@Override
	public void actionPerformed(EditorAccess editorAccess,
			LinkedList<URL> affectedEditorUrls) {
		XdmNode opfDocument;
		try {
			opfDocument = epubAccess.getContentAccess().getOpfDocument();
		} catch (EpubException e) {
			editorAccess.showErrorMessage("Unable to get OPF document", e);
			return;
		}
		String dtbFileName = epubAccess.getPid().replaceFirst("dk-nota-", "")
				+ ".xml";
		File dtbFile = null;
		dtbFile = editorAccess.getPluginWorkspace().chooseFile(new File(
				epubAccess.getArchiveUri().resolve(dtbFileName)),
				"Export [DTBook]", new String[] {"xml"}, "DTBook files", true);
		if (dtbFile == null) return;
		EpubToDtbWorker epubToDtbWorker = new EpubToDtbWorker(epubAccess,
				opfDocument, new ResultsListener(new ResultsView(epubAccess
						.getPid() + " - Convert to DTBook")), dtbFile.toURI());
		epubToDtbWorker.execute();
	}

}
