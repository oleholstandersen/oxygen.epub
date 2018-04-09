package dk.nota.epub.conversion;

import java.net.URI;

import dk.nota.dtb.conversion.InspirationConverter;
import dk.nota.dtb.conversion.InspirationOutput;
import dk.nota.epub.AbstractEpubWorkerWithResults;
import dk.nota.epub.EpubAccess;
import dk.nota.epub.content.Concatter;
import dk.nota.oxygen.ResultsListener;
import dk.nota.xml.DocumentResult;
import dk.nota.xml.XmlAccessProvider;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XdmNode;

public class InspirationOutputWorker extends AbstractEpubWorkerWithResults {
	
	private InspirationOutput inspirationOutput;
	private URI outputUri;
	
	public InspirationOutputWorker(EpubAccess epubAccess,
			XdmNode opfDocument, ResultsListener listener,
			InspirationOutput inspirationOutput, URI outputUri) {
		super("INSPIRATION CONVERSION", listener, epubAccess, opfDocument);
		this.inspirationOutput = inspirationOutput;
		this.outputUri = outputUri;
	}

	@Override
	protected DocumentResult doInBackground() throws Exception {
		fireResultsUpdate("INSPIRATION CONVERSION STARTING");
		Concatter concatter = new Concatter(opfDocument, false);
		concatter.addListener(getResultsListener());
		DocumentResult documentResult = new DocumentResult(concatter.call());
		EpubToDtbConverter dtbConverter = new EpubToDtbConverter(documentResult
				.getDocuments().iterator().next(), opfDocument, outputUri);
		dtbConverter.addListener(getResultsListener());
		documentResult = new DocumentResult(dtbConverter.call());
		copyImages(outputUri.resolve("./"), concatter.getImages());
		InspirationConverter inspirationConverter = new InspirationConverter(
				inspirationOutput.getTransformer(), documentResult
				.getDocuments().iterator().next(), outputUri);
		documentResult = new DocumentResult(inspirationConverter.call());
		Serializer serializer = XmlAccessProvider.getXmlAccess()
				.getSerializer();
		if (inspirationOutput == InspirationOutput.INSP_PRINT)
			serializer.setOutputProperty(Serializer.Property.METHOD, "text");
		documentResult.writeDocuments(serializer);
		return documentResult;
	}

}
