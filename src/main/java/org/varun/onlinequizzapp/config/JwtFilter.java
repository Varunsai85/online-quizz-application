package org.varun.onlinequizzapp.config;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.varun.onlinequizzapp.service.JwtService;
import org.varun.onlinequizzapp.service.UserService;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final ApplicationContext context;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        try {

            if (authHeader != null && authHeader.startsWith("Bearer")) {
                final String token = authHeader.substring(7);
                final String subject = jwtService.extractSubject(token);
                if (subject != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    UserDetails userDetails = context.getBean(UserService.class).loadUserByUsername(subject);
                    if (jwtService.validateToken(userDetails, token)) {
                        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                    }
                }
            }
            filterChain.doFilter(request, response);
        } catch (ExpiredJwtException ex) {
            writeResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Token Expired", ex.getMessage());
        } catch (MalformedJwtException ex) {
            writeResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Malformed Token", ex.getMessage());
        } catch (Exception ex) {
            writeResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Authentication error", ex.getMessage());
        }
    }

    private void writeResponse(HttpServletResponse response, int status, String message, String details) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        String json = """
                {
                "success": false,
                "message": %s,
                "details": %s
                }
                """.formatted(message, details);
        response.getWriter().write(json);
    }
}
