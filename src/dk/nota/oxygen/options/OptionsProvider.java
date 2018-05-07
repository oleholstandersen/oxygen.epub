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
	
	public static Object getOptionValue(String option) {
		return PluginWorkspaceProvider.getPluginWorkspace().getOptionsStorage()
				.getOption(option, null);
	}
	
	public static String getDecryptedOptionValue(String option) {
		Object encryptedValue = getOptionValue(option);
		if (!(encryptedValue instanceof String))
			throw new IllegalArgumentException(String.format(
					"The value of option %s is not a string", option));
		return PluginWorkspaceProvider.getPluginWorkspace().getUtilAccess()
				.decrypt((String)encryptedValue);
	}

}
