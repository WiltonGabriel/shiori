package dev.shiori.dto.admin;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AlterarStatusUsuarioRequest(
        @NotNull(message = "O status ativo é obrigatório")
        Boolean ativo,

        @Size(max = 500, message = "O motivo deve ter no máximo 500 caracteres")
        String motivo
) {}
