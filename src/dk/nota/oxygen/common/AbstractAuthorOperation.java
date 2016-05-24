package dk.nota.oxygen.common;

import java.util.LinkedList;
import java.util.List;

import javax.swing.text.BadLocationException;

import ro.sync.ecss.extensions.api.ArgumentDescriptor;
import ro.sync.ecss.extensions.api.ArgumentsMap;
import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.ecss.extensions.api.AuthorDocumentController;
import ro.sync.ecss.extensions.api.AuthorElementBaseInterface;
import ro.sync.ecss.extensions.api.AuthorOperation;
import ro.sync.ecss.extensions.api.AuthorOperationException;
import ro.sync.ecss.extensions.api.access.AuthorEditorAccess;
import ro.sync.ecss.extensions.api.access.AuthorWorkspaceAccess;
import ro.sync.ecss.extensions.api.content.OffsetInformation;
import ro.sync.ecss.extensions.api.node.AttrValue;
import ro.sync.ecss.extensions.api.node.AuthorDocumentFragment;
import ro.sync.ecss.extensions.api.node.AuthorElement;
import ro.sync.ecss.extensions.api.node.AuthorNode;

public abstract class AbstractAuthorOperation implements AuthorOperation {
	
	private AuthorAccess authorAccess;
	
	public void dissolveElement(AuthorElement element)
			throws AuthorOperationException, BadLocationException {
		floatInterval(element.getStartOffset() + 1, element.getEndOffset() - 1);
	}
	
	protected abstract void doOperation() throws AuthorOperationException;

	@Override
	public void doOperation(AuthorAccess authorAccess, ArgumentsMap arguments)
			throws IllegalArgumentException, AuthorOperationException {
		this.authorAccess = authorAccess;
		parseArguments(arguments);
		doOperation();
	}
	
	public Object evaluateXpathSingle(String xpath, AuthorNode contextNode)
			throws AuthorOperationException {
		return getDocumentController().evaluateXPath(xpath, contextNode, true,
				true, true, true)[0];
	}
	
	public void floatInterval(int start, int end) throws BadLocationException {
		AuthorDocumentFragment content = getDocumentController()
				.createDocumentFragment(start, end);
		getDocumentController().delete(start, end);
		AuthorNode parentNode = getDocumentController().getNodeAtOffset(start);
		if (parentNode.getEndOffset() - parentNode.getStartOffset() == 1) {
			getDocumentController().deleteNode(parentNode);
			getDocumentController().insertFragment(start - 1, content);
			return;
		}
		splitNodeAtOffset(parentNode, start, true);
		OffsetInformation offsetInformation = getDocumentController()
				.getContentInformationAtOffset(start);
		switch (offsetInformation.getPositionType()) {
		case OffsetInformation.ON_START_MARKER:
			getDocumentController().insertFragment(start - 1, content);
			break;
		case OffsetInformation.ON_END_MARKER:
			getDocumentController().insertFragment(start + 1, content);
			break;
		default: getDocumentController().insertFragment(start, content);
		}
	}
	
	public AuthorNode floatNode(AuthorNode node)
			throws AuthorOperationException, BadLocationException {
		int start = node.getStartOffset();
		floatInterval(start, node.getEndOffset());
		return getDocumentController().getNodeAtOffset(start + 2);
	}
	
	public boolean fragmentIsEmptyElement(AuthorDocumentFragment fragment) {
		if (fragment.getContentNodes().size() > 1) return false;
		AuthorNode fragmentNode = fragment.getContentNodes().get(0);
		if (!(fragmentNode instanceof AuthorElement)) return false;
		return ((AuthorElement)fragmentNode).getContentNodes().isEmpty();
	}

	@Override
	public abstract ArgumentDescriptor[] getArguments();
	
	public AuthorAccess getAuthorAccess() {
		return authorAccess;
	}
	
	public AuthorEditorAccess getAuthorEditor() {
		return getAuthorAccess().getEditorAccess();
	}
	
	@Override
	public abstract String getDescription();
	
	public AuthorDocumentController getDocumentController() {
		return authorAccess.getDocumentController();
	}
	
	public AuthorElement[] getElements(AuthorNode[] nodes) {
		LinkedList<AuthorElement> elements = new LinkedList<AuthorElement>();
		for (AuthorNode node : nodes) {
			if (node instanceof AuthorElement)
				elements.add((AuthorElement)node);
		}
		return elements.toArray(new AuthorElement[elements.size()]);
	}
	
	public AuthorElement[] getElementsByXpath(String xpath)
			throws AuthorOperationException {
		return getElements(getNodesByXpath(xpath));
	}
	
	public AuthorElement[] getElementsByXpath(String xpath,
			AuthorNode contextNode) throws AuthorOperationException {
		return getElements(getNodesByXpath(xpath, contextNode));
	}
	
