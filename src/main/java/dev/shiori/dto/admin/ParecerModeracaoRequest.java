package dev.shiori.dto.admin;

import jakarta.validation.constraints.Size;

public record ParecerModeracaoRequest(
        @Size(max = 500, message = "O parecer deve ter no máximo 500 caracteres")
        String parecer
) {}
