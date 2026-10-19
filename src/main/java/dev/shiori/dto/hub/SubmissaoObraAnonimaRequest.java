package dev.shiori.dto.hub;

import dev.shiori.domain.enums.TipoObra;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SubmissaoObraAnonimaRequest(
        @NotBlank(message = "O UUID da instância é obrigatório")
        String instanceUuid,

        @NotBlank(message = "O apelido público da instância é obrigatório")
        String apelidoPublico,

        @NotBlank(message = "O título principal da obra é obrigatório")
        @Size(max = 200, message = "O título principal deve ter no máximo 200 caracteres")
        String tituloPrincipal,

        @Size(max = 200, message = "O título original deve ter no máximo 200 caracteres")
        String tituloOriginal,

        @Size(max = 500, message = "A URL da capa deve ter no máximo 500 caracteres")
        String capaUrl,

        @Size(max = 2000, message = "A sinopse deve ter no máximo 2000 caracteres")
        String sinopse,

        Integer totalCapitulosLancados,

        Integer totalVolumes,

        @NotNull(message = "O tipo de obra é obrigatório")
        TipoObra tipoObra
) {}
