package dev.shiori.dto.admin;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record MesclarTagsRequest(
        @NotEmpty(message = "A lista de tags de origem não pode ser vazia")
        List<Long> tagOrigemIds,

        @NotNull(message = "A tag de destino canônica é obrigatória")
        Long tagDestinoId
) {}
