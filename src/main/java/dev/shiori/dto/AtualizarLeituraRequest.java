package dev.shiori.dto;

import dev.shiori.domain.enums.StatusLeitura;

public record AtualizarLeituraRequest(StatusLeitura statusLeitura, Double avaliacaoPessoal) {

}
