package dev.shiori.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record BanirUsuarioRequest(
        @NotBlank(message = "O motivo do banimento é obrigatório")
        @Size(max = 500, message = "O motivo deve ter no máximo 500 caracteres")
        String motivo
) {}
