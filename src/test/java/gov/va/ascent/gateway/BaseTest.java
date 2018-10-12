package gov.va.ascent.gateway;

import org.junit.After;

import io.prometheus.client.CollectorRegistry;

public class BaseTest {

	/**
	 * Clear the metrics registry. If it is not cleared, then tests error out due to multiple contexts created during test
	 * case execution. Errors such as the following will be seen if the registry is not cleared.
	 * 
	 * java.lang.IllegalArgumentException: Collector already registered that provides name: jvm_buffer_count
	 */
	@After
	public void tearDown() {
		CollectorRegistry.defaultRegistry.clear();
	}

}
