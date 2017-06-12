package dk.nota.oxygen.epub.opf;

import dk.nota.oxygen.common.EditorAccess;
import dk.nota.oxygen.epub.common.ArchiveSensitiveAction;
import dk.nota.oxygen.epub.common.EpubAccess;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.XsltTransformer;

public class ImportDtbAction extends ArchiveSensitiveAction {
	
	public ImportDtbAction() {
		super("DTBook");
	}
	
	@Override
	public void actionPerformed(EditorAccess editorAccess) {
		EpubAccess epubAccess = editorAccess.getEpubAccess();
		java.io.File sourceFile = editorAccess.getWorkspace().chooseFile(
				null, "Import", new String[] {"xml"}, "DTBook files", false);
		if (sourceFile == null) return;
		try {
			XsltTransformer dtbImporter = epubAccess.getOpfTransformer(
					"import-dtb.xsl");
			dtbImporter.setParameter(new QName("DTB_URL"), new XdmAtomicValue(
					sourceFile.toURI().toString()));
			dtbImporter.setDestination(epubAccess.getOutputTransformer());
			dtbImporter.transform();
		} catch (SaxonApiException e) {
			editorAccess.showErrorMessage(e.toString());
		}
	}
}
