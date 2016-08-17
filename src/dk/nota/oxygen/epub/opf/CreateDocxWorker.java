package dk.nota.oxygen.epub.opf;

import java.net.URI;

import dk.nota.oxygen.common.ConsoleWindow;
import dk.nota.oxygen.common.EditorAccess;
import dk.nota.oxygen.epub.common.EpubAccess;
import net.sf.saxon.s9api.XdmDestination;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XsltTransformer;

public class CreateDocxWorker extends CreateDtbWorker {
	
	private URI docxFileUri;
	
	public CreateDocxWorker(EditorAccess editorAccess, EpubAccess epubAccess,
			ConsoleWindow consoleWindow, java.io.File docxFile) {
		super(editorAccess, epubAccess, consoleWindow, docxFile, true, false);
		docxFileUri = docxFile.toURI().resolve(docxFile.getName() + "/");
	}

	@Override
	protected Object doInBackground() throws Exception {
		XdmNode dtbDocument = (XdmNode)super.doInBackground();
		getConsoleWindow().writeToConsole("CONVERTING TO DOCX...");
		XsltTransformer docxTransformer = getEpubAccess().getXmlAccess()
				.getXsltTransformer("dtbook2docx/dtbook2docx.xsl");
		docxTransformer.setInitialContextNode(dtbDocument);
		docxTransformer.setDestination(new XdmDestination());
		docxTransformer.setBaseOutputURI(docxFileUri.toString());
		docxTransformer.transform();
		copyImages(docxFileUri.resolve("word/media/"));
		
		return null;
	}
	
	@Override
	protected void done() {
		getConsoleWindow().writeToConsole("DOCX CONVERSION DONE");
	}

}
