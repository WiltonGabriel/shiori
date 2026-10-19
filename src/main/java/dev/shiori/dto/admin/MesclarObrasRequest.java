package dev.shiori.dto.admin;

import jakarta.validation.constraints.NotNull;

public record MesclarObrasRequest(
        @NotNull(message = "O ID da obra duplicada é obrigatório")
        Long obraDuplicadaId,

        @NotNull(message = "O ID da obra principal é obrigatório")
        Long obraPrincipalId
) {}
