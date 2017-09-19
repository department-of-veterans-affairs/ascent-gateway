package gov.va.ascent.gateway.filter;

import com.netflix.zuul.context.RequestContext;
import gov.va.ascent.gateway.audit.AscentGatewayAuditHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.netflix.zuul.filters.ZuulProperties;
import org.springframework.cloud.netflix.zuul.filters.discovery.DiscoveryClientRouteLocator;
import org.springframework.cloud.sleuth.Span;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.ByteArrayInputStream;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.POST_TYPE;

@RunWith(SpringRunner.class)
public class AscentGatewayAuditFilterTest {

    private AscentGatewayAuditFilter filter;

    @MockBean
    Tracer tracer;

    @MockBean
    AscentGatewayAuditHelper auditHelper;

    @Mock
    private DiscoveryClient discovery;

    private ZuulProperties properties = new ZuulProperties();

    private DiscoveryClientRouteLocator routeLocator;

    private MockHttpServletRequest request = new MockHttpServletRequest();

    @Before
    public void init() {
        initMocks(this);
        this.properties = new ZuulProperties();
        this.routeLocator = new DiscoveryClientRouteLocator("/", this.discovery,
                this.properties);
        this.filter = new AscentGatewayAuditFilter();
        this.filter.tracer = tracer;
        this.filter.auditHelper = auditHelper;
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

        assertEquals(-1, this.filter.filterOrder());
        assertEquals(POST_TYPE, this.filter.filterType());
    }

    @Test
    public void shouldFilterFalseContainsContentResponsesNull() throws Exception {
        // Setup
        RequestContext ctx = RequestContext.getCurrentContext();

        // contentContains Method - return false
        ctx.setResponseDataStream(null);
        ctx.setResponseBody(null);

        // supportsAuditType return true
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod(HttpMethod.POST.name());
        ctx.addOriginResponseHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON.toString());

        //httpStatusSuccessful Method - return true
        MockHttpServletResponse response = new MockHttpServletResponse();
        response.setStatus(200);

        ctx.setRequest(request);
        ctx.setResponse(response);


