package dev.shiori.dto.hub;

import java.time.LocalDateTime;

import dev.shiori.domain.enums.StatusPareamento;
import dev.shiori.domain.model.PareamentoInstancia;

public record PareamentoResponse(
        Long id,
        String instanceUuid,
        String apelidoPublico,
        StatusPareamento status,
        String statusDescricao,
        String tokenAcesso,
        LocalDateTime dataSolicitacao,
        LocalDateTime dataDecisao,
        String motivoDecisao,
        int submissoesValidas,
        int tentativasSpam,
        LocalDateTime suspensoAte
) {
    public static PareamentoResponse aPartirDe(PareamentoInstancia instancia) {
        return new PareamentoResponse(
                instancia.getId(),
                instancia.getInstanceUuid(),
                instancia.getApelidoPublico(),
                instancia.getStatus(),
                instancia.getStatus().getDescricao(),
                instancia.getTokenAcesso(),
                instancia.getDataSolicitacao(),
                instancia.getDataDecisao(),
                instancia.getMotivoDecisao(),
                instancia.getSubmissoesValidas(),
                instancia.getTentativasSpam(),
                instancia.getSuspensoAte()
        );
    }
}
