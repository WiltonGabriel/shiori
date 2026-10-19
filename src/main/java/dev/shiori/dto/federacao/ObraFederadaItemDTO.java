package dev.shiori.dto.federacao;

import dev.shiori.domain.enums.TipoObra;

public record ObraFederadaItemDTO(
        String tituloPrincipal,
        String tituloOriginal,
        String capaUrl,
        String sinopse,
        Integer totalCapitulosLancados,
        Integer totalVolumes,
        TipoObra tipoObra,
        String mangadexId,
        String anilistId,
        String kitsuId,
        String isbn,
        String origemFonte
) {}
