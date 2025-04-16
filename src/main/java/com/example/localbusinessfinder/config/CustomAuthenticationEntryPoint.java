package com.example.localbusinessfinder.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import java.io.IOException;

public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    // Entry point for regular users -> /login
    private final AuthenticationEntryPoint defaultEntryPoint =
            new LoginUrlAuthenticationEntryPoint("/login");

    // Entry point for admin users -> /admin/login
    private final AuthenticationEntryPoint adminEntryPoint =
            new LoginUrlAuthenticationEntryPoint("/admin/login");

    // Matcher to identify requests targeting admin area
    private final RequestMatcher adminRequestMatcher = new AntPathRequestMatcher("/admin/**");

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException, ServletException {

        // If the request matches the admin path pattern...
        if (adminRequestMatcher.matches(request)) {
            // ...delegate to the admin entry point (redirect to /admin/login)
            adminEntryPoint.commence(request, response, authException);
        } else {
            // ...otherwise, delegate to the default entry point (redirect to /login)
            defaultEntryPoint.commence(request, response, authException);
        }
    }
} 