package gov.va.ascent.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.cloud.sleuth.sampler.AlwaysSampler;
import org.springframework.context.annotation.Bean;

import gov.va.ascent.framework.log.AscentLogger;
import gov.va.ascent.framework.log.AscentLoggerFactory;
import gov.va.ascent.gateway.filter.AscentGatewayAuditFilter;
import gov.va.ascent.gateway.filter.AscentGatewayErrorFilter;
import gov.va.ascent.gateway.filter.AscentGatewayPostFilter;
import gov.va.ascent.gateway.filter.AscentGatewayPreFilter;

/**
 * An <tt>Ascent Gateway Server</tt> enabled for Spring Boot Application,
 * Spring Cloud Netflix Zuul proxy server and Hystrix circuit breakers.
 *
 */
@SpringBootApplication
@EnableZuulProxy
@EnableDiscoveryClient // enables spring-cloud-config client
@EnableHystrix
public class AscentGatewayApplication {

	private static final AscentLogger LOGGER = AscentLoggerFactory.getLogger(AscentGatewayApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(AscentGatewayApplication.class, args);
	}

	@Bean
	public AscentGatewayPreFilter preFilter() {
		return new AscentGatewayPreFilter();
	}

	@Bean
	public AscentGatewayErrorFilter errorFilter() {
		return new AscentGatewayErrorFilter();
	}

	@Bean
	public AscentGatewayPostFilter postFilter() {
		return new AscentGatewayPostFilter();
	}

	@Bean
	public AscentGatewayAuditFilter auditFilter() {
		return new AscentGatewayAuditFilter();
	}

	@Bean
	AlwaysSampler defaultSampler() {
		LOGGER.info("defaultSampler invoked");
		return new AlwaysSampler();
	}

}
