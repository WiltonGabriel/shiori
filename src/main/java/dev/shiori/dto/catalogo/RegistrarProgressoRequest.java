package dev.shiori.dto.catalogo;

import dev.shiori.domain.enums.StatusLeitura;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record RegistrarProgressoRequest(
        @NotNull(message = "O capítulo atual é obrigatório")
        @PositiveOrZero(message = "O capítulo atual deve ser maior ou igual a zero")
        Double capituloAtual,

        @PositiveOrZero(message = "O volume atual deve ser maior ou igual a zero")
        Double volumeAtual,

        @DecimalMin(value = "0.0", message = "A nota deve ser maior ou igual a 0.0")
        @DecimalMax(value = "10.0", message = "A nota deve ser menor ou igual a 10.0")
        Double nota,

        String anotacao,

        StatusLeitura statusLeitura
) {
}
