package com.aeroport.gateway.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.*;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil service;

    public JwtFilter(JwtUtil service) {
        this.service = service;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
//        response.setHeader("Access-Control-Allow-Origin", "http://localhost:3000");

//        String origin = request.getHeader("Origin");
//        if (origin != null) {
//            List<String> allowedOrigins = List.of("http://localhost:3000", "http://localhost:5173");
//
//            if (allowedOrigins.contains(origin)) {
//                response.setHeader("Access-Control-Allow-Origin", origin);
//            }
//        }
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "*");

        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        String authHeader = request.getHeader("Authorization");
        String path = request.getRequestURI();
        String method = request.getMethod();

        if(isPublic(path, method)) {
            filterChain.doFilter(request, response);
            return;
        }

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        String token = authHeader.substring(7);

        if (!service.isValid(token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        if (isAdmin(path, method) && !service.getRole(token).equals("ADMIN")) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        String email = service.getEmail(token);
        String role = service.getRole(token);

        HttpServletRequestWrapper wrappedRequest = new HttpServletRequestWrapper(request) {
            @Override
            public String getHeader(String name) {
                if ("X-User-Email".equals(name)) return email;
                if ("X-User-Role".equals(name)) return role;
                return super.getHeader(name);
            }

            @Override
            public Enumeration<String> getHeaders(String name) {
                if ("X-User-Email".equals(name)) return Collections.enumeration(List.of(email));
                if ("X-User-Role".equals(name)) return Collections.enumeration(List.of(role));
                return super.getHeaders(name);
            }

            @Override
            public Enumeration<String> getHeaderNames() {
                List<String> names = Collections.list(super.getHeaderNames());
                names.add("X-User-Email");
                names.add("X-User-Role");
                return Collections.enumeration(names);
            }
        };

        filterChain.doFilter(wrappedRequest, response);
    }

    private Boolean isPublic(String path, String method) {
        return path.startsWith("/auth/") || path.startsWith("/vols") && method.equals("GET") || path.startsWith("/actuator/") || path.startsWith("/paiements/webhook");
    }

    private Boolean isAdmin(String path, String method) {
        if (path.startsWith("/vols")) {
            return true;
        }
        if(path.startsWith("/reservations/vol/"))  {
            return true;
        }
        if (path.startsWith("/notifications") && method.equals("POST")) {
            return true;
        }
        return path.startsWith("/notifications/vol/");
    }
}
