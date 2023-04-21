package fr.insee.kraftwerk.api;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;


@Component
@WebFilter(urlPatterns = "/*")
@Order(-999)
@Log4j2
public class LogRequestFilter extends OncePerRequestFilter {
	
	private static final String REQUEST_MESSAGE_FORMAT = 
			 "CALL {} {} - "
		//	+ "Content-Type :  {} \n "
		//	+ "Headers : {} \n "
			+ "Params : {} - "
			+ "Body : {} \n ";
	
	private static final String RESPONSE_MESSAGE_FORMAT = 
			 "END {} {}  - "
			+ "Status :  {} - "
		//	+ "Content-Type : {} \n "
		//	+ "Headers : {} \n "
			+ "Body : {} \n";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

    	//Cache request to avoid calling twice the same inputStream
        ContentCachingRequestWrapper req = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper resp = new ContentCachingResponseWrapper(response);
        
        log.info(REQUEST_MESSAGE_FORMAT, 
        		req.getMethod(), req.getRequestURI(), 
        	//	req.getContentType(),
            //    new ServletServerHttpRequest(req).getHeaders(), //Headers
                request.getQueryString(),//Params
                new String(req.getContentAsByteArray(), StandardCharsets.UTF_8));//Body


        // Execution request chain
        filterChain.doFilter(req, resp);
               
        StringBuilder  headers = new StringBuilder() ;
        resp.getHeaderNames().forEach(h-> headers.append(h).append(" : ").append(resp.getHeader(h)).append(";"));

        log.info(RESPONSE_MESSAGE_FORMAT, 
        		req.getMethod(), req.getRequestURI(), 
        		resp.getStatus(),
   //     		resp.getContentType(), 
   //     		headers.toString(),
                getResponseBody(req, resp)); //Body
        
        // Finally remember to respond to the client with the cached data.
        resp.copyBodyToResponse();
    }

	private String getResponseBody(ContentCachingRequestWrapper req, ContentCachingResponseWrapper resp) {
		if (req.getRequestURI().contains("swagger-ui") ||req.getRequestURI().contains("api-docs")) return "Hidden Swagger response";
		return new String(resp.getContentAsByteArray(), StandardCharsets.UTF_8);
	}
    
}
