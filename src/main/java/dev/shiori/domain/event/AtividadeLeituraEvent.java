package dev.shiori.domain.event;

import dev.shiori.domain.enums.TipoAtividadeFeed;

public record AtividadeLeituraEvent(
        Long usuarioId,
        Long obraId,
        Long itemCatalogoId,
        TipoAtividadeFeed tipoAtividade,
        String descricao
) {
}
