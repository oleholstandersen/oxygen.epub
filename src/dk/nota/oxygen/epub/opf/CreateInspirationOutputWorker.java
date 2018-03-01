package dk.nota.oxygen.epub.opf;

import java.io.File;
import java.net.MalformedURLException;
import java.util.concurrent.ExecutionException;

import dk.nota.oxygen.common.ImageStoringResultsListener;
import dk.nota.oxygen.epub.common.EpubAccess;
import net.sf.saxon.s9api.XdmDestination;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XsltTransformer;
import ro.sync.document.DocumentPositionedInfo;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;

public class CreateInspirationOutputWorker extends CreateDtbWorker {
	
	private InspOutputType outputType;

	public CreateInspirationOutputWorker(
			ImageStoringResultsListener imageListener, EpubAccess epubAccess,
			File outputFile, InspOutputType outputType) {
		super("INSPIRATION CONVERSION", imageListener, epubAccess,
				outputFile);
		this.outputType = outputType;
	}
	
	@Override
	protected XdmNode doInBackground() throws Exception {
		XdmNode dtbDocument = super.doInBackground();
		fireResultsUpdate(String.format("CONVERTING TO %s...", outputType
				.getName().toUpperCase()));
		XsltTransformer outputTransformer = null;
		switch (outputType) {
		case INSP_AUDIO:
			outputTransformer = getEpubAccess().getEpubXmlAccess()
					.getXsltTransformer("inspiration/inspiration-audio.xsl");
			break;
		case INSP_BRAILLE:
			outputTransformer = getEpubAccess().getEpubXmlAccess()
					.getXsltTransformer("inspiration/inspiration-braille.xsl");
			break;
		case INSP_ETEXT:
			outputTransformer = getEpubAccess().getEpubXmlAccess()
					.getXsltTransformer("inspiration/inspiration-etext.xsl");
			break;
		case INSP_PRINT:
			outputTransformer = getEpubAccess().getEpubXmlAccess()
					.getXsltTransformer("inspiration/inspiration-print.xsl");
			outputTransformer.setBaseOutputURI(getFile().toURI()
					.toString());
			break;
		case INSP_PROOF:
			// TODO: Improve dtb-inspiration-proof.xsl
			outputTransformer = getEpubAccess().getEpubXmlAccess()
					.getXsltTransformer("inspiration/inspiration-proof.xsl");
		}
		outputTransformer.setErrorListener(getResultsListener());
		outputTransformer.setMessageListener(getResultsListener());
		outputTransformer.setInitialContextNode(dtbDocument);
		outputTransformer.setDestination(
				outputType != InspOutputType.INSP_PRINT ?
				getEpubAccess().getEpubXmlAccess().getSerializer(getFile()) :
				new XdmDestination());
		outputTransformer.transform();
		return null;
	}
	
	@Override
	protected void done() {
		try {
			get();
			fireResultsUpdate("INSPIRATION CONVERSION DONE");
			if (outputType != InspOutputType.INSP_PRINT)
				PluginWorkspaceProvider.getPluginWorkspace().open(
						getFile().toURI().toURL());
		} catch (InterruptedException | ExecutionException e) {
			getResultsListener().writeException(
					e instanceof ExecutionException ? e.getCause() : e,
					DocumentPositionedInfo.SEVERITY_FATAL);
		} catch (MalformedURLException e) {
			PluginWorkspaceProvider.getPluginWorkspace().showErrorMessage(
					"Unable to open DTBook file due to malformed URL", e);
		}
	}

}
