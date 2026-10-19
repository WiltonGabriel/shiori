package dev.shiori.domain.model;

import java.time.LocalDateTime;

import dev.shiori.domain.enums.TipoVoto;
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
        name = "tb_voto_tag",
        uniqueConstraints = @UniqueConstraint(name = "uk_voto_usuario_tag_obra", columnNames = {"usuario_id", "tag_id", "obra_id"})
)
public class VotoTag extends EntidadeBase {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id", nullable = false)
    private Tag tag;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "obra_id", nullable = false)
    private Obra obra;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoVoto tipoVoto;

    @Column(nullable = false)
    private LocalDateTime dataVoto;

    protected VotoTag() {
    }

    public VotoTag(Usuario usuario, Tag tag, Obra obra, TipoVoto tipoVoto) {
        if (usuario == null) {
            throw new RegraDeNegocioException("O usuário votante é obrigatório");
        }
        if (tag == null) {
            throw new RegraDeNegocioException("A tag é obrigatória");
        }
        if (obra == null) {
            throw new RegraDeNegocioException("A obra é obrigatória");
        }
        if (tipoVoto == null) {
            throw new RegraDeNegocioException("O tipo de voto é obrigatório");
        }

        this.usuario = usuario;
        this.tag = tag;
        this.obra = obra;
        this.tipoVoto = tipoVoto;
        this.dataVoto = LocalDateTime.now();
    }

    public void alterarVoto(TipoVoto novoVoto) {
        if (novoVoto == null) {
            throw new RegraDeNegocioException("O novo tipo de voto não pode ser nulo");
        }
        this.tipoVoto = novoVoto;
        this.dataVoto = LocalDateTime.now();
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public Tag getTag() {
        return tag;
    }

    public Obra getObra() {
        return obra;
    }

    public TipoVoto getTipoVoto() {
        return tipoVoto;
    }

    public LocalDateTime getDataVoto() {
        return dataVoto;
    }
}
