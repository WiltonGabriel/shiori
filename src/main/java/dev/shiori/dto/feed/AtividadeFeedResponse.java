package dev.shiori.dto.feed;

import java.time.LocalDateTime;

import dev.shiori.domain.enums.TipoAtividadeFeed;
import dev.shiori.domain.model.AtividadeFeed;

public record AtividadeFeedResponse(
        Long id,
        Long usuarioId,
        String usuarioApelido,
        String usuarioFotoUrl,
        TipoAtividadeFeed tipoAtividade,
        Long obraId,
        String obraTitulo,
        String obraCapaUrl,
        String descricao,
        LocalDateTime dataCriacao
) {
    public static AtividadeFeedResponse aPartirDe(AtividadeFeed atividade) {
        String usuarioApelido = atividade.getUsuario() != null ? atividade.getUsuario().getApelidoOuEmail() : atividade.getUsuarioApelidoSnapshot();
        String usuarioFotoUrl = atividade.getUsuario() != null ? atividade.getUsuario().getFotoUrl() : atividade.getUsuarioFotoUrlSnapshot();
        String obraTitulo = atividade.getObra() != null ? atividade.getObra().getTituloPrincipal() : atividade.getObraTituloSnapshot();
        String obraCapaUrl = atividade.getObra() != null ? atividade.getObra().getCapaUrl() : atividade.getObraCapaUrlSnapshot();

        return new AtividadeFeedResponse(
                atividade.getId(),
                atividade.getUsuario() != null ? atividade.getUsuario().getId() : null,
                usuarioApelido,
                usuarioFotoUrl,
                atividade.getTipoAtividade(),
                atividade.getObra() != null ? atividade.getObra().getId() : null,
                obraTitulo,
                obraCapaUrl,
                atividade.getDescricao(),
                atividade.getDataCriacao()
        );
    }
}
