package gov.va.ascent.gateway.filter;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collection;
import java.util.Enumeration;

public abstract class AscentGatewayAbstractFilter extends ZuulFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(AscentGatewayAbstractFilter.class);

    void debugRequestResponse(final RequestContext ctx) {
        HttpServletRequest request = ctx.getRequest();
        HttpServletResponse response = ctx.getResponse();

        LOGGER.debug("Request :: < " + request.getScheme() + " " + request.getLocalAddr() + ":" + request.getLocalPort());
        LOGGER.debug("Request :: < " + request.getMethod() + " " + request.getRequestURI() + " " + request.getProtocol());
        LOGGER.debug("Response:: > HTTP:" + ctx.getResponseStatusCode());

        Enumeration<String> requestHeaderNames = request.getHeaderNames();
        while (requestHeaderNames.hasMoreElements()) {
            String key = requestHeaderNames.nextElement();
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
