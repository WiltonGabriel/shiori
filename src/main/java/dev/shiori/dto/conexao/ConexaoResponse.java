package dev.shiori.dto.conexao;

import java.time.LocalDateTime;

import dev.shiori.domain.enums.StatusConexao;
import dev.shiori.domain.model.ConexaoUsuario;

public record ConexaoResponse(
        Long id,
        Long solicitanteId,
        String solicitanteApelido,
        String solicitanteFotoUrl,
        Long recebedorId,
        String recebedorApelido,
        String recebedorFotoUrl,
        StatusConexao status,
        LocalDateTime dataCriacao,
        LocalDateTime dataAtualizacao
) {
    public static ConexaoResponse aPartirDe(ConexaoUsuario conexao) {
        return new ConexaoResponse(
                conexao.getId(),
                conexao.getSolicitante() != null ? conexao.getSolicitante().getId() : null,
                conexao.getSolicitante() != null ? conexao.getSolicitante().getApelidoOuEmail() : null,
                conexao.getSolicitante() != null ? conexao.getSolicitante().getFotoUrl() : null,
                conexao.getRecebedor() != null ? conexao.getRecebedor().getId() : null,
                conexao.getRecebedor() != null ? conexao.getRecebedor().getApelidoOuEmail() : null,
                conexao.getRecebedor() != null ? conexao.getRecebedor().getFotoUrl() : null,
                conexao.getStatus(),
                conexao.getDataCriacao(),
                conexao.getDataAtualizacao()
        );
    }
}
