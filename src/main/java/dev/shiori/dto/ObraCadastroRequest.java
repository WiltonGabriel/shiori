package dev.shiori.dto;

import dev.shiori.domain.enums.StatusLeitura;
import dev.shiori.domain.enums.StatusObra;

public record ObraCadastroRequest(
        String tituloPrincipal,
        String tituloOriginal,
        String capaUrl,
        String sinopse,
        Double notaGeral,
        StatusObra statusObra,
        StatusLeitura statusLeitura,
        Double avaliacaoPessoal) {
}
