package dev.shiori.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record NovoCicloRequest(
        @NotNull(message = "O capítulo atingido é obrigatório")
        @PositiveOrZero(message = "O capítulo atingido não pode ser negativo")
        Double capituloAtingido,

        @DecimalMin(value = "0.0", message = "A nota deve ser no mínimo 0.0")
        @DecimalMax(value = "10.0", message = "A nota deve ser no máximo 10.0")
        Double nota,

        String anotacoes) {
}
