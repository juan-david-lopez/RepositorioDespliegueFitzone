package co.edu.uniquindio.FitZone.config;

import co.edu.uniquindio.FitZone.service.impl.UserDetailsServiceImpl;
import co.edu.uniquindio.FitZone.util.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JwtRequestFilter es una clase que extiende OncePerRequestFilter y se utiliza para filtrar las solicitudes HTTP
 * y verificar la validez del token JWT. Si el token es válido, se establece la autenticación en el contexto de seguridad
 * de Spring Security, lo que permite que el usuario acceda a los recursos protegidos de la aplicación.
 */
@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtRequestFilter.class);

    private final UserDetailsServiceImpl userDetailsService;
    private final JwtUtil jwtUtil;

    public JwtRequestFilter(UserDetailsServiceImpl userDetailsService, JwtUtil jwtUtil) {
        this.userDetailsService = userDetailsService;
        this.jwtUtil = jwtUtil;
    }

    /**
     * doFilterInternal es un método que se ejecuta para cada solicitud HTTP y verifica la validez del token JWT.
     * Si el token es válido, se establece la autenticación en el contexto de seguridad de Spring Security.
     * @param request
     * @param response
     * @param filterChain
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        final String authorizationHeader = request.getHeader("Authorization");
        final String requestURI = request.getRequestURI();

        String username = null;
        String jwt = null;
        boolean tokenExpired = false;

        // Saltar validación JWT para endpoints públicos de autenticación
        if (isPublicEndpoint(requestURI)) {
            filterChain.doFilter(request, response);
            return;
        }

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);

            // ✅ AGREGAR TRY-CATCH para manejar tokens malformados
            try {
                // Verificar formato básico antes de procesar
                if (isValidJwtFormat(jwt)) {
                    username = jwtUtil.extractUsername(jwt);
                } else {
                    logger.warn("Token JWT con formato inválido recibido en URI: {}", requestURI);
                    sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Token JWT con formato inválido");
                    return;
                }
            } catch (MalformedJwtException e) {
                logger.warn("Token JWT malformado recibido en URI: {}. Error: {}", requestURI, e.getMessage());
                sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Token JWT malformado");
                return;
            } catch (ExpiredJwtException e) {
                logger.info("Token JWT expirado recibido en URI: {}. Usuario: {}", requestURI, e.getClaims().getSubject());
                tokenExpired = true;
                sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Token JWT expirado. Por favor, inicia sesión nuevamente.");
                return;
            } catch (IllegalArgumentException e) {
                logger.error("Error procesando token JWT en URI: {}. Error: {}", requestURI, e.getMessage());
                sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Error procesando token JWT");
                return;
            } catch (Exception e) {
                logger.error("Error inesperado procesando token JWT en URI: {}. Error: {}", requestURI, e.getMessage());
                sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Error de autenticación");
                return;
            }
        }

        // Solo proceder con autenticación si tenemos un username válido
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

                if (jwtUtil.validateToken(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                    usernamePasswordAuthenticationToken.setDetails(new WebAuthenticationDetailsSource()
                            .buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
                    logger.debug("Usuario {} autenticado exitosamente para URI: {}", username, requestURI);
                }
            } catch (Exception e) {
                logger.error("Error estableciendo autenticación para usuario {} en URI: {}. Error: {}",
                        username, requestURI, e.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Verificar si la URI corresponde a un endpoint público que no requiere autenticación JWT
     */
    private boolean isPublicEndpoint(String uri) {
        return uri.equals("/auth/login") ||
                uri.equals("/auth/login-2fa") ||
                uri.equals("/auth/verify-otp") ||
                uri.equals("/auth/resend-otp") ||
                uri.equals("/auth/forgot-password") ||
                uri.equals("/auth/reset-password") ||
                uri.startsWith("/public/") ||
                uri.equals("/users/public/register") ||
                uri.equals("/error") ||
                uri.equals("/favicon.ico");
    }

    /**
     * Verificar formato básico de JWT (debe tener exactamente 3 partes separadas por puntos)
     */
    private boolean isValidJwtFormat(String token) {
        if (token == null || token.trim().isEmpty()) {
            return false;
        }

        String[] parts = token.split("\\.");
        return parts.length == 3 &&
                !parts[0].isEmpty() &&
                !parts[1].isEmpty() &&
                !parts[2].isEmpty();
    }

    /**
     * Enviar respuesta de error en formato JSON
     */
    private void sendErrorResponse(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String jsonResponse = String.format(
            "{\"error\": \"%s\", \"status\": %d, \"timestamp\": \"%s\"}",
            message,
            status,
            java.time.Instant.now().toString()
        );

        response.getWriter().write(jsonResponse);
    }
}