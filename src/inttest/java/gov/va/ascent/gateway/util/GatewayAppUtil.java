package gov.va.ascent.gateway.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import gov.va.ascent.test.framework.service.RESTConfigService;
import gov.va.ascent.test.framework.service.VaultService;
import gov.va.ascent.test.framework.util.AppConstants;
import gov.va.ascent.test.framework.util.RESTUtil;

public class GatewayAppUtil {

	private static final Pattern urlPattern = Pattern.compile("(http|https)://([A-Za-z0-9\\-\\.]+)(:(\\d+))?$");

	private GatewayAppUtil() {

	}

	public static String getBaseURL() {
		RESTConfigService restConfig = RESTConfigService.getInstance();
		String baseURL = restConfig.getPropertyName("baseURL", true);

		String vaultToken = System.getProperty(AppConstants.VAULT_TOKEN_PARAM_NAME);
		if (vaultToken != null && vaultToken != "") {
			String jsonResponse = VaultService.getVaultCredentials(vaultToken);

			RESTUtil restUtil = new RESTUtil();
			String userName = restUtil.parseJSON(jsonResponse, "data.'ascent.security.username'");
			String password = restUtil.parseJSON(jsonResponse, "data.'ascent.security.password'");

			final Matcher m = urlPattern.matcher(baseURL);
			if (!m.matches())
				throw new RuntimeException("Invalid base url!");
			final String protocol = m.group(1).toLowerCase();
			final String host = m.group(2);
			final String port     = m.group(3);
			StringBuffer urlBuffer = new StringBuffer();
			urlBuffer.append(protocol).append("://").append(userName).append(":").append(password).append("@").append(host);
			if(port !=null) {
				urlBuffer.append(port);
			}
			//String finalUrl = protocol + "://" + userName + ":" + password + "@" + host + port ;
			return urlBuffer.toString();
		}
		return baseURL;
	}
}
