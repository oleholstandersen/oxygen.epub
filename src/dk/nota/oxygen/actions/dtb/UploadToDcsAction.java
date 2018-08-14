package dk.nota.oxygen.actions.dtb;

import java.awt.event.ActionEvent;
import java.net.URI;

import javax.swing.AbstractAction;

import dk.nota.oxygen.EditorAccess;
import dk.nota.oxygen.EditorAccessProvider;
import dk.nota.oxygen.ResultsListener;
import dk.nota.oxygen.ResultsView;
import dk.nota.oxygen.workers.dtb.DtbUploadWorker;
import dk.nota.xml.XmlAccess;
import dk.nota.xml.XmlAccessProvider;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmNode;

public class UploadToDcsAction extends AbstractAction {
	
	public UploadToDcsAction() {
		super("Upload to DCS");
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		EditorAccess editorAccess = EditorAccessProvider.getEditorAccess();
		URI documentUri = URI.create(editorAccess.getCurrentEditorUrl()
				.toString());
		XmlAccess xmlAccess = XmlAccessProvider.getXmlAccess();
		String pid;
		try {
			XdmNode document = xmlAccess.getDocument(documentUri);
			pid = xmlAccess.getFirstNodeByXpath(
					"//meta[@name = ('dc:identifier','dc:Identifier')]",
					document)
					.getAttributeValue(new QName("content"));
		} catch (SaxonApiException e) {
			editorAccess.showErrorMessage(
					"Unable to get PID from DTBook document", e);
			return;
		}
		DtbUploadWorker dtbUploadWorker = new DtbUploadWorker(
				new ResultsListener(new ResultsView(pid + " - Upload DTBook")),
				documentUri, pid);
		dtbUploadWorker.execute();
	}

}