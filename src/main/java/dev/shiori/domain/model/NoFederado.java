package dev.shiori.domain.model;

import java.time.LocalDateTime;

import dev.shiori.domain.exception.RegraDeNegocioException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "tb_no_federado")
public class NoFederado extends EntidadeBase {

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(name = "url_hub", nullable = false, length = 255)
    private String urlHub;

    @Column(name = "segredo_compartilhado", nullable = false, length = 128)
    private String segredoCompartilhado;

    @Column(nullable = false)
    private boolean ativo = true;

    @Column(name = "ultima_sincronizacao")
    private LocalDateTime ultimaSincronizacao;

    @Column(name = "total_obras_recebidas", nullable = false)
    private int totalObrasRecebidas = 0;

    @Column(name = "total_obras_enviadas", nullable = false)
    private int totalObrasEnviadas = 0;

    protected NoFederado() {
    }

    public NoFederado(String nome, String urlHub, String segredoCompartilhado) {
        if (nome == null || nome.isBlank()) {
            throw new RegraDeNegocioException("O nome do nó federado é obrigatório");
        }
        if (urlHub == null || urlHub.isBlank()) {
            throw new RegraDeNegocioException("A URL do Hub vizinho é obrigatória");
        }
        if (segredoCompartilhado == null || segredoCompartilhado.isBlank()) {
            throw new RegraDeNegocioException("O segredo compartilhado para autenticação é obrigatório");
        }

        this.nome = nome.trim();
        this.urlHub = urlHub.trim();
        this.segredoCompartilhado = segredoCompartilhado.trim();
        this.ativo = true;
    }

    public void registrarSincronizacaoRecebida(int quantidade) {
        this.totalObrasRecebidas += quantidade;
        this.ultimaSincronizacao = LocalDateTime.now();
    }

    public void registrarSincronizacaoEnviada(int quantidade) {
        this.totalObrasEnviadas += quantidade;
        this.ultimaSincronizacao = LocalDateTime.now();
    }

    public void alternarStatus(boolean ativo) {
        this.ativo = ativo;
    }

    public String getNome() {
        return nome;
    }

    public String getUrlHub() {
        return urlHub;
    }

    public String getSegredoCompartilhado() {
        return segredoCompartilhado;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public LocalDateTime getUltimaSincronizacao() {
        return ultimaSincronizacao;
    }

    public int getTotalObrasRecebidas() {
        return totalObrasRecebidas;
    }

    public int getTotalObrasEnviadas() {
        return totalObrasEnviadas;
    }
}
