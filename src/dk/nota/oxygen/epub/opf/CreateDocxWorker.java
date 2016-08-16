package dk.nota.oxygen.epub.opf;

import de.schlichtherle.io.DefaultArchiveDetector;
import de.schlichtherle.io.File;
import dk.nota.oxygen.common.ConsoleWindow;
import dk.nota.oxygen.common.EditorAccess;
import dk.nota.oxygen.common.ZipArchiveDetector;
import dk.nota.oxygen.epub.common.EpubAccess;
import net.sf.saxon.s9api.XdmNode;

public class CreateDocxWorker extends CreateDtbWorker {
	
	private File docxFile;
	
	public CreateDocxWorker(EditorAccess editorAccess, EpubAccess epubAccess,
			ConsoleWindow consoleWindow, java.io.File docxFile) {
		super(editorAccess, epubAccess, consoleWindow, docxFile, true, false);
		this.docxFile = new ZipArchiveDetector().createFile(docxFile);
	}

	@Override
	protected Object doInBackground() throws Exception {
		XdmNode dtbDocument = (XdmNode)super.doInBackground();
		getConsoleWindow().writeToConsole("CONVERTING TO DOCX...");
		return null;
	}

}
