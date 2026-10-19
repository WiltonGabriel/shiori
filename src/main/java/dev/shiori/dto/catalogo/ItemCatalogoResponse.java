package dev.shiori.dto.catalogo;

import java.time.LocalDateTime;
import java.util.List;

import dev.shiori.domain.enums.StatusLeitura;
import dev.shiori.domain.enums.Visibilidade;
import dev.shiori.domain.model.ItemCatalogo;

public record ItemCatalogoResponse(
        Long id,
        Long obraId,
        String obraTitulo,
        String obraCapaUrl,
        Double progressoAtual,
        Double volumeAtual,
        StatusLeitura statusLeitura,
        Double avaliacaoPessoal,
        Visibilidade visibilidade,
        String urlLeitura,
        Double mediaAvaliacaoCiclos,
        LocalDateTime dataAdicao,
        LocalDateTime ultimaAtualizacao,
        List<HistoricoLeituraResponse> historicosLeitura
) {
    public static ItemCatalogoResponse aPartirDe(ItemCatalogo item, boolean incluirUrlLeitura) {
        List<HistoricoLeituraResponse> historicos = item.getHistoricosLeitura() != null
                ? item.getHistoricosLeitura().stream().map(HistoricoLeituraResponse::aPartirDe).toList()
                : List.of();

        return new ItemCatalogoResponse(
                item.getId(),
                item.getObra() != null ? item.getObra().getId() : null,
                item.getObra() != null ? item.getObra().getTituloPrincipal() : null,
                item.getObra() != null ? item.getObra().getCapaUrl() : null,
                item.getProgressoAtual(),
                item.getVolumeAtual(),
                item.getStatusLeitura(),
                item.getAvaliacaoPessoal(),
                item.getVisibilidade(),
                incluirUrlLeitura ? item.getUrlLeitura() : null,
                item.getMediaAvaliacaoCiclos(),
                item.getDataAdicao(),
                item.getUltimaAtualizacao(),
                historicos
        );
    }
}
