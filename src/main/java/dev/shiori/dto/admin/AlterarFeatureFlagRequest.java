package dev.shiori.dto.admin;

import jakarta.validation.constraints.NotNull;

public record AlterarFeatureFlagRequest(
        @NotNull(message = "O status ativado é obrigatório")
        Boolean ativado
) {}
