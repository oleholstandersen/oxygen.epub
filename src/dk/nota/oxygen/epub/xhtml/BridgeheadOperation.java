package dk.nota.oxygen.epub.xhtml;

import ro.sync.ecss.extensions.api.ArgumentDescriptor;
import ro.sync.ecss.extensions.api.ArgumentsMap;
import ro.sync.ecss.extensions.api.AuthorOperationException;
import ro.sync.ecss.extensions.api.node.AuthorElement;

public class BridgeheadOperation extends XhtmlEpubAuthorOperation {

	@Override
	protected void doOperation() throws AuthorOperationException {
		AuthorElement paragraph = getFirstElementByXpath(
				"ancestor-or-self::p");
		if (!hasClassAttribute(paragraph)) setClass(paragraph, "bridgehead");
		else getDocumentController().removeAttribute("class", paragraph);;
		if (!hasEpubType(paragraph)) setEpubType(paragraph, "bridgehead");
		else getDocumentController().removeAttribute("epub:type", paragraph);
	}

	@Override
	public ArgumentDescriptor[] getArguments() {
		return null; // No arguments to return
	}

	@Override
	public String getDescription() {
		return "Creates a bridgehead at the current position";
	}

	@Override
	protected void parseArguments(ArgumentsMap arguments)
			throws IllegalArgumentException {
		// No arguments to parse
	}

}
