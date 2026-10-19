package dev.shiori.infra.integration;

import java.util.List;

public record IntegracaoMetadadosObra(
        String provedorCodigo,
        String idExterno,
        String tituloPrincipal,
        String tituloOriginal,
        String sinopse,
        String capaUrl,
        Integer totalCapitulosLancados,
        Integer totalVolumes,
        String tipoObra,
        List<String> generos,
        List<String> tags
) {
    public IntegracaoMetadadosObra(
            String idExterno,
            String tituloPrincipal,
            String tituloOriginal,
            String sinopse,
            String capaUrl,
            Integer totalCapitulosLancados,
            Integer totalVolumes,
            String tipoObra,
            List<String> generos,
            List<String> tags
    ) {
        this(null, idExterno, tituloPrincipal, tituloOriginal, sinopse, capaUrl, totalCapitulosLancados, totalVolumes, tipoObra, generos, tags);
    }
}
