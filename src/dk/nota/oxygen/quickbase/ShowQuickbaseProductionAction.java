package dk.nota.oxygen.quickbase;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import ro.sync.exml.workspace.api.PluginWorkspaceProvider;

public class ShowQuickbaseProductionAction extends AbstractAction {
	
	String pid;
	int rid;
	
	public ShowQuickbaseProductionAction() {
		super("Show production in QuickBase");
		setEnabled(false);
	}
	
	public ShowQuickbaseProductionAction(String pid, int rid) {
		super(String.format("Show %s in QuickBase", pid));
		this.pid = pid;
		this.rid = rid;
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		String url = QuickbaseAccess.QB_TABLE_URL;
		if (rid == 0) {
			// If we don't have the record ID we can't link directly to the
			// record, so we show a report based on the production ID instead.
			// TODO: Find workaround if possible - maybe query for the RID if
			// that's not too time-consuming?
			url += String.format("?a=q&qt=tab&query=({'14'.EX.'%s'})", pid);
		} else url += "?a=dr&rid=%s" + rid;
		PluginWorkspaceProvider.getPluginWorkspace()
			.openInExternalApplication(url, true, "text/html");
	}
	
	public void update(String pid) {
		this.pid = pid;
		putValue(AbstractAction.NAME, String.format("Show %s in QuickBase",
				pid.replaceFirst("^dk-nota-", "")));
	}
	
	public void update(String pid, int rid) {
		this.rid = rid;
		update(pid);
	}

}
