package dk.nota.oxygen.common;

import java.util.LinkedList;

import javax.xml.transform.SourceLocator;

import dk.nota.oxygen.xml.EpubXmlAccess;
import net.sf.saxon.s9api.Axis;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmSequenceIterator;

public class ImageStoringResultsListener extends ResultsListener {
		
	private LinkedList<String> imagePaths = new LinkedList<String>();
	
	public ImageStoringResultsListener(ResultsView resultsView) {
		super(resultsView);
	}
	
	public LinkedList<String> getImageUrls() {
		return imagePaths;
	}
	
	@Override
	public void handleMessage(XdmNode message, boolean terminate,
			SourceLocator sourceLocator) {
		XdmSequenceIterator messageIterator = message.axisIterator(Axis
				.DESCENDANT_OR_SELF, new QName(EpubXmlAccess.NOTA_NAMESPACE,
						"image"));
		while (messageIterator.hasNext()) imagePaths.add(messageIterator
				.next().getStringValue());						
	}
	
}