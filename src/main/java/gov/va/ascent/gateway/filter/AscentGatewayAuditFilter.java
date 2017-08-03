package gov.va.ascent.gateway.filter;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ErrorAttributes;
import org.springframework.cloud.sleuth.Span;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import com.netflix.util.Pair;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;

import gov.va.ascent.gateway.audit.AscentGatewayAuditHelper;


public class AscentGatewayAuditFilter extends ZuulFilter {

	private final static Logger LOGGER = LoggerFactory.getLogger(AscentGatewayAuditFilter.class);
	public static final String SPAN_HTTP_REQUEST = "http.request";
	public static final String SPAN_HTTP_RESPONSE = "http.response";

	@Autowired ErrorAttributes errorAttributes;
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
						&& (obj.second().contains(MediaType.APPLICATION_JSON_UTF8_VALUE) 
								|| obj.second().contains(MediaType.APPLICATION_JSON_VALUE)
								|| obj.second().contains(MediaType.APPLICATION_XML_VALUE) 
								|| obj.second().contains(MediaType.APPLICATION_ATOM_XML_VALUE))).findFirst();
		if ((context.getRequest().getMethod().equals(HttpMethod.POST.name()) && contentTypeHeaderPair.isPresent()) 
				|| !httpStatusSuccessful(context.getResponse())) {
			return true;
		}
		return false;
	}

	@Override
	public Object run() {
		RequestContext ctx = RequestContext.getCurrentContext();
		HttpServletRequest request = ctx.getRequest();
		HttpServletResponse response = ctx.getResponse();

		if (LOGGER.isDebugEnabled()) {
			debugRequestResponse(ctx, request, response);
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

	private void debugRequestResponse(final RequestContext ctx,
			final HttpServletRequest request,
			final HttpServletResponse response) {

		LOGGER.debug("Request :: < " + request.getScheme() + " " + request.getLocalAddr() + ":" + request.getLocalPort());
		LOGGER.debug("Request :: < " + request.getMethod() + " " + request.getRequestURI() + " " + request.getProtocol());
		LOGGER.debug("Response:: > HTTP:" + ctx.getResponseStatusCode());

		Enumeration<String> requestHeaderNames = request.getHeaderNames();  
		while (requestHeaderNames.hasMoreElements()) {
			String key = (String) requestHeaderNames.nextElement();
			String value = request.getHeader(key);
			LOGGER.debug("Request Header: {} -> {}", key, value);
		}

		Collection<String> responseHeaderNames = response.getHeaderNames();
		responseHeaderNames.forEach(key->{
			String value = response.getHeader(key);
			LOGGER.debug("Response Header: {} -> {}", key, value);
		});
	}
}
