package dk.nota.oxygen.common;

import java.util.LinkedList;

import javax.xml.transform.SourceLocator;

import dk.nota.oxygen.xml.EpubXmlAccess;
import dk.nota.oxygen.xml.ResultsViewListener;
import net.sf.saxon.s9api.Axis;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmSequenceIterator;

public class ResultsViewImageListener extends ResultsViewListener {
		
	private LinkedList<String> imagePaths = new LinkedList<String>();
	
	public ResultsViewImageListener(ResultsView resultsView) {
		super(resultsView);
	}
	
	public LinkedList<String> getImagePaths() {
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