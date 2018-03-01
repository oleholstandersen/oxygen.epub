package dk.nota.oxygen.epub.opf;

import java.util.LinkedList;

import javax.xml.transform.SourceLocator;

import dk.nota.oxygen.common.ResultsListener;
import dk.nota.oxygen.common.ResultsView;
import dk.nota.oxygen.xml.EpubXmlAccess;
import net.sf.saxon.s9api.Axis;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmSequenceIterator;

public class CreateDtbListener extends ResultsListener {
	
	private LinkedList<String> imageUrls = new LinkedList<String>();

	public CreateDtbListener(ResultsView resultsView) {
		super(resultsView);
	}
	
	public LinkedList<String> getImageUrls() {
		return imageUrls;
	}
	
	@Override
	protected void handleMessage(XdmNode message, boolean terminate,
			SourceLocator sourceLocator) {
		XdmSequenceIterator messageIterator = message.axisIterator(Axis
				.DESCENDANT_OR_SELF, new QName(EpubXmlAccess.NOTA_NAMESPACE,
						"image"));
		while (messageIterator.hasNext())
			imageUrls.add(messageIterator.next().getStringValue());	
	}

}
