package gov.va.ascent.gateway.error;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.event.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ErrorAttributes;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.context.request.RequestAttributes;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.netflix.zuul.context.RequestContext;

import gov.va.ascent.framework.log.AscentLogger;
import gov.va.ascent.gateway.BaseTest;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AscentGatewayErrorControllerTest extends BaseTest {

	private static final String ERROR_MSG = "This is a test error message";

	@Mock
	ErrorAttributes errorAttributes = new ErrorAttributes() {
		@Override
		public Map<String, Object> getErrorAttributes(final RequestAttributes requestAttributes, final boolean b) {
			return null;
		}

		@Override
		public Throwable getError(final RequestAttributes requestAttributes) {
			return null;
		}
	};

	@Mock
	Tracer tracer;

	@Autowired
	private TestRestTemplate testRestTemplate;

//	@Mock
//	HttpServletResponse response;

	@InjectMocks
//	@Spy
	AscentGatewayErrorController controller;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		AscentLogger logger = null;
		try {
			final Field loggerField = AscentGatewayErrorController.class.getDeclaredField("LOGGER");
			loggerField.setAccessible(true);
			logger = (AscentLogger) loggerField.get(controller);
			logger.setLevel(Level.DEBUG);
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
			fail("Could not set log level to DEBUG");
		}
		// ((ch.qos.logback.classic.Logger) AscentGatewayErrorController.LOGGER).setLevel(ch.qos.logback.classic.Level.DEBUG);
		;
	}

	@Test
	public void errorTest() {
		String result = testRestTemplate.getForObject("/error", String.class);
		System.out.println(">>>>>    EntityResponse value:  " + result);
		final JsonParser parser = new JsonParser();
		final JsonElement jsonElement = parser.parse(result);
		assertNotNull(jsonElement.isJsonObject());

		final HttpServletResponse spiedResponse = spy(HttpServletResponse.class);
		doReturn(0).when(spiedResponse).getStatus();
//		spy(controller).error(any(HttpServletRequest.class), any(HttpServletResponse.class));
		mock(AscentGatewayErrorController.class).error(any(), any());
//		doReturn(0).when(response).getStatus();
		// when(response.getStatus()).thenReturn(0);
		result = testRestTemplate.getForObject("/error", String.class);
		System.out.println(">>>>>    EntityResponse value:  " + result);
		assertNotNull(result);
	}

	@Test
	public void testGetErrorMessage() {
		final RequestContext context = mock(RequestContext.class);

		InputStream is = null;
		try {
			is = spy(new ByteArrayInputStream(ERROR_MSG.getBytes()));
			when(context.getResponseDataStream()).thenReturn(is);

			final String errorMessage = controller.getErrorMessage(context);
			assertNotNull(errorMessage);
			assertTrue(ERROR_MSG.equals(errorMessage));
		} finally {
			IOUtils.closeQuietly(is);
		}

		try {
			is = spy(new ByteArrayInputStream(ERROR_MSG.getBytes()));
			when(context.getResponseDataStream()).thenReturn(is);
			try {
				// doThrow(new IOException("Testing")).when(is).read(any(byte[].class), anyInt(), anyInt());
				doThrow(new IOException("Testing")).when(is).read(any(byte[].class));
			} catch (final IOException e) {
				e.printStackTrace();
				fail("Should not havae thrown exception here.");
			}
			final String errorMessage = controller.getErrorMessage(context);
			assertNotNull(errorMessage);
			assertTrue(ERROR_MSG.equals(errorMessage));
		} finally {
			IOUtils.closeQuietly(is);
		}
	}
}
