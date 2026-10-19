package dev.shiori.dto.catalogo;

import jakarta.validation.constraints.NotNull;

public record VincularObraRequest(
        @NotNull(message = "O ID da obra global é obrigatório")
        Long obraId,
        Boolean sincronizarMetadados
) {
    public boolean sincronizarOuPadrao() {
        return sincronizarMetadados != null ? sincronizarMetadados : true;
    }
}
