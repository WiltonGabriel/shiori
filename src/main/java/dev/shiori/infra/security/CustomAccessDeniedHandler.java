package dev.shiori.infra.security;

import java.io.IOException;
import java.time.Instant;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException) throws IOException {

        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);

        String json = String.format("""
                {
                  "type": "https://shiori.dev/erros/acesso-proibido",
                  "title": "Acesso Proibido",
                  "status": 403,
                  "detail": "Acesso proibido. Você não possui as permissões necessárias para acessar este recurso.",
                  "path": "%s",
                  "timestamp": "%s"
                }
                """, request.getRequestURI(), Instant.now());

        response.getWriter().write(json);
    }
}
