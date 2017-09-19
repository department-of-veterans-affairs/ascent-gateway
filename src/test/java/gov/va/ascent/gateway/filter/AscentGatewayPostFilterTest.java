package gov.va.ascent.gateway.filter;

import com.netflix.zuul.context.RequestContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.netflix.zuul.filters.ProxyRequestHelper;
import org.springframework.cloud.netflix.zuul.filters.ZuulProperties;
import org.springframework.cloud.netflix.zuul.filters.discovery.DiscoveryClientRouteLocator;
import org.springframework.mock.web.MockHttpServletRequest;

import javax.ws.rs.POST;

import static org.junit.Assert.assertEquals;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.ERROR_TYPE;
import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.POST_TYPE;

public class AscentGatewayPostFilterTest {

    private AscentGatewayPostFilter filter;

    @Mock
    private DiscoveryClient discovery;

    private ZuulProperties properties = new ZuulProperties();

    private DiscoveryClientRouteLocator routeLocator;

    private MockHttpServletRequest request = new MockHttpServletRequest();

    private ProxyRequestHelper proxyRequestHelper = new ProxyRequestHelper();

    @Before
    public void init() {
        initMocks(this);
        this.properties = new ZuulProperties();
        this.routeLocator = new DiscoveryClientRouteLocator("/", this.discovery,
                this.properties);
        this.filter = new AscentGatewayPostFilter();
        RequestContext ctx = RequestContext.getCurrentContext();
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
        Object obj = this.filter.run();
        assertEquals(null, obj);
    }
}
