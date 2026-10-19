package dev.shiori.dto.auth;

public record AuthResponse(
        String token,
        String tipo,
        Long usuarioId,
        String email,
        String apelido,
        String role,
        boolean onboardingConcluido
) {
    public static AuthResponse of(
            String token,
            Long usuarioId,
            String email,
            String apelido,
            String role,
            boolean onboardingConcluido) {
        return new AuthResponse(
                token,
                "Bearer",
                usuarioId,
                email,
                apelido,
                role,
                onboardingConcluido);
    }
}
