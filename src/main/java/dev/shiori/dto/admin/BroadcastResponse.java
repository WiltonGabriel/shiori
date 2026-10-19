package dev.shiori.dto.admin;

import java.time.LocalDateTime;

import dev.shiori.domain.enums.NivelBroadcast;
import dev.shiori.domain.model.BroadcastSistema;

public record BroadcastResponse(
        Long id,
        String titulo,
        String mensagem,
        NivelBroadcast nivel,
        boolean ativo,
        LocalDateTime dataCriacao,
        LocalDateTime dataExpiracao,
        String adminCriadorApelido
) {
    public static BroadcastResponse deEntidade(BroadcastSistema b) {
        return new BroadcastResponse(
                b.getId(),
                b.getTitulo(),
                b.getMensagem(),
                b.getNivel(),
                b.isAtivo(),
                b.getDataCriacao(),
                b.getDataExpiracao(),
                b.getAdminCriador() != null ? b.getAdminCriador().getApelidoOuEmail() : "Sistema"
        );
    }
}
