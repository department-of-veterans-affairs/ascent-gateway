package gov.va.ascent.gateway.steps;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cucumber.api.Scenario;
import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import gov.va.ascent.gateway.util.GatewayAppUtil;
import gov.va.ascent.test.framework.restassured.BaseStepDef;

public class GatewayStatus extends BaseStepDef {

	final Logger log = LoggerFactory.getLogger(GatewayStatus.class);

	@Before({ "@gatewaystatus" })
	public void setUpREST() {
		initREST();
	}

	@Given("^I pass the header information for gateway service$")
	public void passTheHeaderInformationForDashboard(Map<String, String> tblHeader) throws Throwable {
		passHeaderInformation(tblHeader);
	}

	@When("^user makes a request to gateway \"([^\"]*)\"$")
	public void makerequesusttogatewaylGet(String strURL) throws Throwable {
		invokeAPIUsingGet(GatewayAppUtil.getBaseURL() + strURL, false);
	}

	@Then("^the response code must be for gateway service (\\d+)$")
	public void serviceresposestatuscodemustbe(int intStatusCode) throws Throwable {
		validateStatusCode(intStatusCode);
	}

	@After({ "@gatewaystatus" })
	public void cleanUp(Scenario scenario) {
		postProcess(scenario);
	}

}
