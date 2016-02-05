package dk.nota.oxygen.epub.xhtml;

import java.util.LinkedList;

import javax.swing.text.BadLocationException;

import dk.nota.oxygen.epub.common.EpubAuthorOperation;
import ro.sync.ecss.extensions.api.ArgumentDescriptor;
import ro.sync.ecss.extensions.api.ArgumentsMap;
import ro.sync.ecss.extensions.api.AuthorOperationException;
import ro.sync.ecss.extensions.api.node.AuthorElement;
import ro.sync.ecss.extensions.api.node.AuthorNode;

public class ListOperation extends EpubAuthorOperation {
	
	private ListType listType;
	
	@Override
	protected void doOperation() throws AuthorOperationException {
		try {
			LinkedList<AuthorElement> blocks = new LinkedList<AuthorElement>();
			// Get relevant selected blocks: list items or paragraphs
			for (AuthorNode node : getSelectedNodes()) {
				AuthorElement block = getFirstElementByXpath(
						"ancestor-or-self::li|ancestor-or-self::p", node);
				if (block != null && !blocks.contains(block)) blocks.add(block);
			}
			for (AuthorElement block : blocks)
				getDocumentController().renameElement(block,
						listType == ListType.NONE ? "p" : "li");
			AuthorElement parent = (AuthorElement)getCommonParent(blocks
					.getFirst().getStartOffset(), blocks.getLast()
					.getEndOffset());
			if (parent.getLocalName().matches("^(ol|ul)$")) {
				if (listType == ListType.NONE) floatInterval(blocks.getFirst()
						.getStartOffset(), blocks.getLast().getEndOffset());
				else getDocumentController().renameElement(parent,
						listType.getName());
			} else {
				wrapInFragment(listType.getFragment(), blocks.getFirst()
						.getStartOffset(), blocks.getLast().getEndOffset());
			}
		} catch (BadLocationException e) {
			throw new AuthorOperationException(e.toString());
		}
	}

	@Override
	public ArgumentDescriptor[] getArguments() {
		return new ArgumentDescriptor[] {
				new ArgumentDescriptor("listType", ArgumentDescriptor
						.TYPE_CONSTANT_LIST, "The desired list type",
						new String[] {"ordered", "unordered", "none"},
						"unordered")
		};
	}

	@Override
	public String getDescription() {
		return "Allows manipulation of lists and list items";
	}

	@Override
	protected void parseArguments(ArgumentsMap arguments)
			throws IllegalArgumentException {
		String argument = (String)arguments.getArgumentValue("listType");
		if (argument.equals("ordered")) listType = ListType.ORDERED;
		else if (argument.equals("unordered")) listType = ListType.UNORDERED;
		else if (argument.equals("none")) listType = ListType.NONE;
	}
	
	public enum ListType {
		
		ORDERED, UNORDERED, NONE;
		
		public String getFragment() {
			switch (this) {
			case ORDERED: return "<ol xmlns='http://www.w3.org/1999/xhtml'/>";
			case UNORDERED: return "<ul xmlns='http://www.w3.org/1999/xhtml'/>";
			default: return null;
			}
		}
		
		public String getName() {
			switch (this) {
			case ORDERED: return "ol";
			case UNORDERED: return "ul";
			default: return null;
			}
		}
		
	}

}
