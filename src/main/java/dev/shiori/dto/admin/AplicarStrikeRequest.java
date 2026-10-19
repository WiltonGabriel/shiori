package dev.shiori.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AplicarStrikeRequest(
        @NotBlank(message = "O motivo do strike é obrigatório")
        @Size(max = 500)
        String motivo
) {}
