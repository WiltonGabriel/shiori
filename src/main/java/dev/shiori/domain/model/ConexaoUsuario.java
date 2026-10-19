package dev.shiori.domain.model;

import java.time.LocalDateTime;
import java.util.Objects;

import dev.shiori.domain.enums.StatusConexao;
import dev.shiori.domain.exception.RegraDeNegocioException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
        name = "tb_conexao_usuario",
        uniqueConstraints = @UniqueConstraint(name = "uk_conexao_solicitante_recebedor", columnNames = {"solicitante_id", "recebedor_id"})
)
public class ConexaoUsuario extends EntidadeBase {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "solicitante_id", nullable = false)
    private Usuario solicitante;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recebedor_id", nullable = false)
    private Usuario recebedor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusConexao status = StatusConexao.PENDENTE;

    @Column(nullable = false)
    private LocalDateTime dataCriacao;

    private LocalDateTime dataAtualizacao;

    protected ConexaoUsuario() {
    }

    public ConexaoUsuario(Usuario solicitante, Usuario recebedor) {
        if (solicitante == null) {
            throw new RegraDeNegocioException("O usuário solicitante é obrigatório");
        }
        if (recebedor == null) {
            throw new RegraDeNegocioException("O usuário recebedor é obrigatório");
        }
        if (solicitante.equals(recebedor)) {
            throw new RegraDeNegocioException("Um usuário não pode estabelecer uma conexão consigo mesmo");
        }

        this.solicitante = solicitante;
        this.recebedor = recebedor;
        this.status = StatusConexao.PENDENTE;
        this.dataCriacao = LocalDateTime.now();
        this.dataAtualizacao = LocalDateTime.now();
    }

    public void aceitar() {
        if (this.status == StatusConexao.BLOQUEADA) {
            throw new RegraDeNegocioException("Uma conexão bloqueada não pode ser aceita diretamente");
        }
        this.status = StatusConexao.ACEITA;
        this.dataAtualizacao = LocalDateTime.now();
    }

    public void bloquear() {
        this.status = StatusConexao.BLOQUEADA;
        this.dataAtualizacao = LocalDateTime.now();
    }

    public boolean isAceita() {
        return this.status == StatusConexao.ACEITA;
    }

    public boolean isPendente() {
        return this.status == StatusConexao.PENDENTE;
    }

    public boolean isBloqueada() {
        return this.status == StatusConexao.BLOQUEADA;
    }

    public boolean envolve(Usuario usuario) {
        if (usuario == null) {
            return false;
        }
        return usuario.equals(solicitante) || usuario.equals(recebedor);
    }

    public Usuario obterOutroUsuario(Usuario usuario) {
        if (!envolve(usuario)) {
            throw new RegraDeNegocioException("O usuário informado não faz parte desta conexão");
        }
        return usuario.equals(solicitante) ? recebedor : solicitante;
    }

    public Usuario getSolicitante() {
        return solicitante;
    }

    public Usuario getRecebedor() {
        return recebedor;
    }

    public StatusConexao getStatus() {
        return status;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public LocalDateTime getDataAtualizacao() {
        return dataAtualizacao;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConexaoUsuario that = (ConexaoUsuario) o;
        if (getId() != null && that.getId() != null) {
            return getId().equals(that.getId());
        }
        return Objects.equals(solicitante, that.solicitante) && Objects.equals(recebedor, that.recebedor);
    }

    @Override
    public int hashCode() {
        if (getId() != null) {
            return getId().hashCode();
        }
        return Objects.hash(solicitante, recebedor);
    }
}
