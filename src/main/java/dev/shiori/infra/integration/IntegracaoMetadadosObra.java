package dev.shiori.infra.integration;

import java.util.List;

public record IntegracaoMetadadosObra(
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
) {}
