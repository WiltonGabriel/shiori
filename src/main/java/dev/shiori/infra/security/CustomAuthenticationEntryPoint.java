package dev.shiori.infra.security;

import java.io.IOException;
import java.time.Instant;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException) throws IOException {

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);

        String json = String.format("""
                {
                  "type": "https://shiori.dev/erros/nao-autorizado",
                  "title": "Não Autorizado",
                  "status": 401,
                  "detail": "Acesso não autorizado. É necessário fornecer um token Bearer válido para acessar este recurso.",
                  "path": "%s",
                  "timestamp": "%s"
                }
                """, request.getRequestURI(), Instant.now());

        response.getWriter().write(json);
    }
}
