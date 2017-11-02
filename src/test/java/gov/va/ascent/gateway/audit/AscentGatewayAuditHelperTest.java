package gov.va.ascent.gateway.audit;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Appender;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class AscentGatewayAuditHelperTest {

    @Mock
    Tracer tracer;

    @InjectMocks
    AscentGatewayAuditHelper ascentGatewayAuditHelper;

    @SuppressWarnings("rawtypes")
    @Mock
    private Appender mockAppender;
    // Captor is genericised with ch.qos.logback.classic.spi.LoggingEvent
    @Captor
    private ArgumentCaptor<LoggingEvent> captorLoggingEvent;

    // added the mockAppender to the root logger
    @SuppressWarnings("unchecked")
    // It's not quite necessary but it also shows you how it can be done
    @Before
    public void setup() {
        final Logger logger = (Logger) LoggerFactory.getLogger(AscentGatewayAuditHelper.class);
        logger.setLevel(Level.DEBUG);
        logger.addAppender(mockAppender);
    }

    // Always have this teardown otherwise we can stuff up our expectations.
    // Besides, it's
    // good coding practice
    @SuppressWarnings("unchecked")
    @After
    public void teardown() {
        final Logger logger = (Logger) LoggerFactory.getLogger(AscentGatewayAuditHelper.class);
        logger.detachAppender(mockAppender);
    }

    @Test
    public void addTracerTagsAsyncTestInvalidJSON() {
        ascentGatewayAuditHelper.addTracerTagsAsync("({response: 'mytext'})");
        // Now verify our logging interactions
        verify(mockAppender, times(2)).doAppend(captorLoggingEvent.capture());
        // Having a genricised captor means we don't need to cast

        final List<LoggingEvent> loggingEvents = captorLoggingEvent.getAllValues();
        // Check log level is correct
        assertThat(loggingEvents.get(0).getLevel(), is(Level.WARN));
        assertThat(loggingEvents.get(1).getLevel(), is(Level.WARN));
        assertThat(loggingEvents.get(0).getFormattedMessage(), is("Not a valid Json String: com.google.gson.stream.MalformedJsonException: Use JsonReader.setLenient(true) to accept malformed JSON at line 1 column 3 path $"));
        assertThat(loggingEvents.get(1).getFormattedMessage(), is("Non valid Json String Exception: {}"));
    }


    @Test
    public void addTracerTagsAsyncTestValidJSON() {
        ascentGatewayAuditHelper.addTracerTagsAsync("{\"response\":\"test response\"}");
        // Now verify our logging interactions
        verify(mockAppender, times(4)).doAppend(captorLoggingEvent.capture());
        // Having a genricised captor means we don't need to cast

        final List<LoggingEvent> loggingEvents = captorLoggingEvent.getAllValues();
        // Check log level is correct
        assertThat(loggingEvents.get(0).getLevel(), is(Level.DEBUG));
        assertThat(loggingEvents.get(1).getLevel(), is(Level.DEBUG));
        assertThat(loggingEvents.get(0).getFormattedMessage(), is("JsonElement:: {\"response\":\"test response\"}"));
        assertThat(loggingEvents.get(1).getFormattedMessage(), containsString("[members={response=\"test response\"}]"));
    }

    @Test
    public void addTracerTagsAsyncTestLoggerDebugOff() {
        final Logger logger = (Logger) LoggerFactory.getLogger(AscentGatewayAuditHelper.class);
        logger.setLevel(Level.ERROR);
        ascentGatewayAuditHelper.addTracerTagsAsync("{\"response\":\"test response\"}");
        // Now verify our logging interactions
        verify(mockAppender, times(0)).doAppend(captorLoggingEvent.capture());
        // Having a genricised captor means we don't need to cast
    }

    @Test
    public void addTracerTagsAsyncTestJsonPrimitive() {
        ascentGatewayAuditHelper.addTracerTagsAsync("{\"geometry\": {\n" +
                "    \"type\": \"Point\",\n" +
                "    \"coordinates\": [\n" +
                "        -94.6716,\n" +
                "        39.0693\n" +
                "    ]\n" +
                "}\n" +
                "}");
        // Now verify our logging interactions
        verify(mockAppender, times(9)).doAppend(captorLoggingEvent.capture());
        // Having a genricised captor means we don't need to cast

        final List<LoggingEvent> loggingEvents = captorLoggingEvent.getAllValues();
        // Check log level is correct
        assertThat(loggingEvents.get(0).getLevel(), is(Level.DEBUG));
        assertThat(loggingEvents.get(1).getLevel(), is(Level.DEBUG));
        assertThat(loggingEvents.get(2).getLevel(), is(Level.DEBUG));
        assertThat(loggingEvents.get(3).getLevel(), is(Level.DEBUG));
        assertThat(loggingEvents.get(4).getLevel(), is(Level.DEBUG));
        assertThat(loggingEvents.get(5).getLevel(), is(Level.DEBUG));
        assertThat(loggingEvents.get(6).getLevel(), is(Level.DEBUG));
        assertThat(loggingEvents.get(7).getLevel(), is(Level.DEBUG));
        assertThat(loggingEvents.get(8).getLevel(), is(Level.DEBUG));

        assertThat(loggingEvents.get(0).getFormattedMessage(), is("JsonElement:: {\"geometry\":{\"type\":\"Point\",\"coordinates\":[-94.6716,39.0693]}}"));
        assertThat(loggingEvents.get(1).getFormattedMessage(), containsString("[members={geometry={\"type\":\"Point\",\"coordinates\":[-94.6716,39.0693]}}]"));
        assertThat(loggingEvents.get(2).getFormattedMessage(), containsString("Key >>> geometry"));
        assertThat(loggingEvents.get(3).getFormattedMessage(), containsString("Val >>> {\"type\":\"Point\",\"coordinates\":[-94.6716,39.0693]}"));
        assertThat(loggingEvents.get(4).getFormattedMessage(), containsString("processJsonElement#JsonElement:: com.google.gson.JsonPrimitive"));
        assertThat(loggingEvents.get(5).getFormattedMessage(), containsString("processJsonElement#JsonElement:: com.google.gson.JsonArray"));
        assertThat(loggingEvents.get(6).getFormattedMessage(), containsString("JsonArray:: com.google.gson.JsonArray"));
        assertThat(loggingEvents.get(7).getFormattedMessage(), containsString("processJsonElement#JsonElement:: com.google.gson.JsonPrimitive"));
        assertThat(loggingEvents.get(8).getFormattedMessage(), containsString("processJsonElement#JsonElement:: com.google.gson.JsonPrimitive"));

    }

    @Test
    public void testJsonArray() throws Exception {
        ascentGatewayAuditHelper.addTracerTagsAsync("{\"geometry\": [{\n" +
                "    \"type\": \"Point\",\n" +
                "    \"coordinates\": [\n" +
                "        -94.6716,\n" +
                "        39.0693\n" +
                "    ]\n" +
                "},{\n" +
                "    \"type\": \"Point\",\n" +
                "    \"coordinates\": [\n" +
                "        -94.6716,\n" +
                "        39.0693\n" +
                "    ]\n" +
                "}]\n" +
                "}");

        // Now verify our logging interactions
        verify(mockAppender, times(21)).doAppend(captorLoggingEvent.capture());
        // Having a genricised captor means we don't need to cast

        final List<LoggingEvent> loggingEvents = captorLoggingEvent.getAllValues();
        // Check log level is correct
        assertThat(loggingEvents.get(0).getLevel(), is(Level.DEBUG));
        assertThat(loggingEvents.get(1).getLevel(), is(Level.DEBUG));
        assertThat(loggingEvents.get(2).getLevel(), is(Level.DEBUG));
        assertThat(loggingEvents.get(3).getLevel(), is(Level.DEBUG));
        assertThat(loggingEvents.get(4).getLevel(), is(Level.DEBUG));
        assertThat(loggingEvents.get(5).getLevel(), is(Level.DEBUG));
        assertThat(loggingEvents.get(6).getLevel(), is(Level.INFO));
        assertThat(loggingEvents.get(7).getLevel(), is(Level.INFO));
        assertThat(loggingEvents.get(8).getLevel(), is(Level.INFO));
        assertThat(loggingEvents.get(9).getLevel(), is(Level.INFO));
        assertThat(loggingEvents.get(10).getLevel(), is(Level.DEBUG));
        assertThat(loggingEvents.get(11).getLevel(), is(Level.DEBUG));
        assertThat(loggingEvents.get(12).getLevel(), is(Level.DEBUG));
        assertThat(loggingEvents.get(13).getLevel(), is(Level.DEBUG));
        assertThat(loggingEvents.get(14).getLevel(), is(Level.INFO));
        assertThat(loggingEvents.get(15).getLevel(), is(Level.INFO));
        assertThat(loggingEvents.get(16).getLevel(), is(Level.INFO));
        assertThat(loggingEvents.get(17).getLevel(), is(Level.INFO));
        assertThat(loggingEvents.get(18).getLevel(), is(Level.DEBUG));
        assertThat(loggingEvents.get(19).getLevel(), is(Level.DEBUG));
        assertThat(loggingEvents.get(20).getLevel(), is(Level.DEBUG));

        assertEquals("JsonElement:: {}", loggingEvents.get(0).getMessage());
        assertEquals("JsonObject:: {}", loggingEvents.get(1).getMessage());
        assertEquals("Key >>> {}", loggingEvents.get(2).getMessage());
        assertEquals("Val >>> {}", loggingEvents.get(3).getMessage());
        assertEquals("JsonArray:: {}", loggingEvents.get(4).getMessage());

    }

}
