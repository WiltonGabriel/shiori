package dev.shiori.domain.model;

import java.time.LocalDateTime;

import dev.shiori.domain.enums.MotivoDenuncia;
import dev.shiori.domain.enums.StatusDenuncia;
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
@Table(name = "tb_denuncia_obra")
public class DenunciaObra extends EntidadeBase {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "obra_id", nullable = false)
    private Obra obra;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuarioDenunciante;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MotivoDenuncia motivo;

    @Column(columnDefinition = "TEXT")
    private String detalhes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusDenuncia status = StatusDenuncia.PENDENTE;

    @Column(nullable = false)
    private LocalDateTime dataCriacao;

    private LocalDateTime dataResolucao;

    @Column(columnDefinition = "TEXT")
    private String parecerModeracao;

    protected DenunciaObra() {
    }

    public DenunciaObra(Obra obra, Usuario usuarioDenunciante, MotivoDenuncia motivo, String detalhes) {
        if (obra == null) {
            throw new RegraDeNegocioException("A obra denunciada é obrigatória");
        }
        if (usuarioDenunciante == null) {
            throw new RegraDeNegocioException("O usuário denunciante é obrigatório");
        }
        if (motivo == null) {
            throw new RegraDeNegocioException("O motivo da denúncia é obrigatório");
        }

        this.obra = obra;
        this.usuarioDenunciante = usuarioDenunciante;
        this.motivo = motivo;
        this.detalhes = detalhes != null ? detalhes.trim() : null;
        this.status = StatusDenuncia.PENDENTE;
        this.dataCriacao = LocalDateTime.now();
    }

    public void resolverDenuncia(StatusDenuncia novoStatus, String parecerModeracao) {
        if (novoStatus == null || novoStatus == StatusDenuncia.PENDENTE) {
            throw new RegraDeNegocioException("O status de resolução deve ser PROCEDENTE ou IMPROCEDENTE");
        }
        this.status = novoStatus;
        this.parecerModeracao = parecerModeracao != null ? parecerModeracao.trim() : null;
        this.dataResolucao = LocalDateTime.now();
    }

    public Obra getObra() {
        return obra;
    }

    public Usuario getUsuarioDenunciante() {
        return usuarioDenunciante;
    }

    public MotivoDenuncia getMotivo() {
        return motivo;
    }

    public String getDetalhes() {
        return detalhes;
    }

    public StatusDenuncia getStatus() {
        return status;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public LocalDateTime getDataResolucao() {
        return dataResolucao;
    }

    public String getParecerModeracao() {
        return parecerModeracao;
    }
}
