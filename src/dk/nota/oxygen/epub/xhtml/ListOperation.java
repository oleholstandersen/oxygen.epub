package dk.nota.oxygen.epub.xhtml;

import java.util.LinkedList;

import javax.swing.text.BadLocationException;

import ro.sync.ecss.extensions.api.ArgumentDescriptor;
import ro.sync.ecss.extensions.api.ArgumentsMap;
import ro.sync.ecss.extensions.api.AuthorOperationException;
import ro.sync.ecss.extensions.api.node.AuthorElement;
import ro.sync.ecss.extensions.api.node.AuthorNode;

public class ListOperation extends XhtmlEpubAuthorOperation {
	
	private OperationType operationType;
	
	private void create(LinkedList<AuthorElement> blocks)
			throws AuthorOperationException {
		int start = blocks.getFirst().getStartOffset();
		int end = blocks.getLast().getEndOffset();
		for (AuthorElement block : blocks) {
			if (block.getName().equals("p"))
				getDocumentController().renameElement(block, "li");
			else if (block.getName().matches("^(ol|ul)$")) {
				wrapInFragment(operationType.getListItemFragment(),
						block.getStartOffset(), block.getEndOffset());
				end += 2;
			}
		}
		wrapInFragment(operationType.getListFragment(), start, end);
	}
	
	private void dissolve(LinkedList<AuthorElement> listItems)
			throws AuthorOperationException, BadLocationException {
		for (AuthorElement listItem : listItems) {
			floatNode(listItem);
		}
	}
	
	@Override
	protected void doOperation() throws AuthorOperationException {
		try {
			LinkedList<AuthorElement> blocks = new LinkedList<AuthorElement>();
			for (AuthorNode node : getSelectedNodes()) {
				AuthorElement block = getFirstElementByXpath(operationType
						.getXpathForBlocks(), node);
				if (block != null && !blocks.contains(block)) blocks.add(block);
			}
			switch (operationType) {
			case CREATE_OL: case CREATE_UL:
				create(blocks);
				return;
			case INDENT_OL: case INDENT_UL:
				indent(blocks);
				return;
			case DISSOLVE:
				dissolve(blocks);
				return;
			}
		} catch (BadLocationException e) {
			throw new AuthorOperationException(e.toString());
		}
	}

	@Override
	public ArgumentDescriptor[] getArguments() {
		return new ArgumentDescriptor[] {
				new ArgumentDescriptor("operationType", ArgumentDescriptor
						.TYPE_CONSTANT_LIST, "The desired operation type",
						new String[] {"createOrdered", "createUnordered",
						"indentOrdered", "indentUnordered", "dissolve"},
						"convertOrdered")
		};
	}

	@Override
	public String getDescription() {
		return "Allows manipulation of lists and list items";
	}
	
	private void indent(LinkedList<AuthorElement> blocks)
			throws AuthorOperationException {
		int start = blocks.getFirst().getStartOffset();
		int end = blocks.getLast().getEndOffset();
		wrapInFragment(operationType.getListFragment(), start, end);
		wrapInFragment(operationType.getListItemFragment(), start, end);
	}

	@Override
	protected void parseArguments(ArgumentsMap arguments)
			throws IllegalArgumentException {
		String typeString = (String)arguments.getArgumentValue("operationType");
		switch (typeString) {
		case "createOrdered":
			operationType = OperationType.CREATE_OL;
			break;
		case "createUnordered":
			operationType = OperationType.CREATE_UL;
			break;
		case "indentOrdered":
			operationType = OperationType.INDENT_OL;
			break;
		case "indentUnordered":
			operationType = OperationType.INDENT_UL;
			break;
		case "dissolve":
			operationType = OperationType.DISSOLVE;
			break;
		}
	}
	
	public enum OperationType {
		
		CREATE_OL, CREATE_UL, INDENT_OL, INDENT_UL, DISSOLVE;
		
		public boolean createList() {
			return (this == CREATE_OL || this == CREATE_UL);
		}
		
		public String getListFragment() {
			switch (this) {
			case CREATE_OL: case INDENT_OL:
				return "<ol xmlns='http://www.w3.org/1999/xhtml'/>";
			case CREATE_UL: case INDENT_UL:
				return "<ul xmlns='http://www.w3.org/1999/xhtml'/>";
			default:
				return null;
			}
		}
		
		public String getListItemFragment() {
			return "<li xmlns='http://www.w3.org/1999/xhtml'/>";
		}
		
		public String getName() {
			switch (this) {
			case CREATE_OL: case INDENT_OL: return "ol";
			case CREATE_UL: case INDENT_UL: return "ul";
			default: return null;
			}
		}
		
		public String getXpathForBlocks() {
			switch (this) {
			case CREATE_OL: case CREATE_UL:
				return "(ancestor-or-self::ol|ancestor-or-self::p|" +
				    	"ancestor-or-self::ul)[1]";
			default: return "ancestor-or-self::li[last()]";
			}
		}
		
	}

}
