package dev.shiori.dto.catalogo;

import dev.shiori.domain.enums.Visibilidade;
import jakarta.validation.constraints.NotNull;

public record AtualizarVisibilidadeRequest(
        @NotNull(message = "A visibilidade é obrigatória")
        Visibilidade visibilidade
) {
}
