package dk.nota.oxygen.quickbase;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.IOException;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import dk.nota.oxygen.epub.plugin.EpubPluginExtension;
import net.sf.saxon.s9api.SaxonApiException;
import ro.sync.exml.plugin.option.OptionPagePluginExtension;
import ro.sync.exml.workspace.api.PluginWorkspace;
import ro.sync.exml.workspace.api.options.WSOptionsStorage;
import ro.sync.exml.workspace.api.standalone.ui.Button;

public class QuickbaseOptionPagePluginExtension
		extends OptionPagePluginExtension {
	
	private JTextField emailField;
	private JCheckBox enabledCheckbox;
	private JPasswordField passwordField;

	@Override
	public void apply(PluginWorkspace pluginWorkspace) {
		WSOptionsStorage optionsStorage = pluginWorkspace.getOptionsStorage();
		optionsStorage.setOption(EpubPluginExtension.QB_EMAIL_OPTION,
				emailField.getText());
		optionsStorage.setOption(EpubPluginExtension.QB_PASSWORD_OPTION,
				String.valueOf(passwordField.getPassword()));
		optionsStorage.setOption(EpubPluginExtension.QB_ENABLED_OPTION,
				enabledCheckbox.isSelected() ? "true" : "false");
	}
	
	private void addComponentToPanel(JComponent component, JPanel panel,
			GridBagConstraints constraints) {
		((GridBagLayout)panel.getLayout()).setConstraints(component,
				constraints);
		panel.add(component);
	}
	
	private Button createConnectButton() {
		Button connectButton = new Button("Connect");
		connectButton.addActionListener(
				listener -> {
					try {
						EpubPluginExtension.getQuickbaseAccess()
							.connect(emailField.getText(), passwordField
									.getPassword());
						EpubPluginExtension.getQuickbaseMenu().enableActions();
					} catch (IOException | SaxonApiException e) {
						e.printStackTrace();
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
	
	private JPanel createSettingsPanel() {
		JPanel settingsPanel = createPanel("Settings");
		JLabel enabledLabel = new JLabel("Connect to QuickBase at startup");
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
		passwordField = new JPasswordField(20);
		// Assemble panels
		JPanel optionPanel = new JPanel();
		optionPanel.setLayout(new BoxLayout(optionPanel, BoxLayout.Y_AXIS));
		optionPanel.add(createCredentialsPanel());
		optionPanel.add(createSettingsPanel());
		// Workaround to avoid vertical expansion of panels in a BoxLayout
		// TODO: Maybe use GridBagLayout here as well
		JPanel pagePanel = new JPanel(new BorderLayout());
		pagePanel.add(optionPanel, BorderLayout.NORTH);
		// Load options
		WSOptionsStorage optionsStorage = pluginWorkspace.getOptionsStorage();
		emailField.setText(optionsStorage.getOption(EpubPluginExtension
				.QB_EMAIL_OPTION, null));
		passwordField.setText(optionsStorage.getOption(EpubPluginExtension
				.QB_PASSWORD_OPTION, null));
		enabledCheckbox.setSelected(optionsStorage.getOption(EpubPluginExtension
				.QB_ENABLED_OPTION, "false").equals("true"));
		return pagePanel;
	}

	@Override
	public void restoreDefaults() {
		emailField.setText("");
		passwordField.setText("");
		enabledCheckbox.setSelected(false);
	}

}
