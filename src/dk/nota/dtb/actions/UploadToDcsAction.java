package dk.nota.dtb.actions;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.URI;

import javax.swing.AbstractAction;

import org.apache.http.client.HttpResponseException;

import dk.nota.dcs.DtbUploadWorker;
import dk.nota.dcs.DtbUploader;
import dk.nota.oxygen.EditorAccessProvider;
import dk.nota.oxygen.ResultsListener;
import dk.nota.oxygen.ResultsView;
import net.sf.saxon.s9api.SaxonApiException;

public class UploadToDcsAction extends AbstractAction {
	
	public UploadToDcsAction() {
		super("Upload to DCS");
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		URI documentUri = URI.create(EditorAccessProvider.getEditorAccess()
				.getCurrentEditorUrl().toString());
		DtbUploadWorker dtbUploadWorker = new DtbUploadWorker(
				new ResultsListener(new ResultsView("Upload DTBook")),
				documentUri);
		dtbUploadWorker.execute();
	}

}
