package it.unical.ea.eventra.conf.Audit;

import it.unical.ea.eventra.data.entity.AuditLog;
import it.unical.ea.eventra.data.repository.AuditLogRepository;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;


import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

@Component
public class AuditLoggingFilter extends OncePerRequestFilter {

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);

        filterChain.doFilter(wrappedRequest, wrappedResponse);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = (auth != null && auth.isAuthenticated()) ? auth.getName() : "anonymous";

        AuditLog log = new AuditLog();
        log.setUsername(username);
        log.setMethod(request.getMethod());
        log.setEndpoint(request.getRequestURI());
        log.setIpAddress(request.getRemoteAddr());
        log.setResponseStatus(wrappedResponse.getStatus());
        log.setTimestamp(LocalDateTime.now());
        log.setRequestBody(new String(wrappedRequest.getContentAsByteArray(), StandardCharsets.UTF_8));
        log.setQueryParams(request.getQueryString());

        auditLogRepository.save(log);
        wrappedResponse.copyBodyToResponse();
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return uri.startsWith("/swagger") || uri.startsWith("/h2-console") || uri.contains("/actuator");
    }
}
