package dev.shiori.dto.federacao;

import java.time.LocalDateTime;

import dev.shiori.domain.model.NoFederado;

public record NoFederadoResponse(
        Long id,
        String nome,
        String urlHub,
        boolean ativo,
        LocalDateTime ultimaSincronizacao,
        int totalObrasRecebidas,
        int totalObrasEnviadas
) {
    public static NoFederadoResponse aPartirDe(NoFederado no) {
        return new NoFederadoResponse(
                no.getId(),
                no.getNome(),
                no.getUrlHub(),
                no.isAtivo(),
                no.getUltimaSincronizacao(),
                no.getTotalObrasRecebidas(),
                no.getTotalObrasEnviadas()
        );
    }
}
