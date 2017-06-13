package org.dainst.gazetteer.helpers;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

/**
 * Converts all http messages on FORWARD to GET in order to prevent problems with
 * PUT/DELETE messages on JSP pages
 */
public class HttpMethodConvertingFilter implements Filter {

	@Override
    public void init(FilterConfig config) throws ServletException {}

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
        throws IOException, ServletException {

        chain.doFilter(wrapRequest((HttpServletRequest) request), response);
    }

    @Override
    public void destroy() {}

    private static HttpServletRequestWrapper wrapRequest(HttpServletRequest request) {
    	
        return new HttpServletRequestWrapper(request) {
        	
            @Override
            public String getMethod() {
                return "GET";
            }
        };
    }
}
