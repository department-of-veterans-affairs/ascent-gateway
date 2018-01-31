package gov.va.ascent.gateway.steps;

import static org.junit.Assert.assertEquals;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cucumber.api.Scenario;
import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

import gov.va.ascent.gateway.util.GatewayAppUtil;
import gov.va.ascent.test.framework.restassured.BaseStepDef;

public class GatewayRoute extends BaseStepDef {

	final Logger log = LoggerFactory.getLogger(GatewayRoute.class);

	@Before({ "@gatewayroute" })
	public void setUpREST() {
		initREST();
	}

	@Given("^I pass the header information for gateway route service$")
	public void passTheHeaderInformationForDashboard(Map<String, String> tblHeader) throws Throwable {
		passHeaderInformation(tblHeader);
	}

	@When("^user makes a request to gateway route \"([^\"]*)\"$")
	public void makerequesusttogatewaylGet(String strURL) throws Throwable {
		invokeAPIUsingGet(GatewayAppUtil.getBaseURL() + strURL, false);

	}

	@And("^the response code must be for route service (\\d+)$")
	public void serviceresposestatuscodemustbe(int intStatusCode) throws Throwable {
		validateStatusCode(intStatusCode);
	}

	@Then("^assert the \"([^\"]*)\" refdata route \"([^\"]*)\"$")
	public void assertthepropertyrefdata(String propertyName, String propertyValue) throws Throwable {
		String value = resUtil.parseJSON(strResponse, propertyName);
		assertEquals(propertyValue, value);
	}

	@After({ "@gatewayroute" })
	public void cleanUp(Scenario scenario) {
		postProcess(scenario);
	}

}
