package dev.shiori.dto.federacao;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record NoFederadoRequest(
        @NotBlank(message = "O nome do nó federado é obrigatório")
        @Size(max = 100, message = "O nome do nó deve ter no máximo 100 caracteres")
        String nome,

        @NotBlank(message = "A URL do Hub vizinho é obrigatória")
        @Size(max = 255, message = "A URL do Hub deve ter no máximo 255 caracteres")
        String urlHub,

        @NotBlank(message = "O segredo compartilhado é obrigatório")
        @Size(max = 128, message = "O segredo compartilhado deve ter no máximo 128 caracteres")
        String segredoCompartilhado
) {}
