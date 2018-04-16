package dk.nota.xml;

public class XmlAccessProvider {
	
	private static XmlAccess epubXmlAccess = new XmlAccess(ProcessorProvider
			.getProcessor());
	
	public static XmlAccess getXmlAccess() {
		return epubXmlAccess;
}

}
