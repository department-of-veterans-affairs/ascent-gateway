package gov.va.ascent.gateway.filter;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.sleuth.Tracer;

import com.netflix.zuul.context.RequestContext;


public class AscentGatewayPostFilter extends AscentGatewayAbstractFilter {

	private static final Logger LOGGER = LoggerFactory.getLogger(AscentGatewayPostFilter.class);
	
	@Autowired Tracer tracer;
	
	/*
	 * (non-Javadoc)
	 * @see com.netflix.zuul.ZuulFilter#filterType()
	 */
	@Override
	public String filterType() {
		return "post";
	}

	/*
	 * (non-Javadoc)
	 * @see com.netflix.zuul.ZuulFilter#filterOrder()
	 */
	@Override
	public int filterOrder() {
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * @see com.netflix.zuul.IZuulFilter#shouldFilter()
	 */
	@Override
	public boolean shouldFilter() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see com.netflix.zuul.IZuulFilter#run()
	 */
	@Override
	public Object run() {
		RequestContext ctx = RequestContext.getCurrentContext();
		HttpServletRequest request = ctx.getRequest();
		MDC.put("http.response", String.valueOf(ctx.getResponseStatusCode()));
		LOGGER.info(String.format("Completed Request %s request for %s", request.getMethod(), request.getRequestURL().toString()));
		if (LOGGER.isDebugEnabled()) {
			debugRequestResponse(ctx);
		}
		MDC.clear();
		return null;
	}
}
