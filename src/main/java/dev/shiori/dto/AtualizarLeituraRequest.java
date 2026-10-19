package dev.shiori.dto;

import dev.shiori.domain.enums.StatusLeitura;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;

public record AtualizarLeituraRequest(
        StatusLeitura statusLeitura,

        @DecimalMin(value = "0.0", message = "A avaliação pessoal deve ser no mínimo 0.0")
        @DecimalMax(value = "10.0", message = "A avaliação pessoal deve ser no máximo 10.0")
        Double avaliacaoPessoal) {
}
