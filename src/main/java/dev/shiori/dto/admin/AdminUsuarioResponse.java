package dev.shiori.dto.admin;

import java.time.LocalDateTime;

import dev.shiori.domain.enums.Role;
import dev.shiori.domain.model.Usuario;

public record AdminUsuarioResponse(
        Long id,
        String email,
        String apelido,
        String fotoUrl,
        Role role,
        boolean ativo,
        Integer pontosConfianca,
        LocalDateTime dataCriacao,
        LocalDateTime ultimaAtualizacao
) {
    public static AdminUsuarioResponse deEntidade(Usuario u) {
        return new AdminUsuarioResponse(
                u.getId(),
                u.getEmail(),
                u.getApelido(),
                u.getFotoUrl(),
                u.getRole(),
                u.isAtivo(),
                u.getPontosConfianca(),
                u.getDataCriacao(),
                u.getUltimaAtualizacao()
        );
    }
}
