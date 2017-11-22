package gov.va.ascent.gateway.error;

import static org.junit.Assert.assertNotNull;

import java.util.Map;

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
@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AscentGatewayErrorControllerTest {

    @Mock
    ErrorAttributes errorAttributes = new ErrorAttributes() {
        @Override
        public Map<String, Object> getErrorAttributes(RequestAttributes requestAttributes, boolean b) {
            return null;
        }

        @Override
        public Throwable getError(RequestAttributes requestAttributes) {
            return null;
        }
    };

    @Mock
    Tracer tracer;

    @Autowired
    private TestRestTemplate testRestTemplate;

    @InjectMocks
    AscentGatewayErrorController controller;

    @Test
    public void errorTest(){
        String result = testRestTemplate.getForObject("/error", String.class);
        JsonParser parser = new JsonParser();
        JsonElement jsonElement = parser.parse(result);
        assertNotNull(jsonElement.isJsonObject());
    }

}
