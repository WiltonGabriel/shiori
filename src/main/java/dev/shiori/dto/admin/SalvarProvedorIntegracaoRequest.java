package dev.shiori.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record SalvarProvedorIntegracaoRequest(
        @NotBlank(message = "O código do provedor é obrigatório")
        @Size(max = 50)
        String codigo,

        @NotBlank(message = "O nome de exibição é obrigatório")
        @Size(max = 100)
        String nomeExibicao,

        @NotBlank(message = "A URL base é obrigatória")
        @Size(max = 255)
        String urlBase,

        String apiKey,

        @Positive(message = "O limite diário deve ser positivo")
        Long limiteDiario
) {}
