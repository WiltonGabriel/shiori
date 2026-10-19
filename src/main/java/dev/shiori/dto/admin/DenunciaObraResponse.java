package dev.shiori.dto.admin;

import java.time.LocalDateTime;

import dev.shiori.domain.enums.MotivoDenuncia;
import dev.shiori.domain.enums.StatusDenuncia;
import dev.shiori.domain.model.DenunciaObra;

public record DenunciaObraResponse(
        Long id,
        Long obraId,
        String obraTitulo,
        Long denuncianteId,
        String denuncianteApelido,
        MotivoDenuncia motivo,
        String detalhes,
        StatusDenuncia status,
        LocalDateTime dataCriacao,
        LocalDateTime dataResolucao,
        String parecerModeracao
) {
    public static DenunciaObraResponse deEntidade(DenunciaObra d) {
        return new DenunciaObraResponse(
                d.getId(),
                d.getObra() != null ? d.getObra().getId() : null,
                d.getObra() != null ? d.getObra().getTituloPrincipal() : null,
                d.getUsuarioDenunciante() != null ? d.getUsuarioDenunciante().getId() : null,
                d.getUsuarioDenunciante() != null ? d.getUsuarioDenunciante().getApelidoOuEmail() : null,
                d.getMotivo(),
                d.getDetalhes(),
                d.getStatus(),
                d.getDataCriacao(),
                d.getDataResolucao(),
                d.getParecerModeracao()
        );
    }
}
