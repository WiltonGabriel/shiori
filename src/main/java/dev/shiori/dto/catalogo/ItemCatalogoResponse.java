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
        Integer obraTotalCapitulos,
        Integer obraTotalVolumes,
        Double progressoAtual,
        Double volumeAtual,
        StatusLeitura statusLeitura,
        Double avaliacaoPessoal,
        Visibilidade visibilidade,
        String urlLeitura,
        Double mediaAvaliacaoCiclos,
        LocalDateTime dataAdicao,
        LocalDateTime ultimaAtualizacao,
        List<HistoricoLeituraResponse> historicosLeitura,
        String tipoObra,
        Boolean sincronizarMetadados,
        Boolean temNovosCapitulos,
        Integer totalCapitulosGlobal,
        Boolean desvinculado
) {
    public String titulo() {
        return obraTitulo();
    }

    public String capaUrl() {
        return obraCapaUrl();
    }

    public Integer totalCapitulos() {
        return obraTotalCapitulos();
    }

    public Integer totalVolumes() {
        return obraTotalVolumes();
    }

    public static ItemCatalogoResponse aPartirDe(ItemCatalogo item, boolean incluirUrlLeitura) {
        List<HistoricoLeituraResponse> historicos = item.getHistoricosLeitura() != null
                ? item.getHistoricosLeitura().stream().map(HistoricoLeituraResponse::aPartirDe).toList()
                : List.of();

        return new ItemCatalogoResponse(
                item.getId(),
                item.getObra() != null ? item.getObra().getId() : null,
                item.getTitulo(),
                item.getCapaUrl(),
                item.getTotalCapitulos(),
                item.getTotalVolumes(),
                item.getProgressoAtual(),
                item.getVolumeAtual(),
                item.getStatusLeitura(),
                item.getAvaliacaoPessoal(),
                item.getVisibilidade(),
                incluirUrlLeitura ? item.getUrlLeitura() : null,
                item.getMediaAvaliacaoCiclos(),
                item.getDataAdicao(),
                item.getUltimaAtualizacao(),
                historicos,
                item.getTipoObra() != null ? item.getTipoObra().name() : null,
                item.isSincronizarMetadados(),
                item.temNovosCapitulosDisponiveis(),
                item.getObra() != null ? item.getObra().getTotalCapitulosLancados() : null,
                item.getObra() == null
        );
    }
}
