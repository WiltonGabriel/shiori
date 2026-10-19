package dev.shiori.dto.admin;

public record MesclarTagsResponse(
        Long tagDestinoId,
        String tagDestinoNome,
        int tagsMescladas,
        int obrasAtualizadas,
        String mensagem
) {}
