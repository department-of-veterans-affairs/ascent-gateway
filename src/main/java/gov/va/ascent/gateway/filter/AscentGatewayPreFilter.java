package gov.va.ascent.gateway.filter;

import java.security.Principal;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;

import gov.va.ascent.security.jwt.PersonTraits;

public class AscentGatewayPreFilter extends ZuulFilter {
	
	private final static Logger LOGGER = LoggerFactory.getLogger(AscentGatewayPreFilter.class);

	@Override
	public Object run() {
		RequestContext ctx = RequestContext.getCurrentContext();
	    HttpServletRequest request = ctx.getRequest();
		MDC.put("http.request", request.getRequestURI());
		if(SecurityContextHolder.getContext().getAuthentication() != null) {
			Principal principal = SecurityContextHolder.getContext().getAuthentication();
			if (principal instanceof UsernamePasswordAuthenticationToken) {
				PersonTraits auth = (PersonTraits)((UsernamePasswordAuthenticationToken) principal).getPrincipal();
				MDC.put("user", auth.getFirstName() + " " + auth.getLastName());
			}
		}
		if (LOGGER.isDebugEnabled()) {
            ctx.setDebugRequest(true);
		    Enumeration<String> headerNames = request.getHeaderNames();
			while (headerNames.hasMoreElements()) {
				String key = (String) headerNames.nextElement();
				String value = request.getHeader(key);
				LOGGER.debug("Request Header: {} -> {}", key, value);
			}
	    }

	    LOGGER.info(String.format("Zuul Filter:  %s request to %s", request.getMethod(), request.getRequestURL().toString()));

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
