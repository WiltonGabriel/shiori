package dev.shiori.dto;

import java.time.LocalDate;
import dev.shiori.domain.model.HistoricoLeitura;

public record HistoricoCicloResponse(Long id, Double capituloInicial, Double capituloFinal, Double tamanhoCiclo,
        boolean capituloUnico, Double avaliacaoCiclo, String anotacoes, LocalDate dataLeitura) {

    public static HistoricoCicloResponse fromEntity(HistoricoLeitura historico) {
        return new HistoricoCicloResponse(
                historico.getId(),
                historico.getCapituloInicial(),
                historico.getCapituloFinal(),
                historico.calcularTamanhoCiclo(),
                historico.isCapituloUnico(),
                historico.getAvaliacaoCiclo(),
                historico.getAnotacoes(),
                historico.getDataLeitura());
    }
}
