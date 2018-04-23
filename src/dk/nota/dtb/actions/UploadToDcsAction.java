package dk.nota.dtb.actions;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.URI;

import javax.swing.AbstractAction;

import org.apache.http.client.HttpResponseException;

import dk.nota.dcs.DtbUploader;
import net.sf.saxon.s9api.SaxonApiException;

public class UploadToDcsAction extends AbstractAction {
	
	public UploadToDcsAction() {
		super("Upload to DCS");
	}

	@Override
	public void actionPerformed(ActionEvent event) {
	}

}
