package dev.shiori.dto.usuario;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record AlterarEmailRequest(
        @NotBlank(message = "O novo e-mail é obrigatório")
        @Email(message = "Formato de e-mail inválido")
        String novoEmail,

        @NotBlank(message = "A senha atual é obrigatória")
        String senhaAtual
) {
}
