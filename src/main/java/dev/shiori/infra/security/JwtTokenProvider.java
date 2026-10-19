package dev.shiori.infra.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import dev.shiori.domain.model.Usuario;
import dev.shiori.infra.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtTokenProvider {

    private final long expirationMs;
    private final SecretKey key;

    @Autowired
    public JwtTokenProvider(JwtProperties jwtProperties) {
        this(jwtProperties.getSecret(), jwtProperties.getExpirationMs());
    }

    public JwtTokenProvider(String secret, long expirationMs) {
        this.expirationMs = expirationMs;
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String gerarToken(Usuario usuario) {
        return gerarToken(
                usuario.getId(),
                usuario.getEmail(),
                usuario.getRole() != null ? usuario.getRole().name() : "ROLE_USER",
                usuario.isOnboardingConcluido());
    }

    public String gerarToken(Long usuarioId, String email, String role, boolean onboardingConcluido) {
        Date agora = new Date();
        Date expiracao = new Date(agora.getTime() + expirationMs);

        return Jwts.builder()
                .subject(email)
                .claim("id", usuarioId)
                .claim("role", role)
                .claim("onboardingConcluido", onboardingConcluido)
                .issuedAt(agora)
                .expiration(expiracao)
                .signWith(key)
                .compact();
    }

    public boolean validarToken(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }
        try {
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public Claims extrairClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String extrairEmail(String token) {
        return extrairClaims(token).getSubject();
    }

    public Long extrairUsuarioId(String token) {
        Object idObj = extrairClaims(token).get("id");
        if (idObj instanceof Number number) {
            return number.longValue();
        }
        return null;
    }

    public String extrairRole(String token) {
        Object roleObj = extrairClaims(token).get("role");
        return roleObj != null ? roleObj.toString() : "ROLE_USER";
    }

    public boolean extrairOnboardingConcluido(String token) {
        Object onboarding = extrairClaims(token).get("onboardingConcluido");
        return Boolean.TRUE.equals(onboarding);
    }
}
