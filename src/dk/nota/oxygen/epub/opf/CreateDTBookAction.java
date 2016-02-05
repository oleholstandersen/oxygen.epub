package dk.nota.oxygen.epub.opf;

import java.io.File;

import dk.nota.oxygen.common.EditorAccess;
import dk.nota.oxygen.epub.common.ArchiveSensitiveAction;

public class CreateDTBookAction extends ArchiveSensitiveAction {

	public CreateDTBookAction() {
		super("Create DTBook");
	}

	@Override
	public void actionPerformed(EditorAccess editorAccess) {
		File outputFolder = editorAccess.getWorkspace().chooseDirectory();
	}

}
