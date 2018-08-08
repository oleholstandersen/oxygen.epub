package dk.nota.oxygen.actions.dtb;

import java.awt.event.ActionEvent;
import java.net.URI;

import javax.swing.AbstractAction;

import dk.nota.oxygen.EditorAccessProvider;
import dk.nota.oxygen.ResultsListener;
import dk.nota.oxygen.ResultsView;
import dk.nota.oxygen.workers.dtb.DtbUploadWorker;

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