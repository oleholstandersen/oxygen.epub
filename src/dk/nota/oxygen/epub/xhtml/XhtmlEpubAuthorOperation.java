package dk.nota.oxygen.epub.xhtml;

import javax.swing.text.BadLocationException;

import dk.nota.oxygen.common.AbstractAuthorOperation;
import ro.sync.ecss.extensions.api.AuthorOperationException;
import ro.sync.ecss.extensions.api.node.AttrValue;
import ro.sync.ecss.extensions.api.node.AuthorElement;

public abstract class XhtmlEpubAuthorOperation extends AbstractAuthorOperation {
	
	public boolean editingConcatDocument() {
		return getAuthorEditor().getEditorLocation().toString().endsWith(
				"concat.xhtml");
	}
	
	public boolean hasEpubType(AuthorElement element) {
		return (element.getAttribute("epub:type") != null);
	}
	
	public void normaliseToDepth(AuthorElement element, int depth)
			throws AuthorOperationException, BadLocationException {
		if (element.getName().matches("h\\d")) {
			if (depth > 6) {
				getDocumentController().renameElement(element, "p");
				setClass(element, "bridgehead");
				setEpubType(element, "bridgehead");
			} else getDocumentController().renameElement(element,
					"h" + depth);
			return;
		}
		for (AuthorElement childElement : getElementsByXpath("*", element))
			normaliseToDepth(childElement, depth + 1);
		if (element.getName().matches("section")) {
			if (depth >= 6 && !hasEpubType(element)) dissolveElement(element);
		}
	}
	
	public void normaliseToDepth(AuthorElement[] elements, int depth)
			throws AuthorOperationException, BadLocationException {
		for (AuthorElement element : elements) normaliseToDepth(element,
				depth);
	}
	
	public void setEpubType(AuthorElement element, String epubTypeValue) {
		getDocumentController().setAttribute("epub:type",
				new AttrValue(epubTypeValue), element);
	}

}
