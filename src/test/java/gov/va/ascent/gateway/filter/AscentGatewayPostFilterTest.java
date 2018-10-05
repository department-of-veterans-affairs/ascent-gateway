package gov.va.ascent.gateway.filter;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.POST_TYPE;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.slf4j.event.Level;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.netflix.zuul.filters.ProxyRequestHelper;
import org.springframework.cloud.netflix.zuul.filters.ZuulProperties;
import org.springframework.cloud.netflix.zuul.filters.discovery.DiscoveryClientRouteLocator;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.junit4.SpringRunner;

import com.netflix.zuul.context.RequestContext;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Appender;
import gov.va.ascent.framework.log.AscentLogger;
import gov.va.ascent.framework.log.AscentLoggerFactory;

@RunWith(SpringRunner.class)
public class AscentGatewayPostFilterTest {

	private AscentGatewayPostFilter filter;

	@Mock
	private DiscoveryClient discovery;

	private ZuulProperties properties = new ZuulProperties();

	private DiscoveryClientRouteLocator routeLocator;

	private final MockHttpServletRequest request = new MockHttpServletRequest();

	private final ProxyRequestHelper proxyRequestHelper = new ProxyRequestHelper();

	@SuppressWarnings("rawtypes")
	@Mock
	private Appender<ILoggingEvent> mockAppender;
	// Captor is genericised with ch.qos.logback.classic.spi.LoggingEvent
	@Captor
	private ArgumentCaptor<LoggingEvent> captorLoggingEvent;

	@SuppressWarnings("unchecked")
	// It's not quite necessary but it also shows you how it can be done
	@Before
	public void init() {
		final AscentLogger logger = AscentLoggerFactory.getLogger(AscentGatewayPostFilter.class);
		logger.setLevel(Level.DEBUG);
		logger.getLoggerBoundImpl().addAppender(mockAppender);
		this.properties = new ZuulProperties();
		this.routeLocator = new DiscoveryClientRouteLocator("/", this.discovery,
				this.properties);
		this.filter = new AscentGatewayPostFilter();
		final RequestContext ctx = RequestContext.getCurrentContext();
		ctx.clear();
		ctx.setRequest(this.request);
	}

	@After
	public void clear() {
		RequestContext.getCurrentContext().clear();
	}

	@Test
	public void basicProperties() throws Exception {
		assertEquals(0, this.filter.filterOrder());
		assertEquals(POST_TYPE, this.filter.filterType());
		assertEquals(true, this.filter.shouldFilter());
	}

	@Test
	public void testRun() throws Exception {
		request.addHeader("TestRequestHeader", "TestReqHeaderValue");
		final MockHttpServletResponse response = new MockHttpServletResponse();
		response.setHeader("TestHeader", "TestHeader");
		RequestContext.getCurrentContext().setResponse(response);
		final Object obj = this.filter.run();
		// Now verify our logging interactions
		verify(mockAppender, times(1)).doAppend(captorLoggingEvent.capture());
		// Having a genricised captor means we don't need to cast

		final List<LoggingEvent> loggingEvents = captorLoggingEvent.getAllValues();
		// Check log level is correct
		assertThat(loggingEvents.get(0).getLevel().toString(), is(Level.INFO.name()));
		assertTrue(loggingEvents.get(0).getMessage().startsWith("Completed Request "));
		assertEquals(null, obj);
	}

	@Test
	public void testRunDebugOff() throws Exception {
		final AscentLogger logger = AscentLoggerFactory.getLogger(AscentGatewayPostFilter.class);
		logger.setLevel(Level.ERROR);
		final MockHttpServletResponse response = new MockHttpServletResponse();
		response.setHeader("TestHeader", "TestHeader");
		RequestContext.getCurrentContext().setResponse(response);
		final Object obj = this.filter.run();

		verify(mockAppender, times(0)).doAppend(captorLoggingEvent.capture());
		// Having a genricised captor means we don't need to cast

		// Check log level is correct
		assertEquals(null, obj);
	}
}
