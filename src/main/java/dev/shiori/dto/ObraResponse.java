package dev.shiori.dto;

import java.util.List;
import dev.shiori.domain.enums.StatusLeitura;
import dev.shiori.domain.enums.StatusObra;
import dev.shiori.domain.model.Obra;

public record ObraResponse(Long id, String tituloPrincipal, String tituloOriginal, String capaUrl,
        String sinopse, Double notaGeral, StatusObra statusObra, StatusLeitura statusLeitura,
        Double avaliacaoPessoal, Double mediaAvaliacaoCiclos, Double progressoAtual,
        List<HistoricoCicloResponse> historicosLeitura) {
    public static ObraResponse fromEntity(Obra obra) {
        return new ObraResponse(
                obra.getId(),
                obra.getTituloPrincipal(),
                obra.getTituloOriginal(),
                obra.getCapaUrl(),
                obra.getSinopse(),
                obra.getNotaGeral(),
                obra.getStatusObra(),
                obra.getStatusLeitura(),
                obra.getAvaliacaoPessoal(),
                obra.getMediaAvaliacaoCiclos(),
                obra.getProgressoAtual(),
                obra.getHistoricosLeitura().stream().map(HistoricoCicloResponse::fromEntity).toList());
    }
}
