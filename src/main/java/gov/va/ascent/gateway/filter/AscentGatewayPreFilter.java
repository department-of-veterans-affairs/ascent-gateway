package gov.va.ascent.gateway.filter;

import javax.servlet.http.HttpServletRequest;

import gov.va.ascent.framework.security.SecurityUtils;
import gov.va.ascent.framework.util.SanitizationUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.netflix.zuul.context.RequestContext;

public class AscentGatewayPreFilter extends AscentGatewayAbstractFilter {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(AscentGatewayPreFilter.class);

	@Override
	public Object run() {
		RequestContext ctx = RequestContext.getCurrentContext();
	    HttpServletRequest request = ctx.getRequest();
		MDC.put("http.request", request.getRequestURI());
		if(SecurityUtils.getPersonTraits() != null) {
			MDC.put("user", SecurityUtils.getPersonTraits().getUser());
			MDC.put("tokenId", SecurityUtils.getPersonTraits().getTokenId());
		}
		if (LOGGER.isDebugEnabled()) {
            ctx.setDebugRequest(true);
            debugRequestResponse(ctx);
	    }

		LOGGER.info(String.format("Zuul Filter:  %s request to %s", SanitizationUtil.stripXSS(request.getMethod()),
				SanitizationUtil.stripXSS(request.getRequestURL().toString())));

	    return null;
	}

	@Override
	public boolean shouldFilter() {
		return true;
	}

	@Override
	public int filterOrder() {
		return 0;
	}

	@Override
	public String filterType() {
		return "pre";
	}

}
