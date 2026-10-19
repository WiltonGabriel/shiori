package dev.shiori.dto.admin;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AvaliarSugestaoRequest(
        @NotNull(message = "A decisão de aprovação é obrigatória")
        Boolean aprovado,

        @Size(max = 500, message = "O parecer deve ter no máximo 500 caracteres")
        String parecer
) {}
