package dev.shiori.dto;

import java.time.LocalDateTime;

import dev.shiori.domain.model.ConsentimentoUsuario;

public record ConsentimentoResponse(
        Long id,
        Long usuarioId,
        String versaoTermosUso,
        String versaoPoliticaPrivacidade,
        boolean cookiesNecessarios,
        boolean cookiesAnaliticos,
        boolean cookiesMarketing,
        boolean cookiesFuncionais,
        String ipOrigem,
        LocalDateTime dataConsentimento,
        boolean ativo) {

    public static ConsentimentoResponse fromEntity(ConsentimentoUsuario c) {
        return new ConsentimentoResponse(
                c.getId(),
                c.getUsuario() != null ? c.getUsuario().getId() : null,
                c.getVersaoTermosUso(),
                c.getVersaoPoliticaPrivacidade(),
                c.isCookiesNecessarios(),
                c.isCookiesAnaliticos(),
                c.isCookiesMarketing(),
                c.isCookiesFuncionais(),
                c.getIpOrigem(),
                c.getDataConsentimento(),
                c.isAtivo()
        );
    }
}
