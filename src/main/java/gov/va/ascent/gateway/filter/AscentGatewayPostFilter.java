package gov.va.ascent.gateway.filter;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.sleuth.Tracer;

import com.netflix.zuul.context.RequestContext;

import gov.va.ascent.framework.log.AscentLogger;
import gov.va.ascent.framework.log.AscentLoggerFactory;
import gov.va.ascent.framework.util.SanitizationUtil;

public class AscentGatewayPostFilter extends AscentGatewayAbstractFilter {

	private static final AscentLogger LOGGER = AscentLoggerFactory.getLogger(AscentGatewayPostFilter.class);

	@Autowired
	Tracer tracer;

	/*
	 * (non-Javadoc)
	 *
	 * @see com.netflix.zuul.IZuulFilter#run()
	 */
	@Override
	public Object run() {
		final RequestContext ctx = RequestContext.getCurrentContext();
		final HttpServletRequest request = ctx.getRequest();
		MDC.put("http.response", String.valueOf(ctx.getResponseStatusCode()));

		LOGGER.info("Completed Request {}",
				SanitizationUtil.stripXSS(request.getMethod())
						+ " for " + SanitizationUtil.stripXSS(request.getRequestURL().toString()));

		if (LOGGER.isDebugEnabled()) {
			debugRequestResponse(ctx);
		}
		MDC.clear();
		return null;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.netflix.zuul.ZuulFilter#filterType()
	 */
	@Override
	public String filterType() {
		return "post";
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.netflix.zuul.ZuulFilter#filterOrder()
	 */
	@Override
	public int filterOrder() {
		return 0;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.netflix.zuul.IZuulFilter#shouldFilter()
	 */
	@Override
	public boolean shouldFilter() {
		return true;
	}

}
