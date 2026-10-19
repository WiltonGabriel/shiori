package dev.shiori.dto.hub;

import dev.shiori.domain.enums.StatusPareamento;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record DecisaoPareamentoRequest(
        @NotNull(message = "O status de decisão é obrigatório (AUTORIZADO, SUSPENSO, BANIDO)")
        StatusPareamento status,

        @Size(max = 255, message = "O motivo deve ter no máximo 255 caracteres")
        String motivo,

        Integer diasSuspensao
) {}
