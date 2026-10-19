package dev.shiori.dto.auth;

import java.util.Set;

import dev.shiori.domain.enums.TipoObra;

public record AuthResponse(
        String token,
        String tipo,
        Long usuarioId,
        String email,
        String apelido,
        String role,
        boolean onboardingConcluido,
        Set<TipoObra> formatosPreferidos
) {
    public static AuthResponse of(
            String token,
            Long usuarioId,
            String email,
            String apelido,
            String role,
            boolean onboardingConcluido,
            Set<TipoObra> formatosPreferidos) {
        return new AuthResponse(
                token,
                "Bearer",
                usuarioId,
                email,
                apelido,
                role,
                onboardingConcluido,
                formatosPreferidos);
    }

    public static AuthResponse of(
            String token,
            Long usuarioId,
            String email,
            String apelido,
            String role,
            boolean onboardingConcluido) {
        return of(token, usuarioId, email, apelido, role, onboardingConcluido, null);
    }
}
