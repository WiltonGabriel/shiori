package dev.shiori.dto.usuario;

import java.util.Set;

import dev.shiori.domain.enums.TipoObra;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ConcluirOnboardingRequest(
        @NotBlank(message = "O apelido é obrigatório")
        @Size(min = 3, max = 50, message = "O apelido deve conter entre 3 e 50 caracteres")
        String apelido,

        String fusoHorario,
        String fotoUrl,
        String descricao,
        Set<TipoObra> formatosPreferidos
) {
}
