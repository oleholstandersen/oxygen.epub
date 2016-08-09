package dk.nota.oxygen.epub.opf;

import java.io.File;
import java.net.MalformedURLException;

import dk.nota.oxygen.common.ConsoleWindow;
import dk.nota.oxygen.common.EditorAccess;
import dk.nota.oxygen.epub.common.EpubAccess;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmDestination;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XsltTransformer;

public class CreateInspirationOutputWorker extends CreateDtbWorker {
	
	private InspirationOutputType outputType;
	private boolean success = false;

	public CreateInspirationOutputWorker(EditorAccess editorAccess,
			EpubAccess epubAccess, ConsoleWindow consoleWindow,
			File outputFile, InspirationOutputType outputType) {
		super(editorAccess, epubAccess, consoleWindow, outputFile, true);
		this.outputType = outputType;
	}
	
	@Override
	protected Object doInBackground() throws Exception {
		XdmNode dtbDocument = (XdmNode)super.doInBackground();
		getConsoleWindow().writeToConsole(String.format("CONVERTING TO %s...",
				outputType.getName().toUpperCase()));
		XsltTransformer outputTransformer = null;
		switch (outputType) {
		case AUDIO:
			outputTransformer = getEpubAccess().getXmlAccess()
					.getXsltTransformer("dtb-inspiration-audio.xsl");
			break;
		case BRAILLE:
			outputTransformer = getEpubAccess().getXmlAccess()
					.getXsltTransformer("dtb-inspiration-braille.xsl");
			break;
		case ETEXT:
			outputTransformer = getEpubAccess().getXmlAccess()
					.getXsltTransformer("dtb-inspiration-etext.xsl");
			break;
		case PRINT:
			// FIXME: Make Saxon extension functions work in s9api or handle
			// indent and other issues in some other way.
			outputTransformer = getEpubAccess().getXmlAccess()
						.getXsltTransformer("dtb-inspiration-print.xsl");
			outputTransformer.setBaseOutputURI(getOutputFile().toURI()
					.toString());
		case PROOF:
			// TODO: Improve dtb-inspiration-proof.xsl
			outputTransformer = getEpubAccess().getXmlAccess()
					.getXsltTransformer("dtb-inspiration-proof.xsl");
		}
		outputTransformer.setInitialContextNode(dtbDocument);
		outputTransformer.setDestination(
				outputType != InspirationOutputType.PRINT ?
				getEpubAccess().getXmlAccess().getSerializer(getOutputFile()) :
				new XdmDestination());
		outputTransformer.transform();
		success = true;
		return null;
	}
	
	@Override
	protected void done() {
		if (success) {
			getConsoleWindow().writeToConsole("CONVERSION DONE");
			try {
				if (outputType != InspirationOutputType.PRINT)
					getEditorAccess().getWorkspace().open(getOutputFile().toURI()
						.toURL());
			} catch (MalformedURLException e) {
				getEditorAccess().showErrorMessage(e.toString());
			}
		}
	}

}
