package dev.shiori.dto.hub;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SolicitacaoPareamentoRequest(
        @NotBlank(message = "O UUID da instância é obrigatório")
        @Size(max = 64, message = "O UUID da instância deve ter no máximo 64 caracteres")
        String instanceUuid,

        @NotBlank(message = "O apelido público da instância é obrigatório")
        @Size(max = 60, message = "O apelido público deve ter no máximo 60 caracteres")
        String apelidoPublico
) {}
