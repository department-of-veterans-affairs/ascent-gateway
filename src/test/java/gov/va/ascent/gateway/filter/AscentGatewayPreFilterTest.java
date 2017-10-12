package gov.va.ascent.gateway.filter;

import static org.junit.Assert.assertEquals;
import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.PRE_TYPE;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import com.netflix.zuul.context.RequestContext;


public class AscentGatewayPreFilterTest {

    private AscentGatewayPreFilter filter;

    private MockHttpServletRequest request = new MockHttpServletRequest();

    @Before
    public void init() {
        this.filter = new AscentGatewayPreFilter();
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
        assertEquals(PRE_TYPE, this.filter.filterType());
        assertEquals(true, this.filter.shouldFilter());
    }

    @Test
    public void runTestNoPersonTraits() throws Exception {
        Object obj = this.filter.run();
        assertEquals(null, obj);
    }

}
