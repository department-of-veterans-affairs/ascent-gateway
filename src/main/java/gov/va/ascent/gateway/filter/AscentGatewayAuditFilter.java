package gov.va.ascent.gateway.filter;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.sleuth.Span;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import com.netflix.util.Pair;
import com.netflix.zuul.context.RequestContext;

import gov.va.ascent.gateway.audit.AscentGatewayAuditHelper;


public class AscentGatewayAuditFilter extends AscentGatewayAbstractFilter {

	private static final Logger LOGGER = LoggerFactory.getLogger(AscentGatewayAuditFilter.class);
	private static final String SPAN_HTTP_REQUEST = "http.request";
	private static final String SPAN_HTTP_RESPONSE = "http.response";


    private static final Set<String> MEDIA_TYPE_LIST = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            MediaType.APPLICATION_JSON.toString(),
            MediaType.APPLICATION_JSON_UTF8_VALUE,
            MediaType.APPLICATION_XML_VALUE,
            MediaType.APPLICATION_ATOM_XML_VALUE
    )));

	@Autowired Tracer tracer;
	@Autowired AscentGatewayAuditHelper auditHelper;


	@Override
	public String filterType() {
		return "post";
	}

	/*
	 * (non-Javadoc)
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
			return containsContent(context) && supportsAuditType(context);
		} else {
			return false;
		}
	}

	private boolean containsContent(final RequestContext context) {
		return context.getResponseDataStream() != null || context.getResponseBody() != null;
	}

	private boolean supportsAuditType(final RequestContext context) {
		Optional<Pair<String, String>> contentTypeHeaderPair =
				context.getOriginResponseHeaders().stream().filter(
						obj -> obj.first().equals(HttpHeaders.CONTENT_TYPE)
						&& (MEDIA_TYPE_LIST.contains(obj.second()))).findFirst();

		return ((context.getRequest().getMethod().equals(HttpMethod.POST.name()) && contentTypeHeaderPair.isPresent())
				|| !httpStatusSuccessful(context.getResponse()));
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
		 *  AUDIT THE REQUEST AND RESPONSE ADDED TO TRACER FOR SLEUTH (org.springframework.cloud.sleuth.Tracer)
		 *  ANY OF THE BELOW CONDITIONS WOULD TRIGGER ADDING AUDIT TAGS
		 *  1) METHOD TYPE AS POST AND CONTENT TYPE AS JSON or XML OR
		 *  2) HTTP ERROR RESPONSE
		 */
		String responseMessage = "";
		try {
			InputStream is=request.getInputStream();
			String content=IOUtils.toString(is, StandardCharsets.UTF_8.name());
			if (StringUtils.isNotEmpty(content)) {
				LOGGER.debug("Request content: {}", content);
				this.tracer.addTag(SPAN_HTTP_REQUEST, content);
			}
		} catch (IOException e) {
			LOGGER.warn("IOException Request InputStream:: {}", e);
		}

		InputStream inputStream = ctx.getResponseDataStream();
		if (inputStream!=null) {
			try {
				responseMessage = IOUtils.toString(inputStream, StandardCharsets.UTF_8.name());
				ctx.setResponseBody(responseMessage);
				LOGGER.debug("Response:: {}", responseMessage);
				auditHelper.addTracerTagsAsync(responseMessage);
			} catch (IOException e) {
				LOGGER.warn("IOException Response InputStream:: {}", e);
			}
			finally {
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
