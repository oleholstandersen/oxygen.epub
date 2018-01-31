package dk.nota.oxygen.epub.opf;

import java.io.File;
import java.net.MalformedURLException;

import dk.nota.oxygen.common.ResultsView;
import dk.nota.oxygen.common.EditorAccess;
import dk.nota.oxygen.epub.common.EpubAccess;
import dk.nota.oxygen.xml.ResultsViewListener;
import net.sf.saxon.s9api.XdmDestination;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XsltTransformer;

public class CreateInspirationOutputWorker extends CreateDtbWorker {
	
	private InspOutputType outputType;
	private boolean success = false;

	public CreateInspirationOutputWorker(EditorAccess editorAccess,
			EpubAccess epubAccess, ResultsView resultsView,
			File outputFile, InspOutputType outputType) {
		super(editorAccess, epubAccess, resultsView, outputFile, true);
		this.outputType = outputType;
	}
	
	@Override
	protected Object doInBackground() throws Exception {
		XdmNode dtbDocument = (XdmNode)super.doInBackground();
		getResultsView().writeResult(String.format("CONVERTING TO %s...",
				outputType.getName().toUpperCase()));
		XsltTransformer outputTransformer = null;
		switch (outputType) {
		case INSP_AUDIO:
			outputTransformer = getEpubAccess().getXmlAccess()
					.getXsltTransformer("inspiration/inspiration-audio.xsl");
			break;
		case INSP_BRAILLE:
			outputTransformer = getEpubAccess().getXmlAccess()
					.getXsltTransformer("inspiration/inspiration-braille.xsl");
			break;
		case INSP_ETEXT:
			outputTransformer = getEpubAccess().getXmlAccess()
					.getXsltTransformer("inspiration/inspiration-etext.xsl");
			break;
		case INSP_PRINT:
			outputTransformer = getEpubAccess().getXmlAccess()
					.getXsltTransformer("inspiration/inspiration-print.xsl");
			outputTransformer.setBaseOutputURI(getOutputFile().toURI()
					.toString());
			break;
		case INSP_PROOF:
			// TODO: Improve dtb-inspiration-proof.xsl
			outputTransformer = getEpubAccess().getXmlAccess()
					.getXsltTransformer("inspiration/inspiration-proof.xsl");
		}
		outputTransformer.setErrorListener(new ResultsViewListener(
				getResultsView()));
		outputTransformer.setMessageListener(new ResultsViewListener(
				getResultsView()));
		outputTransformer.setInitialContextNode(dtbDocument);
		outputTransformer.setDestination(outputType != InspOutputType.INSP_PRINT ?
				getEpubAccess().getXmlAccess().getSerializer(getOutputFile()) :
				new XdmDestination());
		outputTransformer.transform();
		success = true;
		return null;
	}
	
	@Override
	protected void done() {
		if (success) {
			getResultsView().writeResult("CONVERSION DONE");
			try {
				if (outputType != InspOutputType.INSP_PRINT)
					getEditorAccess().getWorkspace().open(getOutputFile().toURI()
						.toURL());
			} catch (MalformedURLException e) {
				getEditorAccess().showErrorMessage(e.toString());
			}
		}
	}

}
