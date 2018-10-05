package gov.va.ascent.gateway.filter;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.sleuth.Span;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.netflix.zuul.context.RequestContext;

import gov.va.ascent.framework.log.AscentLogger;
import gov.va.ascent.framework.log.AscentLoggerFactory;

public class AscentGatewayAuditFilter extends AscentGatewayAbstractFilter {

	private static final AscentLogger LOGGER = AscentLoggerFactory.getLogger(AscentGatewayAuditFilter.class);
	private static final String SPAN_HTTP_REQUEST = "http.request";
	private static final String SPAN_HTTP_RESPONSE = "http.response";

	@Autowired
	Tracer tracer;

	@Override
	public String filterType() {
		return "post";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.netflix.zuul.ZuulFilter#filterOrder()
	 * Filter order is set to -1 so as to get the tag
	 * data into next span
	 */
	@Override
	public int filterOrder() {
		return -1;
	}

	@Override
	public boolean shouldFilter() {
		final RequestContext context = RequestContext.getCurrentContext();
		if (context != null) {
			return supportsAuditType(context);
		} else {
			return false;
		}
	}

	private boolean supportsAuditType(final RequestContext context) {
		return !httpStatusSuccessful(context.getResponse());
	}

	@Override
	public Object run() {
		RequestContext ctx = RequestContext.getCurrentContext();
		HttpServletRequest request = ctx.getRequest();
		HttpServletResponse response = ctx.getResponse();

		if (LOGGER.isDebugEnabled()) {
			debugRequestResponse(ctx);
		}

		/**
		 * AUDIT THE REQUEST AND RESPONSE ADDED TO TRACER FOR SLEUTH (org.springframework.cloud.sleuth.Tracer)
		 * BELOW CONDITIONS WOULD TRIGGER ADDING AUDIT TAGS
		 * 1) HTTP ERROR RESPONSE
		 */
		String responseMessage = "";
		try {
			InputStream is = request.getInputStream();
			String content = IOUtils.toString(is, StandardCharsets.UTF_8.name());
			if (StringUtils.isNotEmpty(content)) {
				LOGGER.debug("Request content: {}", content);
				this.tracer.addTag(SPAN_HTTP_REQUEST, content);
			}
		} catch (IOException e) {
			LOGGER.warn("IOException Request InputStream:: {}", e);
		}

		InputStream inputStream = ctx.getResponseDataStream();
		if (inputStream != null) {
			try {
				responseMessage = IOUtils.toString(inputStream, StandardCharsets.UTF_8.name());
				ctx.setResponseBody(responseMessage);
				LOGGER.debug("Response:: {}", responseMessage);
			} catch (IOException e) {
				LOGGER.warn("IOException Response InputStream:: {}", e);
			} finally {
				IOUtils.closeQuietly(inputStream);
			}
		}

		if (!httpStatusSuccessful(response)) {
			int status = response.getStatus();
			if (status == 0) {
				status = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
			}
			if (StringUtils.isEmpty(responseMessage)) {
				responseMessage = "Unexpected error occurred";
			}
			LOGGER.error("Error Response:: {}", responseMessage);
			this.tracer.addTag(Span.SPAN_ERROR_TAG_NAME, responseMessage);
			return ResponseEntity.status(status).body(responseMessage);
		} else {
			this.tracer.addTag(SPAN_HTTP_RESPONSE, responseMessage);
		}

		return null;
	}

	private boolean httpStatusSuccessful(HttpServletResponse response) {
		if (response.getStatus() == 0) {
			return false;
		}
		HttpStatus.Series httpStatusSeries = HttpStatus.Series.valueOf(response.getStatus());
		return httpStatusSeries == HttpStatus.Series.SUCCESSFUL || httpStatusSeries == HttpStatus.Series.REDIRECTION;
	}

}