        assertEquals(false, this.filter.shouldFilter());
    }

    @Test
    public void shouldFilterTrueContainsContentResponseBodyString() throws Exception {
        // Setup
        RequestContext ctx = RequestContext.getCurrentContext();

        // contentContains Method - return true
        ctx.setResponseDataStream(null);
        ctx.setResponseBody("String");

        // supportsAuditType return true
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod(HttpMethod.POST.name());
        ctx.addOriginResponseHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON.toString());

        //httpStatusSuccessful Method - return true
        MockHttpServletResponse response = new MockHttpServletResponse();
        response.setStatus(200);

        ctx.setRequest(request);
        ctx.setResponse(response);


        assertEquals(true, this.filter.shouldFilter());
    }

    @Test
    public void shouldFilterTrueContainsContentResponseStreamData() throws Exception {
        // Setup
        RequestContext ctx = RequestContext.getCurrentContext();

        // contentContains Method - return true
        ctx.setResponseDataStream(new ByteArrayInputStream("String".getBytes()));
        ctx.setResponseBody(null);

        // supportsAuditType return true
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod(HttpMethod.POST.name());
        ctx.addOriginResponseHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON.toString());

        //httpStatusSuccessful Method - return true
        MockHttpServletResponse response = new MockHttpServletResponse();
        response.setStatus(200);

        ctx.setRequest(request);
        ctx.setResponse(response);


        assertEquals(true, this.filter.shouldFilter());
    }

    @Test
    public void shouldFilterTrueSupportsAuditTypeHeaderPairOne() throws Exception {
        // Setup
        RequestContext ctx = RequestContext.getCurrentContext();

        // contentContains Method - return true
        ctx.setResponseDataStream(new ByteArrayInputStream("String".getBytes()));
        ctx.setResponseBody(null);

        // supportsAuditType return true
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod(HttpMethod.POST.name());
        ctx.addOriginResponseHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_UTF8_VALUE.toString());

        //httpStatusSuccessful Method - return true
        MockHttpServletResponse response = new MockHttpServletResponse();
        response.setStatus(200);

        ctx.setRequest(request);
        ctx.setResponse(response);


        assertEquals(true, this.filter.shouldFilter());
    }

    @Test
    public void shouldFilterTrueSupportsAuditTypeHeaderPairTwo() throws Exception {
        // Setup
        RequestContext ctx = RequestContext.getCurrentContext();

        // contentContains Method - return true
        ctx.setResponseDataStream(new ByteArrayInputStream("String".getBytes()));
        ctx.setResponseBody(null);

        // supportsAuditType return true
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod(HttpMethod.POST.name());
        ctx.addOriginResponseHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_XML_VALUE.toString());

        //httpStatusSuccessful Method - return true
        MockHttpServletResponse response = new MockHttpServletResponse();
        response.setStatus(200);

        ctx.setRequest(request);
        ctx.setResponse(response);


        assertEquals(true, this.filter.shouldFilter());
    }

    @Test
    public void shouldFilterTrueSupportsAuditTypeHeaderPairThree() throws Exception {
        // Setup
        RequestContext ctx = RequestContext.getCurrentContext();

        // contentContains Method - return true
        ctx.setResponseDataStream(new ByteArrayInputStream("String".getBytes()));
        ctx.setResponseBody(null);

        // supportsAuditType return true
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod(HttpMethod.POST.name());
        ctx.addOriginResponseHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_ATOM_XML_VALUE.toString());

        //httpStatusSuccessful Method - return true
        MockHttpServletResponse response = new MockHttpServletResponse();
        response.setStatus(200);

        ctx.setRequest(request);
        ctx.setResponse(response);


        assertEquals(true, this.filter.shouldFilter());
    }

    @Test
    public void shouldFilterFalseSupportsAuditTypeHeaderPairFour() throws Exception {
        // Setup
        RequestContext ctx = RequestContext.getCurrentContext();

        // contentContains Method - return true
        ctx.setResponseDataStream(new ByteArrayInputStream("String".getBytes()));
        ctx.setResponseBody(null);

        // supportsAuditType return false
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod(HttpMethod.POST.name());
        ctx.addOriginResponseHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PDF.toString());

        //httpStatusSuccessful Method - return true
        MockHttpServletResponse response = new MockHttpServletResponse();
        response.setStatus(200);

        ctx.setRequest(request);
        ctx.setResponse(response);


        assertEquals(false, this.filter.shouldFilter());
    }

    @Test
    public void shouldFilterFalseSupportsAuditMethodGet() throws Exception {
        // Setup
        RequestContext ctx = RequestContext.getCurrentContext();

        // contentContains Method - return true
        ctx.setResponseDataStream(new ByteArrayInputStream("String".getBytes()));
        ctx.setResponseBody(null);

        // supportsAuditType return false
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod(HttpMethod.GET.name());
        ctx.addOriginResponseHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_ATOM_XML_VALUE.toString());

        //httpStatusSuccessful Method - return true
        MockHttpServletResponse response = new MockHttpServletResponse();
        response.setStatus(200);

        ctx.setRequest(request);
        ctx.setResponse(response);


        assertEquals(false, this.filter.shouldFilter());
    }

    @Test
    public void shouldFilterTrueHttpStatusSuccessfulZero() throws Exception {
        // Setup
        RequestContext ctx = RequestContext.getCurrentContext();

        // contentContains Method - return true
        ctx.setResponseDataStream(new ByteArrayInputStream("String".getBytes()));
        ctx.setResponseBody(null);

        // supportsAuditType return true
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod(HttpMethod.POST.name());
        ctx.addOriginResponseHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_ATOM_XML_VALUE.toString());

        //httpStatusSuccessful Method - return false
        MockHttpServletResponse response = new MockHttpServletResponse();
        response.setStatus(0);

        ctx.setRequest(request);
        ctx.setResponse(response);


        assertEquals(true, this.filter.shouldFilter());
    }

    @Test
    public void shouldFilterTrueHttpStatusSuccessfulSuccess() throws Exception {
        // Setup
        RequestContext ctx = RequestContext.getCurrentContext();

        // contentContains Method - return true
        ctx.setResponseDataStream(new ByteArrayInputStream("String".getBytes()));
        ctx.setResponseBody(null);

        // supportsAuditType return true
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod(HttpMethod.POST.name());
        ctx.addOriginResponseHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_ATOM_XML_VALUE.toString());

        //httpStatusSuccessful Method - return true
        MockHttpServletResponse response = new MockHttpServletResponse();
        response.setStatus(200);

        ctx.setRequest(request);
        ctx.setResponse(response);


        assertEquals(true, this.filter.shouldFilter());
    }

    @Test
    public void shouldFilterTrueHttpStatusSuccessfulRedirect() throws Exception {
        // Setup
        RequestContext ctx = RequestContext.getCurrentContext();

        // contentContains Method - return true
        ctx.setResponseDataStream(new ByteArrayInputStream("String".getBytes()));
        ctx.setResponseBody(null);

        // supportsAuditType return true
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod(HttpMethod.POST.name());
        ctx.addOriginResponseHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_ATOM_XML_VALUE.toString());

        //httpStatusSuccessful Method - return true
        MockHttpServletResponse response = new MockHttpServletResponse();
        response.setStatus(304);

        ctx.setRequest(request);
        ctx.setResponse(response);


        assertEquals(true, this.filter.shouldFilter());
    }

    @Test
    public void shouldFilterTrueHttpStatusSuccessfulError() throws Exception {
        // Setup
        RequestContext ctx = RequestContext.getCurrentContext();

        // contentContains Method - return true
        ctx.setResponseDataStream(new ByteArrayInputStream("String".getBytes()));
        ctx.setResponseBody(null);

        // supportsAuditType return true
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod(HttpMethod.POST.name());
        ctx.addOriginResponseHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_ATOM_XML_VALUE.toString());

        //httpStatusSuccessful Method - return false
        MockHttpServletResponse response = new MockHttpServletResponse();
        response.setStatus(500);

        ctx.setRequest(request);
        ctx.setResponse(response);


        assertEquals(true, this.filter.shouldFilter());
    }

    @Test
    public void runTestErrorTag() throws Exception {
        RequestContext ctx = RequestContext.getCurrentContext();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod(HttpMethod.POST.name());

        MockHttpServletResponse response = new MockHttpServletResponse();
        response.setStatus(0);
        ctx.setRequest(request);
        ctx.setResponse(response);
        ctx.addOriginResponseHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON.toString());
        ctx.setResponseBody("Response Body");
        doNothing().when(tracer).addTag(any(String.class), any(String.class));
        this.filter.run();
        verify(tracer, times(1)).addTag(eq(Span.SPAN_ERROR_TAG_NAME),any(String.class));

    }

    @Test
    public void runTestSpanHttpRequestTag() throws Exception {
        RequestContext ctx = RequestContext.getCurrentContext();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod(HttpMethod.POST.name());
        request.setContent("Some test content".getBytes());
        MockHttpServletResponse response = new MockHttpServletResponse();
        response.setStatus(0);
        ctx.setRequest(request);
        ctx.setResponse(response);
        ctx.addOriginResponseHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON.toString());
        ctx.setResponseBody("Response Body");
        doNothing().when(tracer).addTag(any(String.class), any(String.class));
        this.filter.run();
        verify(tracer, times(1)).addTag(eq("http.request"),any(String.class));

    }

    @Test
    public void runTestSpanHttpResponseTag() throws Exception {
        RequestContext ctx = RequestContext.getCurrentContext();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod(HttpMethod.POST.name());
        request.setContent("Some test content".getBytes());
        MockHttpServletResponse response = new MockHttpServletResponse();
        response.setStatus(200);

        ctx.setRequest(request);
        ctx.setResponse(response);
        ctx.addOriginResponseHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON.toString());
        ctx.setResponseBody("Response Body");
        ctx.setResponseDataStream(new ByteArrayInputStream("mytest string of response content".getBytes()));
        doNothing().when(tracer).addTag(any(String.class), any(String.class));
        this.filter.run();
        verify(tracer, times(1)).addTag(eq("http.response"),any(String.class));
        verify(auditHelper, times(1)).addTracerTagsAsync(any(String.class));
    }

}
