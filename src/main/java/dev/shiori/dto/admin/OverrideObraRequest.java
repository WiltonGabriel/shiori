package dev.shiori.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record OverrideObraRequest(
        @NotBlank(message = "O título principal é obrigatório")
        @Size(max = 255)
        String tituloPrincipal,

        @Size(max = 255)
        String tituloOriginal,

        @Size(max = 500)
        String capaUrl,

        String sinopse,

        Integer totalCapitulosLancados,

        Integer totalVolumes
) {}
