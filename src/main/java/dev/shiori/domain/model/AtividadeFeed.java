package dev.shiori.domain.model;

import java.time.LocalDateTime;
import java.util.Objects;

import dev.shiori.domain.enums.TipoAtividadeFeed;
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
@Table(name = "tb_atividade_feed")
public class AtividadeFeed extends EntidadeBase {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoAtividadeFeed tipoAtividade;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "obra_id")
    private Obra obra;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_catalogo_id")
    private ItemCatalogo itemCatalogo;

    @Column(nullable = false, length = 500)
    private String descricao;

    @Column(nullable = false)
    private LocalDateTime dataCriacao;

    protected AtividadeFeed() {
    }

    public AtividadeFeed(
            Usuario usuario,
            TipoAtividadeFeed tipoAtividade,
            Obra obra,
            ItemCatalogo itemCatalogo,
            String descricao) {
        if (usuario == null) {
            throw new RegraDeNegocioException("O usuário autor da atividade é obrigatório");
        }
        if (tipoAtividade == null) {
            throw new RegraDeNegocioException("O tipo de atividade é obrigatório");
        }
        if (descricao == null || descricao.isBlank()) {
            throw new RegraDeNegocioException("A descrição da atividade é obrigatória");
        }

        this.usuario = usuario;
        this.tipoAtividade = tipoAtividade;
        this.obra = obra;
        this.itemCatalogo = itemCatalogo;
        this.descricao = descricao.trim();
        this.dataCriacao = LocalDateTime.now();
    }

    public static AtividadeFeed registrarInicioLeitura(Usuario usuario, Obra obra, ItemCatalogo item) {
        String msg = String.format("%s começou a ler %s", usuario.getApelidoOuEmail(), obra.getTituloPrincipal());
        return new AtividadeFeed(usuario, TipoAtividadeFeed.INICIOU_LEITURA, obra, item, msg);
    }

    public static AtividadeFeed registrarProgresso(Usuario usuario, Obra obra, ItemCatalogo item, Double capitulo) {
        String msg = String.format(java.util.Locale.US, "%s alcançou o capítulo %.0f de %s", usuario.getApelidoOuEmail(), capitulo, obra.getTituloPrincipal());
        return new AtividadeFeed(usuario, TipoAtividadeFeed.ATUALIZOU_PROGRESSO, obra, item, msg);
    }

    public static AtividadeFeed registrarConclusao(Usuario usuario, Obra obra, ItemCatalogo item) {
        String msg = String.format("%s concluiu a leitura de %s!", usuario.getApelidoOuEmail(), obra.getTituloPrincipal());
        return new AtividadeFeed(usuario, TipoAtividadeFeed.CONCLUIU_LEITURA, obra, item, msg);
    }

    public static AtividadeFeed registrarAvaliacao(Usuario usuario, Obra obra, ItemCatalogo item, Double nota) {
        String msg = String.format(java.util.Locale.US, "%s avaliou %s com nota %.1f/10", usuario.getApelidoOuEmail(), obra.getTituloPrincipal(), nota);
        return new AtividadeFeed(usuario, TipoAtividadeFeed.AVALIOU_OBRA, obra, item, msg);
    }

    public static AtividadeFeed registrarAdicaoEstante(Usuario usuario, Obra obra, ItemCatalogo item) {
        String msg = String.format("%s adicionou %s à sua estante", usuario.getApelidoOuEmail(), obra.getTituloPrincipal());
        return new AtividadeFeed(usuario, TipoAtividadeFeed.ADICIONOU_ESTANTE, obra, item, msg);
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public TipoAtividadeFeed getTipoAtividade() {
        return tipoAtividade;
    }

    public Obra getObra() {
        return obra;
    }

    public ItemCatalogo getItemCatalogo() {
        return itemCatalogo;
    }

    public String getDescricao() {
        return descricao;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(LocalDateTime dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AtividadeFeed that = (AtividadeFeed) o;
        if (getId() != null && that.getId() != null) {
            return getId().equals(that.getId());
        }
        return Objects.equals(usuario, that.usuario) &&
                tipoAtividade == that.tipoAtividade &&
                Objects.equals(descricao, that.descricao) &&
                Objects.equals(dataCriacao, that.dataCriacao);
    }

    @Override
    public int hashCode() {
        if (getId() != null) {
            return getId().hashCode();
        }
        return Objects.hash(usuario, tipoAtividade, descricao, dataCriacao);
    }
}
