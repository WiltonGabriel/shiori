package dev.shiori.dto.admin;

import java.time.LocalDateTime;

import dev.shiori.domain.model.RegistroAuditoria;

public record RegistroAuditoriaResponse(
        Long id,
        Long adminId,
        String adminApelido,
        String acao,
        String entidadeAfetada,
        String entidadeId,
        String detalhes,
        String ipOrigem,
        LocalDateTime dataHora
) {
    public static RegistroAuditoriaResponse deEntidade(RegistroAuditoria r) {
        return new RegistroAuditoriaResponse(
                r.getId(),
                r.getAdminId(),
                r.getAdminApelido(),
                r.getAcao(),
                r.getEntidadeAfetada(),
                r.getEntidadeId(),
                r.getDetalhes(),
                r.getIpOrigem(),
                r.getDataHora()
        );
    }
}
