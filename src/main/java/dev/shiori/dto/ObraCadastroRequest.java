package dev.shiori.dto;

import dev.shiori.domain.enums.TipoObra;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ObraCadastroRequest(
        @NotBlank(message = "O título principal é obrigatório")
        @Size(max = 150, message = "O título principal não pode exceder 150 caracteres")
        String tituloPrincipal,

        @Size(max = 150, message = "O título original não pode exceder 150 caracteres")
        String tituloOriginal,

        @Size(max = 500, message = "A URL da capa não pode exceder 500 caracteres")
        String capaUrl,

        String sinopse,

        @Min(value = 0, message = "O total de capítulos não pode ser negativo")
        Integer totalCapitulosLancados,

        TipoObra tipoObra) {
}
