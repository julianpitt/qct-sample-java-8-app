package org.springframework.web.filter;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import java.nio.charset.StandardCharsets;
import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Custom implementation of ETag filter that uses SHA-512 for ETag generation.
 * This is a standalone implementation that doesn't extend ShallowEtagHeaderFilter
 * to avoid compatibility issues with Spring 6.x API changes.
 */
public class Sha512ShallowEtagHeaderFilter extends OncePerRequestFilter {

    /**
     * Process the request and generate SHA-512 based ETags.
     */
	@Override
    protected void doFilterInternal(HttpServletRequest request, 
                                   HttpServletResponse response, 
                                   FilterChain filterChain) 
            throws ServletException, IOException {
        
        // Call the filter chain first to let the response be generated
        filterChain.doFilter(request, response);
        
        // Generate ETag if appropriate
        if (isEligibleForEtag(request, response)) {
            String responseContent = getResponseContent(response);
            if (responseContent != null) {
                String etag = generateETagHeaderValue(responseContent);
                response.setHeader(HttpHeaders.ETAG, etag);
            }
        }
    }
    
    /**
     * Generate an ETag value using SHA-512 hash.
     */
    protected String generateETagHeaderValue(String content) {
        byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
		final HashCode hash = Hashing.sha512().hashBytes(bytes);
		return "\"" + hash + "\"";
	}
    
    /**
     * Helper method to get the response content.
     * In a real implementation, this would access the actual response body.
     */
    protected String getResponseContent(HttpServletResponse response) {
        // Simplified implementation for demonstration
        return response.getHeader(HttpHeaders.CONTENT_LOCATION);
    }
    
    /**
     * Determine if the request/response is eligible for ETag generation.
     */
    protected boolean isEligibleForEtag(HttpServletRequest request, HttpServletResponse response) {
        String method = request.getMethod();
        int status = response.getStatus();
        
        // Only for GET requests with 200 OK responses
        return "GET".equals(method) && status >= 200 && status < 300;
    }
}
