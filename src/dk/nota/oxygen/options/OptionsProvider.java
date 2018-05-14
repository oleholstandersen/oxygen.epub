package dk.nota.oxygen.options;

import ro.sync.exml.workspace.api.PluginWorkspaceProvider;

public class OptionsProvider {
	
	public static final String DCS_SERVER_OPTION = "dk.nota.oxygen.dcs.server";
	
	public static final String QB_AUTO_OPTION =
			"dk.nota.oxygen.quickbase.auto";
	public static final String QB_EMAIL_OPTION =
			"dk.nota.oxygen.quickbase.useremail";
	public static final String QB_PASSWORD_OPTION =
			"dk.nota.oxygen.quickbase.password";
	public static final String QB_TOKEN_OPTION =
			"dk.nota.oxygen.quickbase.token";
	public static final String QB_URL_MAIN_OPTION =
			"dk.nota.oxygen.quickbase.url.main";
	public static final String QB_URL_TABLE_OPTION =
			"dk.nota.oxygen.quickbase.url.table";
	
	public static String getOptionValue(String option) {
		return getOptionValue(option, null);
	}
	
	public static String getOptionValue(String option, String defaultValue) {
		return PluginWorkspaceProvider.getPluginWorkspace().getOptionsStorage()
				.getOption(option, null);
	}
	
	public static String getDecryptedOptionValue(String option) {
		return getOptionValue(option, null);
	}
	
	public static String getDecryptedOptionValue(String option,
			String defaultValue) {
		String value = getOptionValue(option, defaultValue);
		if (value == defaultValue) return value;
		return PluginWorkspaceProvider.getPluginWorkspace().getUtilAccess()
				.decrypt(value);
	}
	
	public static void setEncryptedOptionValue(String option, String value) {
		String encryptedValue = PluginWorkspaceProvider.getPluginWorkspace()
				.getUtilAccess().encrypt(value);
		setOptionValue(option, encryptedValue);
	}
	
	public static void setOptionValue(String option, String value) {
		PluginWorkspaceProvider.getPluginWorkspace().getOptionsStorage()
			.setOption(option, value);
	}

}
