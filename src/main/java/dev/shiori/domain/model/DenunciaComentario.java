package dev.shiori.domain.model;

import java.time.LocalDateTime;
import java.util.Objects;

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
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
        name = "tb_denuncia_comentario",
        uniqueConstraints = @UniqueConstraint(name = "uk_denuncia_usuario_comentario", columnNames = {"usuario_id", "comentario_id"})
)
public class DenunciaComentario extends EntidadeBase {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comentario_id", nullable = false)
    private Comentario comentario;

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

    protected DenunciaComentario() {
    }

    public DenunciaComentario(Comentario comentario, Usuario usuarioDenunciante, MotivoDenuncia motivo, String detalhes) {
        if (comentario == null) {
            throw new RegraDeNegocioException("O comentário denunciado é obrigatório");
        }
        if (usuarioDenunciante == null) {
            throw new RegraDeNegocioException("O usuário denunciante é obrigatório");
        }
        if (motivo == null) {
            throw new RegraDeNegocioException("O motivo da denúncia é obrigatório");
        }

        this.comentario = comentario;
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

        if (novoStatus == StatusDenuncia.PROCEDENTE && this.comentario != null) {
            this.comentario.desativar();
        }
    }

    public Comentario getComentario() {
        return comentario;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DenunciaComentario that = (DenunciaComentario) o;
        if (getId() != null && that.getId() != null) {
            return getId().equals(that.getId());
        }
        return Objects.equals(comentario, that.comentario) &&
                Objects.equals(usuarioDenunciante, that.usuarioDenunciante);
    }

    @Override
    public int hashCode() {
        if (getId() != null) {
            return getId().hashCode();
        }
        return Objects.hash(comentario, usuarioDenunciante);
    }
}
