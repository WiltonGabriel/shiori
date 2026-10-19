package dev.shiori.dto.usuario;

import jakarta.validation.constraints.Size;

public record AtualizarPerfilRequest(
        @Size(min = 3, max = 50, message = "O apelido deve conter entre 3 e 50 caracteres")
        String apelido,

        String fotoUrl,
        String descricao,
        String fusoHorario
) {
}
