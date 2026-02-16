package unrn.api;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class RawBodyLoggingFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(RawBodyLoggingFilter.class);
    private static final String TARGET_PATH = "/api/admin/peliculas";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        if (!isTargetRequest(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        ContentCachingRequestWrapper wrapped = new ContentCachingRequestWrapper(request);
        try {
            filterChain.doFilter(wrapped, response);
        } finally {
            logRequestBody(wrapped);
        }
    }

    private boolean isTargetRequest(HttpServletRequest request) {
        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            return false;
        }
        String uri = request.getRequestURI();
        return TARGET_PATH.equals(uri);
    }

    private void logRequestBody(ContentCachingRequestWrapper request) {
        byte[] body = request.getContentAsByteArray();
        String contentType = request.getContentType();
        int contentLength = request.getContentLength();
        String raw = new String(body, StandardCharsets.UTF_8);
        String escaped = escapeForLog(raw);

        LOGGER.info(
                "RAW_BODY_CAPTURE method={} uri={} contentType={} contentLength={} rawBody={} escapedBody={}",
                request.getMethod(),
                request.getRequestURI(),
                contentType,
                contentLength,
                raw,
                escaped
        );
    }

    private String escapeForLog(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("\r", "\\r")
                .replace("\n", "\\n")
                .replace("\t", "\\t");
    }
}