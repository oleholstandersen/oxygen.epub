package dk.nota.oxygen.epub.actions;

import java.net.URI;
import java.net.URL;
import java.util.LinkedList;

import dk.nota.dtb.conversion.InspirationOutput;
import dk.nota.epub.EpubException;
import dk.nota.epub.conversion.InspirationOutputWorker;
import dk.nota.oxygen.EditorAccess;
import dk.nota.oxygen.ResultsListener;
import dk.nota.oxygen.ResultsView;
import net.sf.saxon.s9api.XdmNode;

public class InspirationOutputAction extends EpubAction {
	
	private InspirationOutput inspirationOutput;
	
	public InspirationOutputAction(InspirationOutput inspirationOutput) {
		super(inspirationOutput.getName(), false);
		this.inspirationOutput = inspirationOutput;
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
		URI outputUri;
		switch (inspirationOutput) {
		case INSP_PRINT:
			outputUri = epubAccess.getArchiveUri().resolve("print/");
			break;
		case INSP_PROOF:
			outputUri = epubAccess.getArchiveUri().resolve("korrektur.html");
			break;
		default:
			outputUri = epubAccess.getArchiveUri().resolve(inspirationOutput
				.getPrefix() + epubAccess.getPid().replaceFirst(
				"^(dk-nota-)*.{4}", "") + ".xml");
		}
		InspirationOutputWorker inspirationOutputWorker =
				new InspirationOutputWorker(epubAccess, opfDocument,
						new ResultsListener(new ResultsView(epubAccess.getPid()
						+ " - Create " + inspirationOutput.getName())),
						inspirationOutput, outputUri);
		inspirationOutputWorker.execute();
	}

}
