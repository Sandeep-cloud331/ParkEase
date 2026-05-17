package com.parkease.parking.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        // 🔍 DEBUG: Check header
        System.out.println("==== JWT FILTER START ===="); // DEBUG
        System.out.println("Authorization Header: " + authHeader); // DEBUG

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            System.out.println("❌ No Bearer token found"); // DEBUG
            filterChain.doFilter(request, response);
            return;
        }

        final String token = authHeader.substring(7);

        // 🔍 DEBUG: Token extracted
        System.out.println("TOKEN: " + token); // DEBUG

        if (!jwtUtil.isTokenValid(token)) {
            System.out.println("❌ TOKEN INVALID"); // DEBUG
            filterChain.doFilter(request, response);
            return;
        }

        String email  = jwtUtil.extractEmail(token);
        String role   = jwtUtil.extractRole(token);
        Long userId   = jwtUtil.extractUserId(token);

        // 🔍 DEBUG: Extracted values
        System.out.println("EMAIL: " + email); // DEBUG
        System.out.println("ROLE (raw): " + role); // DEBUG
        System.out.println("USER ID: " + userId); // DEBUG

        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            var authToken = new UsernamePasswordAuthenticationToken(
                    email,
                    null,
                    List.of(new SimpleGrantedAuthority("ROLE_" + role))
            );

            // 🔍 DEBUG: Authority being set
            System.out.println("SETTING AUTH WITH ROLE: ROLE_" + role); // DEBUG

            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authToken);

            // 🔍 DEBUG: Final authorities inside context
            System.out.println("FINAL AUTHORITIES: " +
                    SecurityContextHolder.getContext().getAuthentication().getAuthorities()); // DEBUG

            request.setAttribute("userId", userId);
        } else {
            System.out.println("❌ Authentication NOT set (maybe already exists or email null)"); // DEBUG
        }

        filterChain.doFilter(request, response);
    }
}