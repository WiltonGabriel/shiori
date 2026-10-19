package dev.shiori.domain.model;

import java.time.LocalDateTime;

import dev.shiori.domain.enums.NivelBroadcast;
import dev.shiori.domain.exception.RegraDeNegocioException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "tb_broadcast_sistema")
public class BroadcastSistema extends EntidadeBase {

    @Column(nullable = false, length = 150)
    private String titulo;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String mensagem;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private NivelBroadcast nivel = NivelBroadcast.INFO;

    @Column(nullable = false)
    private boolean ativo = true;

    @Column(nullable = false)
    private LocalDateTime dataCriacao;

    private LocalDateTime dataExpiracao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_criador_id")
    private Usuario adminCriador;

    protected BroadcastSistema() {
    }

    public BroadcastSistema(String titulo, String mensagem, NivelBroadcast nivel, LocalDateTime dataExpiracao, Usuario adminCriador) {
        if (titulo == null || titulo.isBlank()) {
            throw new RegraDeNegocioException("O título da notificação global é obrigatório");
        }
        if (mensagem == null || mensagem.isBlank()) {
            throw new RegraDeNegocioException("A mensagem da notificação global é obrigatória");
        }

        this.titulo = titulo.trim();
        this.mensagem = mensagem.trim();
        this.nivel = nivel != null ? nivel : NivelBroadcast.INFO;
        this.ativo = true;
        this.dataCriacao = LocalDateTime.now();
        this.dataExpiracao = dataExpiracao;
        this.adminCriador = adminCriador;
    }

    public void desativar() {
        this.ativo = false;
    }

    public boolean isVigente() {
        if (!this.ativo) {
            return false;
        }
        if (this.dataExpiracao != null && LocalDateTime.now().isAfter(this.dataExpiracao)) {
            return false;
        }
        return true;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getMensagem() {
        return mensagem;
    }

    public NivelBroadcast getNivel() {
        return nivel;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public LocalDateTime getDataExpiracao() {
        return dataExpiracao;
    }

    public Usuario getAdminCriador() {
        return adminCriador;
    }
}
