package dev.shiori.dto.admin;

public record MesclarObrasResponse(
        Long obraPrincipalId,
        String tituloPrincipal,
        Long obraDuplicadaIdExcluida,
        int itensTransferidos,
        int itensIncorporados,
        int comentariosTransferidos,
        String mensagem
) {}
