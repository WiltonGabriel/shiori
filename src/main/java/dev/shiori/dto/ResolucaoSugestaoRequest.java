package dev.shiori.dto;

import dev.shiori.domain.enums.StatusSugestao;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ResolucaoSugestaoRequest(
        @NotNull(message = "O status de resolução é obrigatório")
        StatusSugestao status,

        @Size(max = 1000, message = "O parecer do administrador não pode exceder 1000 caracteres")
        String parecerAdmin) {
}
