package dev.shiori.dto;

import java.util.HashSet;
import java.util.Set;

import dev.shiori.domain.enums.StatusCatalogo;
import dev.shiori.domain.enums.StatusPublicacao;
import dev.shiori.domain.enums.TipoObra;
import dev.shiori.domain.model.Obra;

public record ObraResponse(
        Long id,
        String tituloPrincipal,
        String tituloOriginal,
        String capaUrl,
        String sinopse,
        Integer totalCapitulosLancados,
        Double notaGeral,
        TipoObra tipoObra,
        StatusCatalogo statusCatalogo,
        StatusPublicacao statusPublicacao,
        Long criadoPorUsuarioId,
        Set<String> generos,
        Set<String> tags,
        String origemFonte) {

    public ObraResponse(
            Long id,
            String tituloPrincipal,
            String tituloOriginal,
            String capaUrl,
            String sinopse,
            Integer totalCapitulosLancados,
            Double notaGeral,
            TipoObra tipoObra,
            StatusCatalogo statusCatalogo,
            StatusPublicacao statusPublicacao,
            Long criadoPorUsuarioId,
            Set<String> generos,
            Set<String> tags) {
        this(id, tituloPrincipal, tituloOriginal, capaUrl, sinopse, totalCapitulosLancados, notaGeral, tipoObra, statusCatalogo, statusPublicacao, criadoPorUsuarioId, generos, tags, "SHIORI_INTERNO");
    }

    public static ObraResponse fromEntity(Obra obra) {
        Set<String> generos = new HashSet<>();
        if (obra.getGeneros() != null) {
            for (var g : obra.getGeneros()) {
                if (g != null && g.getNome() != null) {
                    generos.add(g.getNome());
                }
            }
        }

        Set<String> tags = new HashSet<>();
        if (obra.getTagsVisiveis() != null) {
            for (var t : obra.getTagsVisiveis()) {
                if (t != null && t.getNome() != null) {
                    tags.add(t.getNome());
                }
            }
        }

        return new ObraResponse(
                obra.getId(),
                obra.getTituloPrincipal(),
                obra.getTituloOriginal(),
                obra.getCapaUrl(),
                obra.getSinopse(),
                obra.getTotalCapitulosLancados(),
                obra.getNotaGeral(),
                obra.getTipoObra(),
                obra.getStatusCatalogo(),
                obra.getStatusPublicacao(),
                obra.getCriadoPorUsuarioId(),
                generos,
                tags,
                obra.getOrigemFonte() != null ? obra.getOrigemFonte() : "SHIORI_INTERNO"
        );
    }
}
