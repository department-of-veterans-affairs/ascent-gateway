package gov.va.ascent.gateway.error;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.doReturn;

import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ErrorAttributes;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.context.request.RequestAttributes;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AscentGatewayErrorControllerTest {

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

	@Mock
	HttpServletResponse response;

	@InjectMocks
	AscentGatewayErrorController controller;

	@Test
	public void errorTest() {
		String result = testRestTemplate.getForObject("/error", String.class);
		final JsonParser parser = new JsonParser();
		final JsonElement jsonElement = parser.parse(result);
		assertNotNull(jsonElement.isJsonObject());

		doReturn(0).when(response).getStatus();
		result = testRestTemplate.getForObject("/error", String.class);

	}

}
