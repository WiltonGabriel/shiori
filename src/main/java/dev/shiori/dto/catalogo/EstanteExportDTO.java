package dev.shiori.dto.catalogo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import dev.shiori.domain.enums.StatusLeitura;
import dev.shiori.domain.enums.TipoObra;
import dev.shiori.domain.enums.Visibilidade;

public record EstanteExportDTO(
        String versao,
        LocalDateTime exportadoEm,
        String usuarioApelido,
        int totalItens,
        List<EstanteItemExportDTO> itens
) {
    public record EstanteItemExportDTO(
            String titulo,
            String capaUrl,
            TipoObra tipoObra,
            Double progressoAtual,
            Integer volumeAtual,
            Double totalCapitulos,
            Integer totalVolumes,
            StatusLeitura statusLeitura,
            Visibilidade visibilidade,
            Double nota,
            String urlLeitura,
            Boolean sincronizarMetadados,
            Long obraGlobalId,
            List<HistoricoCicloExportDTO> historicos
    ) {}

    public record HistoricoCicloExportDTO(
            Double capituloInicial,
            Double capituloFinal,
            Double avaliacaoCiclo,
            String anotacoes,
            LocalDate dataLeitura
    ) {}
}
