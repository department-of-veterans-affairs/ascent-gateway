package gov.va.ascent.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;

public class AscentGatewayErrorFilter extends ZuulFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(AscentGatewayErrorFilter.class);
    
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
