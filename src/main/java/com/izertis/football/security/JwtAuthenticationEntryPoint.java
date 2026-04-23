package com.izertis.football.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.izertis.football.dto.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Returns a structured JSON 401 response instead of the default HTML error page
 * when an unauthenticated request hits a protected endpoint.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        ErrorResponse body = new ErrorResponse(401, "UNAUTHORIZED",
                "Authentication required. Please provide a valid Bearer token.");
        objectMapper.writeValue(response.getOutputStream(), body);
    }
}
