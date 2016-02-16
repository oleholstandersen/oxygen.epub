package dk.nota.oxygen.epub.opf;

import java.io.File;
import java.io.IOException;

import dk.nota.oxygen.common.EditorAccess;
import dk.nota.oxygen.epub.common.ArchiveSensitiveAction;
import dk.nota.oxygen.epub.common.EpubAccess;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmNode;

public class CreateDTBookAction extends ArchiveSensitiveAction {

	public CreateDTBookAction() {
		super("Create DTBook");
	}

	@Override
	public void actionPerformed(EditorAccess editorAccess) {
		try {
			EpubAccess epubAccess = editorAccess.getEpubAccess();
			XdmNode concatNode = epubAccess.getConcatDocument();
			File dtbFile = editorAccess.getWorkspace().chooseFile(epubAccess
					.getArchiveFile().getParentFile(), "Save DTBook",
					new String[] {"xml"}, "DTBook", true);
			if (dtbFile == null) return;
			
		} catch (SaxonApiException | IOException e) {
			editorAccess.showErrorMessage(e.toString());
		}
	}

}