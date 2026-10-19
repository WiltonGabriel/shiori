package dev.shiori.dto.admin;

public record AdminDashboardResponse(
        long totalUsuarios,
        long totalUsuariosAtivos,
        long totalObras,
        long totalItensEstante,
        long totalSugestoesPendentes,
        long totalDenunciasComentariosPendentes,
        long totalDenunciasObrasPendentes
) {}
