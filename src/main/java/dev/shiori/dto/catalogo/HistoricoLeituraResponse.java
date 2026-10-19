package dev.shiori.dto.catalogo;

import java.time.LocalDate;
import java.time.LocalDateTime;

import dev.shiori.domain.model.HistoricoLeitura;

public record HistoricoLeituraResponse(
        Long id,
        Double capituloInicial,
        Double capituloFinal,
        Double avaliacaoCiclo,
        String anotacoes,
        LocalDate dataLeitura,
        LocalDateTime ultimaAtualizacao
) {
    public static HistoricoLeituraResponse aPartirDe(HistoricoLeitura historico) {
        return new HistoricoLeituraResponse(
                historico.getId(),
                historico.getCapituloInicial(),
                historico.getCapituloFinal(),
                historico.getAvaliacaoCiclo(),
                historico.getAnotacoes(),
                historico.getDataLeitura(),
                historico.getUltimaAtualizacao()
        );
    }
}
