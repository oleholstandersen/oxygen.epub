package dk.nota.oxygen.quickbase;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import dk.nota.oxygen.epub.plugin.EpubPluginExtension;
import ro.sync.exml.plugin.option.OptionPagePluginExtension;
import ro.sync.exml.workspace.api.PluginWorkspace;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;
import ro.sync.exml.workspace.api.options.WSOptionsStorage;
import ro.sync.exml.workspace.api.standalone.ui.Button;
import ro.sync.exml.workspace.api.util.UtilAccess;

public class QuickbaseOptionPagePluginExtension
		extends OptionPagePluginExtension {
	
	private JTextField emailField;
	private JCheckBox enabledCheckbox;
	private JTextField mainUrlField;
	private JPasswordField passwordField;
	private JTextField tableUrlField;
	private JTextField tokenField;

	@Override
	public void apply(PluginWorkspace pluginWorkspace) {
		UtilAccess utilAccess = pluginWorkspace.getUtilAccess();
		WSOptionsStorage optionsStorage = pluginWorkspace.getOptionsStorage();
		optionsStorage.setOption(QuickbaseAccess.QB_EMAIL_OPTION, emailField
				.getText());
		optionsStorage.setOption(QuickbaseAccess.QB_PASSWORD_OPTION,
				utilAccess.encrypt(String.valueOf(passwordField
						.getPassword())));
		optionsStorage.setOption(QuickbaseAccess.QB_ENABLED_OPTION,
				enabledCheckbox.isSelected() ? "true" : "false");
		optionsStorage.setOption(QuickbaseAccess.QB_URL_MAIN_OPTION,
				mainUrlField.getText());
		optionsStorage.setOption(QuickbaseAccess.QB_URL_TABLE_OPTION,
				tableUrlField.getText());
		optionsStorage.setOption(QuickbaseAccess.QB_TOKEN_OPTION, utilAccess
				.encrypt(tokenField.getText()));
	}
	
	private void addComponentToPanel(JComponent component, JPanel panel,
			GridBagConstraints constraints) {
		((GridBagLayout)panel.getLayout()).setConstraints(component,
				constraints);
		panel.add(component);
	}
	
	private Button createConnectButton() {
		QuickbaseAccess quickbaseAccess = EpubPluginExtension.getQuickbaseAccess();
		boolean quickbaseConnected = quickbaseAccess.isConnected();
		Button connectButton = new Button(quickbaseConnected ? "Disconnect" :
				"Connect");
		connectButton.addActionListener(
				listener -> {
					try {
						if (quickbaseConnected) quickbaseAccess.disconnect();
						else quickbaseAccess.connect(emailField.getText(),
								passwordField.getPassword());
					} catch (QuickbaseException e) {
						PluginWorkspaceProvider.getPluginWorkspace()
							.showErrorMessage("A QuickBase error occurred", e);
					}
				});
		return connectButton;
	}
	
	private JPanel createCredentialsPanel() {
		JPanel credentialsPanel = createPanel("Connection");
		JLabel emailLabel = new JLabel("Email");
		JLabel passwordLabel = new JLabel("Password");
		Button connectButton = createConnectButton();
		// Create constant constraints
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.insets = new Insets(3, 3, 3, 3);
		// Create individual constraints
		constraints.gridwidth = GridBagConstraints.RELATIVE;
		addComponentToPanel(emailLabel, credentialsPanel, constraints);
		constraints.weightx = 1.0;
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		addComponentToPanel(emailField, credentialsPanel, constraints);
		constraints.weightx = 0.0;
		constraints.gridwidth = GridBagConstraints.RELATIVE;
		addComponentToPanel(passwordLabel, credentialsPanel, constraints);
		constraints.weightx = 1.0;
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		addComponentToPanel(passwordField, credentialsPanel, constraints);
		constraints.fill = GridBagConstraints.NONE;
		constraints.anchor = GridBagConstraints.EAST;
		addComponentToPanel(connectButton, credentialsPanel, constraints);
		return credentialsPanel;
	}
	
	private JPanel createQuickbaseSettingsPanel() {
		JPanel settingsPanel = createPanel("QuickBase settings");
		JLabel mainUrlLabel = new JLabel("Main URL");
		JLabel tableUrlLabel = new JLabel("Table URL");
		JLabel appTokenLabel = new JLabel("App token");
		// Create constant constraints
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.insets = new Insets(3, 3, 3, 3);
		// Create individual constraints
		constraints.weightx = 0.0;
		constraints.gridwidth = GridBagConstraints.RELATIVE;
		addComponentToPanel(mainUrlLabel, settingsPanel, constraints);
		constraints.weightx = 1.0;
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		addComponentToPanel(mainUrlField, settingsPanel, constraints);
		constraints.weightx = 0.0;
		constraints.gridwidth = GridBagConstraints.RELATIVE;
		addComponentToPanel(tableUrlLabel, settingsPanel, constraints);
		constraints.weightx = 1.0;
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		addComponentToPanel(tableUrlField, settingsPanel, constraints);
		constraints.weightx = 0.0;
		constraints.gridwidth = GridBagConstraints.RELATIVE;
		addComponentToPanel(appTokenLabel, settingsPanel, constraints);
		constraints.weightx = 1.0;
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		addComponentToPanel(tokenField, settingsPanel, constraints);
		return settingsPanel;
	}
	
	private JPanel createUserSettingsPanel() {
		JPanel settingsPanel = createPanel("Local settings");
		JLabel enabledLabel = new JLabel("Connect to QuickBase when oXygen starts");
		// Create constant constraints
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.insets = new Insets(3, 3, 3, 3);
		// Create individual constraints
		constraints.weightx = 0.0;
		constraints.gridwidth = GridBagConstraints.RELATIVE;
		addComponentToPanel(enabledCheckbox, settingsPanel, constraints);
		constraints.weightx = 1.0;
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		addComponentToPanel(enabledLabel, settingsPanel, constraints);
		return settingsPanel;
	}
	
	private JPanel createPanel(String borderTitle) {
		JPanel panel = new JPanel();
		panel.setBorder(new TitledBorder(borderTitle));
		GridBagLayout layout = new GridBagLayout();
		panel.setLayout(layout);
		return panel;
	}

	@Override
	public String getTitle() {
		return "QuickBase";
	}

	@Override
	public JComponent init(PluginWorkspace pluginWorkspace) {
		emailField = new JTextField(20);
		enabledCheckbox = new JCheckBox();
		mainUrlField = new JTextField(20);
		passwordField = new JPasswordField(20);
		tableUrlField = new JTextField(20);
		tokenField = new JTextField(20);
		// Assemble panels
		JPanel optionPanel = new JPanel();
		optionPanel.setLayout(new BoxLayout(optionPanel, BoxLayout.Y_AXIS));
		optionPanel.add(createCredentialsPanel());
		optionPanel.add(createUserSettingsPanel());
		optionPanel.add(createQuickbaseSettingsPanel());
		// Workaround to avoid vertical expansion of panels in a BoxLayout
		// TODO: Maybe use GridBagLayout here as well
		JPanel pagePanel = new JPanel(new BorderLayout());
		pagePanel.add(optionPanel, BorderLayout.NORTH);
		// Load options
		UtilAccess utilAccess = pluginWorkspace.getUtilAccess();
		WSOptionsStorage optionsStorage = pluginWorkspace.getOptionsStorage();
		emailField.setText(optionsStorage.getOption(QuickbaseAccess
				.QB_EMAIL_OPTION, ""));
		passwordField.setText(utilAccess.decrypt(optionsStorage.getOption(
				QuickbaseAccess.QB_PASSWORD_OPTION, "")));
		enabledCheckbox.setSelected(optionsStorage.getOption(QuickbaseAccess
				.QB_ENABLED_OPTION, "false").equals("true"));
		mainUrlField.setText(optionsStorage.getOption(QuickbaseAccess
				.QB_URL_MAIN_OPTION, "https://cnpxml.quickbase.com/db/main"));
		tableUrlField.setText(optionsStorage.getOption(QuickbaseAccess
				.QB_URL_TABLE_OPTION, "https://cnpxml.quickbase.com/db/bjcv74iq3"));
		tokenField.setText(utilAccess.decrypt(optionsStorage.getOption(
				QuickbaseAccess.QB_TOKEN_OPTION, "")));
		return pagePanel;
	}

	@Override
	public void restoreDefaults() {
		emailField.setText("");
		passwordField.setText("");
		enabledCheckbox.setSelected(false);
		mainUrlField.setText("https://cnpxml.quickbase.com/db/main");
		tableUrlField.setText("https://cnpxml.quickbase.com/db/bjcv74iq3");
		tokenField.setText("");
	}

}
