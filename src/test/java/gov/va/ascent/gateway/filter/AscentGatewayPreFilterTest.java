package gov.va.ascent.gateway.filter;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.PRE_TYPE;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Appender;
import gov.va.ascent.framework.security.PersonTraits;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockHttpServletRequest;

import com.netflix.zuul.context.RequestContext;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;


@RunWith(SpringRunner.class)
@ContextConfiguration
public class AscentGatewayPreFilterTest {

    @SuppressWarnings("rawtypes")
    @Mock
    private Appender mockAppender;
    // Captor is genericised with ch.qos.logback.classic.spi.LoggingEvent
    @Captor
    private ArgumentCaptor<LoggingEvent> captorLoggingEvent;


    private AscentGatewayPreFilter filter;

    private MockHttpServletRequest request = new MockHttpServletRequest();
    

    @Before
    public void init() {
        PersonTraits personTraits = new PersonTraits("user", "password",
                AuthorityUtils.createAuthorityList("ROLE_TEST"));
        Authentication auth = new UsernamePasswordAuthenticationToken(personTraits,
                personTraits.getPassword(), personTraits.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
        final Logger logger = (Logger) LoggerFactory.getLogger(AscentGatewayPreFilter.class);
        logger.setLevel(Level.DEBUG);
        logger.addAppender(mockAppender);
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
    	
    	Logger root = (Logger)LoggerFactory.getLogger(this.getClass());
    	Level originalLevel = root.getLevel();
    	root.setLevel(Level.DEBUG);
    	
        Object obj = this.filter.run();
        verify(mockAppender, times(1)).doAppend(captorLoggingEvent.capture());
        final List<LoggingEvent> loggingEvents = captorLoggingEvent.getAllValues();
        Assert.assertThat(loggingEvents.get(0).getLevel(), is(Level.INFO));
        assertEquals("Zuul Filter:   request to http://localhost", loggingEvents.get(0).getMessage());
        assertEquals(null, obj);
    }

    @Test
    @WithMockUser
    public void runTestPersonTraits() throws Exception {
        request.addHeader("TestHeader", "TestReqHeaderValue");
        Object obj = this.filter.run();
        verify(mockAppender, times(1)).doAppend(captorLoggingEvent.capture());
        final List<LoggingEvent> loggingEvents = captorLoggingEvent.getAllValues();
        assertEquals("Zuul Filter:   request to http://localhost", loggingEvents.get(0).getMessage());
        assertEquals(null, obj);
    }

    @Test
    @WithMockUser
    public void runTestPersonTraitsNullDebuggerOff() throws Exception {
        final Logger logger = (Logger) LoggerFactory.getLogger(AscentGatewayPreFilter.class);
        logger.setLevel(Level.ERROR);
        Authentication auth = new UsernamePasswordAuthenticationToken(null,
                null, null);
        SecurityContextHolder.getContext().setAuthentication(auth);
        Object obj = this.filter.run();
        verify(mockAppender, times(0)).doAppend(captorLoggingEvent.capture());
        assertEquals(null, obj);

    }
    

}
