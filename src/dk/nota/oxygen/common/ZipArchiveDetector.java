package dk.nota.oxygen.common;

import de.schlichtherle.io.DefaultArchiveDetector;

public class ZipArchiveDetector extends DefaultArchiveDetector {
	
	public ZipArchiveDetector() {
		super(DefaultArchiveDetector.ALL, "docx|epub", DefaultArchiveDetector
				.ALL.getArchiveDriver(".jar"));
	}

}
