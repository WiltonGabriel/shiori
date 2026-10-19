package dev.shiori.dto.usuario;

import java.time.LocalDateTime;

import dev.shiori.domain.model.Usuario;

public record UsuarioPerfilResponse(
        Long id,
        String email,
        String apelido,
        String fotoUrl,
        String descricao,
        String fusoHorario,
        String role,
        Integer pontosConfianca,
        boolean onboardingConcluido,
        boolean ativo,
        LocalDateTime dataCriacao,
        LocalDateTime ultimaAtualizacao
) {

    public static UsuarioPerfilResponse aPartirDe(Usuario usuario) {
        return new UsuarioPerfilResponse(
                usuario.getId(),
                usuario.getEmail(),
                usuario.getApelido(),
                usuario.getFotoUrl(),
                usuario.getDescricao(),
                usuario.getFusoHorario(),
                usuario.getRole() != null ? usuario.getRole().name() : null,
                usuario.getPontosConfianca(),
                usuario.isOnboardingConcluido(),
                usuario.isAtivo(),
                usuario.getDataCriacao(),
                usuario.getUltimaAtualizacao()
        );
    }
}
