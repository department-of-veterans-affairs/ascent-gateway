package gov.va.ascent.gateway.filter;

import java.util.Collection;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.sleuth.Tracer;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;


public class AscentGatewayPostFilter extends ZuulFilter {

	private final static Logger LOGGER = LoggerFactory.getLogger(AscentGatewayPostFilter.class);
	
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
		HttpServletResponse response = ctx.getResponse();
		MDC.put("http.response", String.valueOf(ctx.getResponseStatusCode()));
		LOGGER.info(String.format("Completed Request %s request for %s", request.getMethod(), request.getRequestURL().toString()));
		if (LOGGER.isDebugEnabled()) {
			
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
		MDC.clear();
		return null;
	}
}
