package gov.va.ascent.gateway.error;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.web.AbstractErrorController;
import org.springframework.boot.autoconfigure.web.ErrorAttributes;
import org.springframework.cloud.sleuth.Span;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.netflix.zuul.context.RequestContext;

import gov.va.ascent.framework.audit.AuditLogger;


@RestController
public class AscentGatewayErrorController extends AbstractErrorController {

	private static final Logger LOGGER = LoggerFactory.getLogger(AscentGatewayErrorController.class);

	@Value("${error.path:/error}")
	private String errorPath;

	@Autowired
	private ErrorAttributes errorAttributes;

	@Autowired Tracer tracer;

	public AscentGatewayErrorController(ErrorAttributes errorAttributes) {
		super(errorAttributes);
	}

	@Override
	public String getErrorPath() {
		return errorPath;
	}

	@RequestMapping(value = "${error.path:/error}", produces = "application/json;charset=UTF-8")
	//TODO: Need to review the method to figure out if this is better way to add span error tag and also the response
	// status body message to be set using getErrorAttributes(request, true)
	public ResponseEntity<Map<String, Object>> error(HttpServletRequest request, HttpServletResponse response) {
		MDC.put("http.request", tracer.getCurrentSpan().getName());
		MDC.put("http.response", String.valueOf(response.getStatus()));
		LOGGER.error("REQUEST :: < " + AuditLogger.sanitize(request.getScheme()) + " "
				+ AuditLogger.sanitize(request.getLocalAddr()) + ":" + request.getLocalPort());
		LOGGER.error("REQUEST :: < " + AuditLogger.sanitize(request.getMethod()) + " "
				+ AuditLogger.sanitize(request.getRequestURI()) + " " + AuditLogger.sanitize(request.getProtocol()));
		LOGGER.error("RESPONSE:: > HTTP:" + response.getStatus());
		int status = response.getStatus();
		if (status == 0) {
			status = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
		}

		RequestContext ctx = RequestContext.getCurrentContext();
		String errorMessage = "Unexpected error occurred";
		InputStream inputStream = ctx.getResponseDataStream();

		if (inputStream!=null) {
			try {
				errorMessage = IOUtils.toString(inputStream, StandardCharsets.UTF_8.name());
				LOGGER.error("Error Response:: {}", errorMessage);
			} catch (IOException e) {
				LOGGER.error("IOException:: {}", e);
			} finally {
				IOUtils.closeQuietly(inputStream);
			}
		}
		LOGGER.debug("Tracer Current Span: {}", this.tracer.getCurrentSpan());
		
		this.tracer.addTag(Span.SPAN_ERROR_TAG_NAME, errorMessage);

		MDC.remove("http.request");
		MDC.remove("user");
		MDC.remove("http.response");

		return ResponseEntity.status(status).body(getErrorAttributes(request, true));
	}

	@Override
	public Map<String, Object> getErrorAttributes(HttpServletRequest request, boolean includeStackTrace) {
		RequestAttributes requestAttributes = new ServletRequestAttributes(request);
		return errorAttributes.getErrorAttributes(requestAttributes, includeStackTrace);
	}
}