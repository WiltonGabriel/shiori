package dev.shiori.domain.model;

import java.time.LocalDateTime;

import dev.shiori.domain.exception.RegraDeNegocioException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "tb_feature_flag")
public class FeatureFlag extends EntidadeBase {

    @Column(nullable = false, unique = true, length = 60)
    private String chave;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(length = 255)
    private String descricao;

    @Column(nullable = false)
    private boolean ativado = false;

    @Column(nullable = false)
    private LocalDateTime dataAtualizacao;

    protected FeatureFlag() {
    }

    public FeatureFlag(String chave, String nome, String descricao, boolean ativado) {
        if (chave == null || chave.isBlank()) {
            throw new RegraDeNegocioException("A chave da feature flag é obrigatória");
        }
        if (nome == null || nome.isBlank()) {
            throw new RegraDeNegocioException("O nome da feature flag é obrigatório");
        }

        this.chave = chave.trim().toUpperCase();
        this.nome = nome.trim();
        this.descricao = descricao != null ? descricao.trim() : null;
        this.ativado = ativado;
        this.dataAtualizacao = LocalDateTime.now();
    }

    public void alternarStatus(boolean ativado) {
        this.ativado = ativado;
        this.dataAtualizacao = LocalDateTime.now();
    }

    public String getChave() {
        return chave;
    }

    public String getNome() {
        return nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public boolean isAtivado() {
        return ativado;
    }

    public LocalDateTime getDataAtualizacao() {
        return dataAtualizacao;
    }
}
