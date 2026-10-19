package dev.shiori.dto.admin;

import java.time.LocalDateTime;

import dev.shiori.domain.enums.NivelBroadcast;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CriarBroadcastRequest(
        @NotBlank(message = "O título é obrigatório")
        @Size(max = 150)
        String titulo,

        @NotBlank(message = "A mensagem é obrigatória")
        String mensagem,

        NivelBroadcast nivel,

        LocalDateTime dataExpiracao
) {}
