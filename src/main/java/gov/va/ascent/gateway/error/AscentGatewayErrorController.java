package gov.va.ascent.gateway.error;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
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

import gov.va.ascent.framework.log.AscentLogger;
import gov.va.ascent.framework.log.AscentLoggerFactory;
import gov.va.ascent.framework.util.SanitizationUtil;

/**
 * The rest endpoint resource for gateway error handling.
 *
 * @author aburkholder
 */
@RestController
public class AscentGatewayErrorController extends AbstractErrorController {

	private static final AscentLogger LOGGER = AscentLoggerFactory.getLogger(AscentGatewayErrorController.class);

	/** The request mapping */
	@Value("${error.path:/error}")
	private String errorPath;

	/** Attributes related to error handling, set on the request object */
	@Autowired
	private ErrorAttributes errorAttributes;

	/** Manage sleuth instrumentation spans */
	@Autowired
	Tracer tracer;

	/**
	 * Instantiate the resource endpoint.
	 *
	 * @param errorAttributes error handling attributes
	 */
	public AscentGatewayErrorController(final ErrorAttributes errorAttributes) {
		super(errorAttributes);
	}

	/**
	 * Get the request mapping path.
	 *
	 * @return String the request mapping path
	 */
	@Override
	public String getErrorPath() {
		return errorPath;
	}

	/**
	 * The endpoint operation for error handling.
	 *
	 * @param request the HttpServletRequest
	 * @param response the HttpServletResponse
	 * @return ResponseEntity&lt;Map&lt;String, Object&gt;&gt; the entity for the response
	 */
	@RequestMapping(value = "${error.path:/error}", produces = "application/json;charset=UTF-8")
	// NOSONAR TODO: Need to review the method to figure out if this is better way to add span error tag and also the response
	// status body message to be set using getErrorAttributes(request, true)
	public ResponseEntity<Map<String, Object>> error(final HttpServletRequest request, final HttpServletResponse response) {
		MDC.put("http.request", tracer.getCurrentSpan().getName());
		MDC.put("http.response", String.valueOf(response.getStatus()));

		LOGGER.error("REQUEST :: < {}",
				SanitizationUtil.stripXSS(request.getScheme())
						+ " " + SanitizationUtil.stripXSS(request.getLocalAddr())
						+ ":" + request.getLocalPort());
		LOGGER.error("REQUEST :: < {}",
				SanitizationUtil.stripXSS(request.getMethod())
						+ " " + SanitizationUtil.stripXSS(request.getRequestURI())
						+ " " + SanitizationUtil.stripXSS(request.getProtocol()));
		LOGGER.error("RESPONSE:: > HTTP:{}", response.getStatus());

		int status = response.getStatus();
		if (status == 0) {
			status = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
			LOGGER.error("RESPONSE:: > ResponseEntity status modified from 0 to {}", status);
		}

		final String errorMessage = getErrorMessage(RequestContext.getCurrentContext());

		LOGGER.debug("Tracer Current Span: {}", this.tracer.getCurrentSpan());

		this.tracer.addTag(Span.SPAN_ERROR_TAG_NAME, errorMessage);

		MDC.remove("http.request");
		MDC.remove("user");
		MDC.remove("http.response");

		return ResponseEntity.status(status).body(getErrorAttributes(request, true));
	}

	/**
	 * Extract the error message from the request context.
	 *
	 * @param context the RequestContext
	 * @return String the error message
	 */
	String getErrorMessage(final RequestContext context) {
		String errorMessage = "Unexpected error occurred";

		InputStream inputStream = null;
		try {
			inputStream = context.getResponseDataStream();
			if (inputStream != null) {
				try {
					errorMessage = IOUtils.toString(inputStream, StandardCharsets.UTF_8.name());
					LOGGER.error("Error Response:: {}", errorMessage);
				} catch (final IOException e) {
					LOGGER.error("IOException:: {}", e);
				}
			}
		} finally {
			IOUtils.closeQuietly(inputStream);
		}

		return errorMessage;
	}

	/**
	 * Retrieve the error attributes from the request.
	 *
	 * @param request the HttpServletRequest
	 * @param includeStackTrace a boolean determining if the stacktrace should be included in the attributes or not
	 * @return Map&lt;String, Object&gt; the error attributes
	 */
	@Override
	public Map<String, Object> getErrorAttributes(final HttpServletRequest request, final boolean includeStackTrace) {
		final RequestAttributes requestAttributes = new ServletRequestAttributes(request);
		return errorAttributes.getErrorAttributes(requestAttributes, includeStackTrace);
	}
}