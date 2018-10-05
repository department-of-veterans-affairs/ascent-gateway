package gov.va.ascent.gateway.filter;

import com.netflix.zuul.context.RequestContext;

import gov.va.ascent.framework.log.AscentLogger;
import gov.va.ascent.framework.log.AscentLoggerFactory;

public class AscentGatewayErrorFilter extends AscentGatewayAbstractFilter {

	private static final AscentLogger LOGGER = AscentLoggerFactory.getLogger(AscentGatewayErrorFilter.class);

	@Override
	public String filterType() {
		return "error";
	}

	@Override
	public int filterOrder() {
		return 0;
	}

	@Override
	public boolean shouldFilter() {
		return true;
	}

	@Override
	public Object run() {
		Throwable throwable = RequestContext.getCurrentContext().getThrowable();
		LOGGER.error("Exception was thrown in filters: ", throwable);

		return null;
	}
}
