package dev.shiori.domain.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import dev.shiori.domain.exception.RegraDeNegocioException;
import dev.shiori.infra.crypto.SensitiveDataCryptoConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "tb_provedor_integracao")
public class ProvedorIntegracao extends EntidadeBase {

    @Column(nullable = false, unique = true, length = 50)
    private String codigo;

    @Column(nullable = false, length = 100)
    private String nomeExibicao;

    @Column(nullable = false, length = 255)
    private String urlBase;

    @Convert(converter = SensitiveDataCryptoConverter.class)
    @Column(columnDefinition = "TEXT")
    private String apiKey;

    @Column(nullable = false)
    private boolean ativo = true;

    @Column(nullable = false)
    private long requisicoesHoje = 0L;

    @Column(nullable = false)
    private long limiteDiario = 10000L;

    private LocalDate dataUltimoReset;

    private LocalDateTime dataUltimaRequisicao;

    protected ProvedorIntegracao() {
    }

    public ProvedorIntegracao(String codigo, String nomeExibicao, String urlBase, String apiKey, long limiteDiario) {
        if (codigo == null || codigo.isBlank()) {
            throw new RegraDeNegocioException("O código do provedor é obrigatório");
        }
        if (nomeExibicao == null || nomeExibicao.isBlank()) {
            throw new RegraDeNegocioException("O nome de exibição é obrigatório");
        }
        if (urlBase == null || urlBase.isBlank()) {
            throw new RegraDeNegocioException("A URL base é obrigatória");
        }

        this.codigo = codigo.trim().toUpperCase();
        this.nomeExibicao = nomeExibicao.trim();
        this.urlBase = urlBase.trim();
        this.apiKey = apiKey != null && !apiKey.isBlank() ? apiKey.trim() : null;
        this.limiteDiario = limiteDiario > 0 ? limiteDiario : 10000L;
        this.ativo = true;
        this.requisicoesHoje = 0L;
        this.dataUltimoReset = LocalDate.now();
    }

    public synchronized void registrarRequisicao() {
        LocalDate hoje = LocalDate.now();
        if (this.dataUltimoReset == null || !this.dataUltimoReset.isEqual(hoje)) {
            this.requisicoesHoje = 0L;
            this.dataUltimoReset = hoje;
        }
        this.requisicoesHoje++;
        this.dataUltimaRequisicao = LocalDateTime.now();
    }

    public boolean atingiuLimiteDiario() {
        LocalDate hoje = LocalDate.now();
        if (this.dataUltimoReset != null && !this.dataUltimoReset.isEqual(hoje)) {
            return false;
        }
        return this.requisicoesHoje >= this.limiteDiario;
    }

    public void atualizarConfiguracao(String nomeExibicao, String urlBase, String apiKey, Long limiteDiario) {
        if (nomeExibicao != null && !nomeExibicao.isBlank()) {
            this.nomeExibicao = nomeExibicao.trim();
        }
        if (urlBase != null && !urlBase.isBlank()) {
            this.urlBase = urlBase.trim();
        }
        if (apiKey != null) {
            this.apiKey = apiKey.isBlank() ? null : apiKey.trim();
        }
        if (limiteDiario != null && limiteDiario > 0) {
            this.limiteDiario = limiteDiario;
        }
    }

    public void alternarStatus(boolean ativo) {
        this.ativo = ativo;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getNomeExibicao() {
        return nomeExibicao;
    }

    public String getUrlBase() {
        return urlBase;
    }

    public String getApiKey() {
        return apiKey;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public long getRequisicoesHoje() {
        return requisicoesHoje;
    }

    public long getLimiteDiario() {
        return limiteDiario;
    }

    public LocalDate getDataUltimoReset() {
        return dataUltimoReset;
    }

    public LocalDateTime getDataUltimaRequisicao() {
        return dataUltimaRequisicao;
    }
}
