package gov.va.ascent.gateway.util;

import gov.va.ascent.test.framework.service.RESTConfigService;

public class GatewayAppUtil {
	
	private GatewayAppUtil() {

	}
	
	public static String getBaseURL() {
		return RESTConfigService.getBaseURL("data.'username'", "data.'password'");
	}
}


