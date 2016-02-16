package dk.nota.oxygen.epub.xhtml;

import javax.swing.text.BadLocationException;

import ro.sync.ecss.extensions.api.ArgumentDescriptor;
import ro.sync.ecss.extensions.api.ArgumentsMap;
import ro.sync.ecss.extensions.api.AuthorOperationException;
import ro.sync.ecss.extensions.api.node.AuthorDocumentFragment;
import ro.sync.ecss.extensions.api.node.AuthorElement;
import ro.sync.ecss.extensions.api.node.AuthorNode;

public class HeadingOperation extends XhtmlEpubAuthorOperation {
	
	private boolean dissolve;
	private int newDepth;
	private String sectionFragment =
			"<section xmlns='http://www.w3.org/1999/xhtml'/>";
	
	private int determineSectionStart(AuthorElement heading)
			throws AuthorOperationException {
		AuthorElement precedingBreak = getFirstElementByXpath(
				"preceding-sibling::*[1][@epub:type = 'pagebreak']", heading);
		if (precedingBreak != null) return precedingBreak.getStartOffset();
		return heading.getStartOffset();
	}
	
	private void dissolveSection(AuthorElement section, int depth)
			throws AuthorOperationException, BadLocationException {
		if (section.getName().equals("body")) return;
		AuthorElement precedingSection = getFirstElementByXpath(
				"preceding-sibling::*[1]/self::section", section);
		normaliseToDepth(section, --depth);
		if (precedingSection == null) dissolveElement(section);
		else {
			AuthorDocumentFragment sectionContent = getDocumentController().
					createDocumentFragment(section.getStartOffset() + 1,
							section.getEndOffset() - 1);
			getDocumentController().deleteNode(section);
			getDocumentController().insertFragment(precedingSection
					.getEndOffset(), sectionContent);
		}
	}

	@Override
	protected void doOperation() throws AuthorOperationException {
		try {
			AuthorElement heading = getFirstElementByXpath(
					"ancestor-or-self::*[parent::body or parent::section][1]");
			if (dissolve) getDocumentController().renameElement(heading, "p");
			else {
				stripElements(heading, "em", "strong");
				getDocumentController().renameElement(heading, "h" + newDepth);
			}
			AuthorElement[] ancestorSections = getElementsByXpath(
					"ancestor::body|ancestor::section", heading);
			int depth = ancestorSections.length;
			AuthorElement parentSection = ancestorSections[depth - 1];
			if (editingConcatDocument()) depth--; // Concat documents are deeper
			if (dissolve) {
				if (!parentSection.getName().equals("body")) dissolveSection(
						parentSection, depth);
				return;
			}
			normaliseToDepth(getElementsByXpath("following-sibling::*",
					heading), newDepth);
			int start = determineSectionStart(heading);
			if (newDepth == depth) {
				if (!parentSection.getName().equals("body")) splitNodeAtOffset(
						parentSection, start, true);
				return;
			}
			AuthorElement section = establishSection(parentSection, start);
			if (newDepth > depth) insertBelow(section, newDepth - depth);
			else if (newDepth < depth) insertAbove(section, depth - newDepth);
		} catch (BadLocationException e) {
			throw new AuthorOperationException(e.toString());
		}
	}
	
	private AuthorElement establishSection(AuthorElement section, int start)
			throws AuthorOperationException, BadLocationException {
		if (!section.getName().equals("body")) {
			if (section.getContentNodes().get(0).getStartOffset() == start)
				return section;
		}
		wrapInFragment(sectionFragment, start++, section.getEndOffset() - 1);
		newDepth--;
		return (AuthorElement)getDocumentController().getNodeAtOffset(start);
	}

	@Override
	public ArgumentDescriptor[] getArguments() {
		return new ArgumentDescriptor[] {
				new ArgumentDescriptor("newDepth", ArgumentDescriptor
						.TYPE_CONSTANT_LIST, "The desired new heading depth",
						new String[] {"1", "2", "3", "4", "5", "6", "NONE"},
						"NONE")
		};
	}

	@Override
	public String getDescription() {
		return "Allows manipulation of heading and sectioning elements";
	}
	
	private void insertAbove(AuthorNode section, int iterations)
			throws AuthorOperationException, BadLocationException {
		if (section.getParent().getName().equals("body")) {
			floatInterval(section.getStartOffset() + 1,
					section.getEndOffset() - 1);
			return;
		}
		section = floatNode(section);
		if (iterations > 1) insertAbove(section, --iterations);
	}
	
	private void insertBelow(AuthorNode section, int iterations)
			throws AuthorOperationException, BadLocationException {
		AuthorElement precedingSection = getFirstElementByXpath(
				"preceding-sibling::*[1]/self::section", section);
		if (precedingSection != null) {
			section = moveNodeToOffset(section, precedingSection
					.getEndOffset());
			iterations--;
		}
		int start = section.getStartOffset();
		int end = section.getEndOffset();
		for (int i = 1; i <= iterations; i++) wrapInFragment(sectionFragment,
				start, end++);
	}

	@Override
	protected void parseArguments(ArgumentsMap arguments)
			throws IllegalArgumentException {
		String depthArgument = (String)arguments.getArgumentValue("newDepth");
		dissolve = depthArgument.equals("NONE");
		if (!dissolve) newDepth = Integer.parseInt(depthArgument);
	}

}
