package dev.shiori.domain.model;

import java.time.LocalDateTime;

import dev.shiori.domain.exception.RegraDeNegocioException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "tb_consentimento_usuario")
public class ConsentimentoUsuario extends EntidadeBase {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(nullable = false, length = 30)
    private String versaoTermosUso;

    @Column(nullable = false, length = 30)
    private String versaoPoliticaPrivacidade;

    @Column(nullable = false)
    private boolean cookiesNecessarios = true;

    @Column(nullable = false)
    private boolean cookiesAnaliticos = false;

    @Column(nullable = false)
    private boolean cookiesMarketing = false;

    @Column(nullable = false)
    private boolean cookiesFuncionais = false;

    @Column(length = 45)
    private String ipOrigem;

    @Column(length = 255)
    private String userAgent;

    @Column(nullable = false)
    private LocalDateTime dataConsentimento;

    @Column(nullable = false)
    private boolean ativo = true;

    protected ConsentimentoUsuario() {
    }

    public ConsentimentoUsuario(
            Usuario usuario,
            String versaoTermosUso,
            String versaoPoliticaPrivacidade,
            boolean cookiesAnaliticos,
            boolean cookiesMarketing,
            boolean cookiesFuncionais,
            String ipOrigem,
            String userAgent) {
        if (usuario == null) {
            throw new RegraDeNegocioException("O usuário titular do consentimento é obrigatório");
        }
        if (versaoTermosUso == null || versaoTermosUso.isBlank()) {
            throw new RegraDeNegocioException("A versão dos termos de uso é obrigatória");
        }
        if (versaoPoliticaPrivacidade == null || versaoPoliticaPrivacidade.isBlank()) {
            throw new RegraDeNegocioException("A versão da política de privacidade é obrigatória");
        }

        this.usuario = usuario;
        this.versaoTermosUso = versaoTermosUso.trim();
        this.versaoPoliticaPrivacidade = versaoPoliticaPrivacidade.trim();
        this.cookiesNecessarios = true;
        this.cookiesAnaliticos = cookiesAnaliticos;
        this.cookiesMarketing = cookiesMarketing;
        this.cookiesFuncionais = cookiesFuncionais;
        this.ipOrigem = ipOrigem != null ? ipOrigem.trim() : null;
        this.userAgent = userAgent != null ? userAgent.trim() : null;
        this.dataConsentimento = LocalDateTime.now();
        this.ativo = true;
    }

    public void revogar() {
        this.ativo = false;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public String getVersaoTermosUso() {
        return versaoTermosUso;
    }

    public String getVersaoPoliticaPrivacidade() {
        return versaoPoliticaPrivacidade;
    }

    public boolean isCookiesNecessarios() {
        return cookiesNecessarios;
    }

    public boolean isCookiesAnaliticos() {
        return cookiesAnaliticos;
    }

    public boolean isCookiesMarketing() {
        return cookiesMarketing;
    }

    public boolean isCookiesFuncionais() {
        return cookiesFuncionais;
    }

    public String getIpOrigem() {
        return ipOrigem;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public LocalDateTime getDataConsentimento() {
        return dataConsentimento;
    }

    public boolean isAtivo() {
        return ativo;
    }
}
