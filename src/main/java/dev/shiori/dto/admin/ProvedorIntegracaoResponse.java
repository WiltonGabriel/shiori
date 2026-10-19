package dev.shiori.dto.admin;

import java.time.LocalDate;
import java.time.LocalDateTime;

import dev.shiori.domain.model.ProvedorIntegracao;

public record ProvedorIntegracaoResponse(
        Long id,
        String codigo,
        String nomeExibicao,
        String urlBase,
        boolean ativo,
        long requisicoesHoje,
        long limiteDiario,
        LocalDate dataUltimoReset,
        LocalDateTime dataUltimaRequisicao
) {
    public static ProvedorIntegracaoResponse deEntidade(ProvedorIntegracao p) {
        return new ProvedorIntegracaoResponse(
                p.getId(),
                p.getCodigo(),
                p.getNomeExibicao(),
                p.getUrlBase(),
                p.isAtivo(),
                p.getRequisicoesHoje(),
                p.getLimiteDiario(),
                p.getDataUltimoReset(),
                p.getDataUltimaRequisicao()
        );
    }
}
