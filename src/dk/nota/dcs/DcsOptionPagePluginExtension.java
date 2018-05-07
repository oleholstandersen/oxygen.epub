package dk.nota.dcs;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import dk.nota.oxygen.quickbase.QuickbaseAccess;
import ro.sync.exml.plugin.option.OptionPagePluginExtension;
import ro.sync.exml.workspace.api.PluginWorkspace;
import ro.sync.exml.workspace.api.options.WSOptionsStorage;

public class DcsOptionPagePluginExtension extends OptionPagePluginExtension {
	
	public static final String DCS_SERVER_DEFAULT =
			"http://http-dcsarchive.beta.dbb.dk";
	public static final String DCS_SERVER_OPTION = "dk.nota.oxygen.dcs.server";
	
	private JTextField serverUrlField;

	@Override
	public void apply(PluginWorkspace pluginWorkspace) {
		WSOptionsStorage optionsStorage = pluginWorkspace.getOptionsStorage();
		optionsStorage.setOption(DCS_SERVER_OPTION,	serverUrlField.getText());
	}

	@Override
	public String getTitle() {
		return "DCS";
	}

	@Override
	public JComponent init(PluginWorkspace pluginWorkspace) {
		serverUrlField = new JTextField(20);
		// Create overall options panel
		JPanel optionPanel = new JPanel();
		optionPanel.setLayout(new BoxLayout(optionPanel, BoxLayout.Y_AXIS));
		// Create subpanel for DCS settings
		JPanel dcsSettingsPanel = new JPanel();
		dcsSettingsPanel.setBorder(new TitledBorder("DCS settings"));
		GridBagLayout layout = new GridBagLayout();
		dcsSettingsPanel.setLayout(layout);
		JLabel serverUrlLabel = new JLabel("Server URL");
		// Create constant constraints
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.insets = new Insets(3, 3, 3, 3);
		((GridBagLayout)dcsSettingsPanel.getLayout()).setConstraints(
				serverUrlLabel, constraints);
		dcsSettingsPanel.add(serverUrlLabel);
		constraints.weightx = 1.0;
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		((GridBagLayout)dcsSettingsPanel.getLayout()).setConstraints(
				serverUrlField, constraints);
		dcsSettingsPanel.add(serverUrlField);
		// Add DCS settings to overall options
		optionPanel.add(dcsSettingsPanel);
		// Create page panel and add options
		JPanel pagePanel = new JPanel(new BorderLayout());
		pagePanel.add(optionPanel, BorderLayout.NORTH);
		// Populate fields
		WSOptionsStorage optionsStorage = pluginWorkspace.getOptionsStorage();
		serverUrlField.setText(optionsStorage.getOption(DCS_SERVER_OPTION,
				DCS_SERVER_DEFAULT));
		return pagePanel;
	}

	@Override
	public void restoreDefaults() {
		serverUrlField.setText(DCS_SERVER_DEFAULT);
	}

}
