package dev.shiori.dto.usuario;

import java.util.Set;

import dev.shiori.domain.enums.TipoObra;
import jakarta.validation.constraints.NotEmpty;

public record AtualizarFormatosPreferidosRequest(
        @NotEmpty(message = "Selecione ao menos um formato literário de seu interesse")
        Set<TipoObra> formatosPreferidos
) {
}
