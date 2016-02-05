package dk.nota.oxygen.common;

import java.awt.event.ActionEvent;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.Action;

import ro.sync.ecss.extensions.api.ArgumentDescriptor;
import ro.sync.ecss.extensions.api.ArgumentsMap;
import ro.sync.ecss.extensions.api.AuthorOperationException;
import ro.sync.exml.workspace.api.editor.page.author.actions.ActionPerformedListener;
import ro.sync.exml.workspace.api.editor.page.author.actions.AuthorActionsProvider;

public class RepeatOperation extends AbstractAuthorOperation {
	
	private ActionEvent lastActionEvent;
	private ActionPerformedListener listener;
	private Procedure procedure;
	
	@Override
	protected void doOperation() throws AuthorOperationException {
		AuthorActionsProvider actionsProvider = getAuthorEditor()
				.getActionsProvider();
		Map<String,Object> actionsMap = actionsProvider
				.getAuthorExtensionActions();
		switch (procedure) {
		case STORE:
			if (listener == null) listener = new ActionPerformedListener() {
				@Override
				public void afterActionPerformed(Object actionEvent) {
					if (actionEvent == null) return;
					lastActionEvent = (ActionEvent)actionEvent;
				}
			};
			for (Entry<String,Object> entry : actionsMap.entrySet()) {
				if (entry.getKey().startsWith("operation")) continue;
				actionsProvider.addActionPerformedListener(entry.getValue(),
						listener);
			}
			return;
		case DISCARD:
			lastActionEvent = null;
			return;
		case PERFORM:
			if (lastActionEvent == null) throw new AuthorOperationException(
					"No stored operation to repeat");
			Object source = lastActionEvent.getSource();
			String actionId = actionsProvider.getActionID(source);
			if (actionId == null) throw new AuthorOperationException(
					"Could not retrieve operation id");
			Action action = (Action)actionsMap.get(actionId.substring(7));
			if (action == null) throw new AuthorOperationException(
					"Could not retrieve operation");	
			actionsProvider.invokeAuthorExtensionActionInContext(action,
						getSelectionStart());
		}
	}

	@Override
	public ArgumentDescriptor[] getArguments() {
		return new ArgumentDescriptor[] {
				new ArgumentDescriptor("procedure", ArgumentDescriptor
						.TYPE_CONSTANT_LIST, "The intended procedure: " +
						"store, perform or discard most recent action",
						new String[] {"store", "perform", "discard"}, "store"),
		};
	}

	@Override
	public String getDescription() {
		return "Repeats most recent operation";
	}

	@Override
	protected void parseArguments(ArgumentsMap arguments)
			throws IllegalArgumentException {
		String argument = (String)arguments.getArgumentValue("procedure");
		if (argument.equals("store")) procedure = Procedure.STORE;
		else if (argument.equals("perform")) procedure = Procedure.PERFORM;
		else if (argument.equals("discard")) procedure = Procedure.DISCARD;
	}
	
	public enum Procedure {
		
		STORE, PERFORM, DISCARD;
		
	}

}
