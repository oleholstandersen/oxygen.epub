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
	
	private void convert(LinkedList<AuthorElement> blocks)
			throws AuthorOperationException {
		int start = blocks.getFirst().getStartOffset();
		int end = blocks.getLast().getEndOffset();
		for (AuthorElement block : blocks) {
			if (block.getName().equals("p"))
				getDocumentController().renameElement(block, "li");
			else if (block.getName().matches("^(ol|ul)$"))
				wrapInFragment(operationType.getListItemFragment(),
						block.getStartOffset(), block.getEndOffset());
		}
		wrapInFragment(operationType.getListFragment(), start, end);
	}
	
	private void dissolve(LinkedList<AuthorElement> listItems)
			throws AuthorOperationException, BadLocationException {
		int start = listItems.getFirst().getStartOffset();
		int end = listItems.getLast().getEndOffset();
		floatInterval(start, end);
	}
	
	@Override
	protected void doOperation() throws AuthorOperationException {
		try {
			LinkedList<AuthorElement> blocks = new LinkedList<AuthorElement>();
			for (AuthorNode node : getSelectedNodes()) {
				AuthorElement block = getFirstElementByXpath(
						operationType.isConversion() ? "(ancestor-or-self::ol|" +
						"ancestor-or-self::p|ancestor-or-self::ul)[1]" :
						"ancestor-or-self::li[1]", node);
				if (block != null && !blocks.contains(block)) blocks.add(block);
			}
			switch (operationType) {
			case CONVERT_OL: case CONVERT_UL:
				convert(blocks);
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
						new String[] {"convertOrdered", "convertUnordered",
								"indentOrdered", "indentUnordered",
								"dissolve"},
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
		case "convertOrdered":
			operationType = OperationType.CONVERT_OL;
			break;
		case "convertUnordered":
			operationType = OperationType.CONVERT_UL;
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
		
		CONVERT_OL, CONVERT_UL, INDENT_OL, INDENT_UL, DISSOLVE;
		
		public boolean isConversion() {
			return (this == CONVERT_OL || this == CONVERT_UL);
		}
		
		public String getListFragment() {
			switch (this) {
			case CONVERT_OL: case INDENT_OL:
				return "<ol xmlns='http://www.w3.org/1999/xhtml'/>";
			case CONVERT_UL: case INDENT_UL:
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
			case CONVERT_OL: case INDENT_OL: return "ol";
			case CONVERT_UL: case INDENT_UL: return "ul";
			default: return null;
			}
		}
		
	}

}
