package dev.shiori.dto.catalogo;

import java.util.List;

import jakarta.validation.constraints.NotNull;

public record EstanteImportRequest(
        @NotNull(message = "A lista de itens a importar é obrigatória")
        List<EstanteExportDTO.EstanteItemExportDTO> itens,
        Boolean substituirExistentes
) {
    public boolean deveSubstituirExistentes() {
        return substituirExistentes != null && substituirExistentes;
    }
}
