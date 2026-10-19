package dev.shiori.domain.event;

import dev.shiori.domain.enums.TipoAtividadeFeed;

public record AtividadeLeituraEvent(
        Long usuarioId,
        Long obraId,
        Long itemCatalogoId,
        TipoAtividadeFeed tipoAtividade,
        String descricao,
        String obraTituloSnapshot,
        String obraCapaUrlSnapshot,
        Double capituloSnapshot,
        Double notaSnapshot
) {
    public AtividadeLeituraEvent(Long usuarioId, Long obraId, Long itemCatalogoId, TipoAtividadeFeed tipoAtividade, String descricao) {
        this(usuarioId, obraId, itemCatalogoId, tipoAtividade, descricao, null, null, null, null);
    }
}

