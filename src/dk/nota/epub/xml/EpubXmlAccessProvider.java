package dk.nota.epub.xml;

import dk.nota.xml.ProcessorProvider;

public class EpubXmlAccessProvider {
	
	private static EpubXmlAccess epubXmlAccess = new EpubXmlAccess(
			ProcessorProvider.getProcessor());
	
	public static EpubXmlAccess getEpubXmlAccess() {
		return epubXmlAccess;
	}

}
