package gov.va.ascent.gateway.filter;

import java.util.Collection;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;

import gov.va.ascent.framework.util.SanitizationUtil;

public abstract class AscentGatewayAbstractFilter extends ZuulFilter {

	private static final Logger LOGGER = LoggerFactory.getLogger(AscentGatewayAbstractFilter.class);

	void debugRequestResponse(final RequestContext ctx) {
		final HttpServletRequest request = ctx.getRequest();
		final HttpServletResponse response = ctx.getResponse();

		LOGGER.debug("Request :: < {}",
				SanitizationUtil.stripXSS(request.getScheme())
						+ " " + SanitizationUtil.stripXSS(request.getLocalAddr())
						+ ":" + request.getLocalPort());
		LOGGER.debug("Request :: < {}",
				SanitizationUtil.stripXSS(request.getMethod())
						+ " " + SanitizationUtil.stripXSS(request.getRequestURI())
						+ " " + SanitizationUtil.stripXSS(request.getProtocol()));
		LOGGER.debug("Response:: > HTTP:{}", ctx.getResponseStatusCode());

		final Enumeration<String> requestHeaderNames = request.getHeaderNames();
		while (requestHeaderNames.hasMoreElements()) {
			final String key = requestHeaderNames.nextElement();
			final String value = request.getHeader(key);
			LOGGER.debug("Request Header: {} -> {}", key, value);
		}

		if (response != null) {
			final Collection<String> responseHeaderNames = response.getHeaderNames();
			responseHeaderNames.forEach(key -> {
				final String value = response.getHeader(key);
				LOGGER.debug("Response Header: {} -> {}", key, value);
			});
		}
	}

}
