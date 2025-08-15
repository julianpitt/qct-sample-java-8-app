package org.springframework.web.filter;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.util.ContentCachingResponseWrapper;
import org.springframework.web.util.WebUtils;

import java.io.IOException;

/**
 * Extension of ShallowEtagHeaderFilter that uses SHA-512 for ETag generation
 */
public class Sha512ShallowEtagHeaderFilter extends ShallowEtagHeaderFilter {

    /**
     * Custom implementation that uses SHA-512 for ETag generation
     */
	@Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) 
            throws ServletException, IOException {
        super.doFilterInternal(request, response, filterChain);
        
        // If response is already committed, we can't modify it
        if (response.isCommitted()) {
            return;
        }
        
        // Get the cached response if available
        ContentCachingResponseWrapper responseWrapper = 
                WebUtils.getNativeResponse(response, ContentCachingResponseWrapper.class);
        if (responseWrapper != null) {
            // Get the response content
            byte[] responseBody = responseWrapper.getContentAsByteArray();
            if (responseBody.length > 0) {
                // Generate SHA-512 ETag
                String eTag = generateSha512ETag(responseBody);
                // Set the ETag header
                response.setHeader("ETag", eTag);
            }
        }
    }
    
    /**
     * Generate an ETag value using SHA-512
     */
    private String generateSha512ETag(byte[] bytes) {
		final HashCode hash = Hashing.sha512().hashBytes(bytes);
		return "\"" + hash + "\"";
	}
}