	public AuthorElement getFirstElement(AuthorNode[] nodes) {
		for (AuthorNode node : nodes)
			if (node instanceof AuthorElement) return (AuthorElement)node;
		return null;
	}
	
	public AuthorElement getFirstElementByXpath(String xpath)
			throws AuthorOperationException {
		return getFirstElement(getNodesByXpath(xpath));
	}
	
	public AuthorElement getFirstElementByXpath(String xpath,
			AuthorNode contextNode) throws AuthorOperationException {
		return getFirstElement(getNodesByXpath(xpath, contextNode));
	}
	
	public AuthorNode[] getNodesByXpath(String xpath)
			throws AuthorOperationException {
		return getDocumentController().findNodesByXPath(xpath, true, true,
				true);
	}
	
	public AuthorNode[] getNodesByXpath(String xpath,
			AuthorNode contextNode) throws AuthorOperationException {
		return getDocumentController().findNodesByXPath(xpath, contextNode,
				true, true, true, true);
	}
	
	public List<AuthorNode> getSelectedNodes() throws BadLocationException {
		int start = getSelectionStart();
		int end = getSelectionEnd();
		if (start == end) {
			LinkedList<AuthorNode> nodes = new LinkedList<AuthorNode>();
			nodes.add(getDocumentController().getNodeAtOffset(start));
			return nodes;
		}
		return getDocumentController().getNodesToSelect(start, end);
	}
	
	public int getSelectionEnd() {
		return getAuthorEditor().getSelectionEnd();
	}
	
	public int getSelectionStart() {
		return getAuthorEditor().getSelectionStart();
	}
	
	public AuthorWorkspaceAccess getWorkspace() {
		return getAuthorAccess().getWorkspaceAccess();
	}
	
	public boolean hasBlockContent(AuthorElement element)
			throws AuthorOperationException, BadLocationException {
		for (AuthorNode node : element.getContentNodes()) {
			if (!getDocumentController().inInlineContext(node.getStartOffset()))
				return true;
		}
		return false;
	}
	
	public boolean hasClassAttribute(AuthorElement element) {
		return (element.getAttribute("class") != null);
	}
	
	public AuthorNode moveNodeToOffset(AuthorNode node, int offset)
			throws BadLocationException {
		AuthorDocumentFragment nodeFragment = getDocumentController()
				.createDocumentFragment(node, true);
		getDocumentController().deleteNode(node);
		getDocumentController().insertFragment(offset++, nodeFragment);
		return getDocumentController().getNodeAtOffset(offset);
	}
	
	protected abstract void parseArguments(ArgumentsMap arguments)
			throws IllegalArgumentException;
	
	public void resetClass(AuthorElement element, String classValue) {
		getDocumentController().setAttribute("class", new AttrValue(classValue),
				element);
	}
	
	public boolean selectionIsSiblingsOnly() throws BadLocationException {
		AuthorNode nodeAtStart = getDocumentController().getNodeAtOffset(
				getSelectionStart());
		AuthorNode nodeAtEnd = getDocumentController().getNodeAtOffset(
				getSelectionEnd());
		return nodeAtStart.getParent() == nodeAtEnd.getParent();
	}
	
	public void showErrorMessage(String message) {
		getWorkspace().showErrorMessage(message);
	}
	
	public void showInformationMessage(String message) {
		getWorkspace().showInformationMessage(message);
	}
	
	public void showStatusMessage(String message) {
		getWorkspace().showStatusMessage(message);
	}
	
	public void splitNodeAtOffset(AuthorNode node, int offset,
			boolean discardEmptyRemainders) throws BadLocationException {
		int start = node.getStartOffset();
		int end = node.getEndOffset();
		if (offset == start || offset == end) return;
		AuthorDocumentFragment fragmentBefore = getDocumentController()
				.createDocumentFragment(start, offset - 1);
		AuthorDocumentFragment fragmentAfter = getDocumentController()
				.createDocumentFragment(offset, end);
		// Avoid duplicate id on element after split
		if (node instanceof AuthorElement) ((AuthorElement)fragmentAfter
				.getContentNodes().get(0)).removeAttribute("id");
		getDocumentController().delete(start, end);
		if (!discardEmptyRemainders || !fragmentIsEmptyElement(fragmentAfter))
			getDocumentController().insertFragment(start, fragmentAfter);
		if (!discardEmptyRemainders || !fragmentIsEmptyElement(fragmentBefore))
			getDocumentController().insertFragment(start, fragmentBefore);
	}
	
	public void stripElements(AuthorElement element, String... elementNames)
			throws AuthorOperationException, BadLocationException {
		for (AuthorElement childElement : getElementsByXpath("*", element)) {
			boolean matches = false;
			for (String elementName : elementNames) {
				if (childElement.getName().equals(elementName)) {
					matches = true;
					break;
				}
			}
			stripElements(childElement, elementNames);
			if (matches) dissolveElement(childElement);
		}
	}

}
