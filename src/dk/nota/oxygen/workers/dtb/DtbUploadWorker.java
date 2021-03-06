package dk.nota.oxygen.workers.dtb;

import java.net.URI;
import dk.nota.oxygen.workers.AbstractWorkerWithResults;
import dk.nota.dcs.DtbUploader;
import dk.nota.oxygen.EditorAccessProvider;
import dk.nota.oxygen.ResultsListener;

public class DtbUploadWorker extends AbstractWorkerWithResults<Object,Object>{
	
	private URI documentUri;
	private String pid;

	public DtbUploadWorker(ResultsListener resultsListener, URI documentUri,
			String pid) {
		super("DTBOOK UPLOAD", resultsListener);
		this.documentUri = documentUri;
		this.pid = pid;
	}

	@Override
	protected Object doInBackground() throws Exception {
		fireResultsUpdate("DTBOOK UPLOAD STARTING");
		DtbUploader dtbUploader = new DtbUploader(documentUri, pid);
		fireResultsUpdate("CHECKING DIRECTORY...");
		dtbUploader.checkDirectory();
		dtbUploader.setDcsId();
		int overwrite = 0;
		if (dtbUploader.getDcsId() != 0) {
			overwrite = EditorAccessProvider.getEditorAccess()
					.getPluginWorkspace().showConfirmDialog("DTBook upload", 
							pid.replaceFirst("^(dk-nota-|DK-NOTA-)", "")
							+ " already exists in the archive: "
							+ "do you wish to overwrite the existing file(s)?",
							new String[] { "Overwrite", "Cancel" },
							new int[] { 1, 0 }, 1);
			if (overwrite == 1) {
				fireResultsUpdate("UPDATING TITLE...");
				dtbUploader.updateTitle();
			} else fireResultsUpdate("UPLOAD CANCELLED");
		} else {
			fireResultsUpdate("CREATING TITLE...");
			dtbUploader.createTitle();
		}
		return null;
	}

}