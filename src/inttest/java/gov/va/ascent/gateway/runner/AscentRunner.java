package gov.va.ascent.gateway.runner;

import org.junit.runner.RunWith;
import org.testng.annotations.BeforeSuite;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import cucumber.api.testng.AbstractTestNGCucumberTests;
import gov.va.ascent.framework.log.AscentLogger;
import gov.va.ascent.framework.log.AscentLoggerFactory;

@RunWith(Cucumber.class)
@CucumberOptions(strict = false, plugin = { "pretty",
		"html:target/site/cucumber-pretty", "json:target/cucumber.json" },
		features = { "src/inttest/resources/gov/va/ascent/gateway/feature" },
		glue = { "gov.va.ascent.gateway.steps" })
public class AscentRunner extends AbstractTestNGCucumberTests {

	final AscentLogger LOGGER = AscentLoggerFactory.getLogger(AscentRunner.class);

	@BeforeSuite(alwaysRun = true)
	public void setUp() throws Exception {
		LOGGER.debug("setUp method");
	}

}
