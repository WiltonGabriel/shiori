package dev.shiori.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ConsentimentoRequest(
        @NotBlank(message = "A versão dos termos de uso é obrigatória")
        @Size(max = 30, message = "A versão dos termos não pode exceder 30 caracteres")
        String versaoTermosUso,

        @NotBlank(message = "A versão da política de privacidade é obrigatória")
        @Size(max = 30, message = "A versão da política não pode exceder 30 caracteres")
        String versaoPoliticaPrivacidade,

        boolean cookiesAnaliticos,
        boolean cookiesMarketing,
        boolean cookiesFuncionais) {
}
