package dev.shiori.domain.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import dev.shiori.domain.enums.MotivoDenuncia;
import dev.shiori.domain.exception.RegraDeNegocioException;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "tb_comentario")
public class Comentario extends EntidadeBase {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = true)
    private Usuario autor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "obra_id")
    private Obra obra;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "historico_leitura_id")
    private HistoricoLeitura historicoLeitura;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String texto;

    @Column(nullable = false)
    private boolean contemSpoiler = false;

    @Column(nullable = false)
    private boolean ativo = true;

    @Column(nullable = false)
    private LocalDateTime dataCriacao;

    private LocalDateTime dataAtualizacao;

    @OneToMany(mappedBy = "comentario", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DenunciaComentario> denuncias = new ArrayList<>();

    protected Comentario() {
    }

    public Comentario(Usuario autor, Obra obra, HistoricoLeitura historicoLeitura, String texto, boolean contemSpoiler) {
        if (autor == null) {
            throw new RegraDeNegocioException("O autor do comentário é obrigatório");
        }
        if (texto == null || texto.isBlank()) {
            throw new RegraDeNegocioException("O texto do comentário não pode ser vazio");
        }
        if (obra == null && historicoLeitura == null) {
            throw new RegraDeNegocioException("O comentário deve estar vinculado a uma obra ou a um histórico de leitura");
        }

        this.autor = autor;
        this.obra = obra;
        this.historicoLeitura = historicoLeitura;
        this.texto = texto.trim();
        this.contemSpoiler = contemSpoiler;
        this.ativo = true;
        this.dataCriacao = LocalDateTime.now();
        this.dataAtualizacao = LocalDateTime.now();
    }

    public static Comentario criarParaObra(Usuario autor, Obra obra, String texto, boolean contemSpoiler) {
        if (obra == null) {
            throw new RegraDeNegocioException("A obra é obrigatória para este comentário");
        }
        return new Comentario(autor, obra, null, texto, contemSpoiler);
    }

    public static Comentario criarParaHistorico(Usuario autor, HistoricoLeitura historico, String texto, boolean contemSpoiler) {
        if (historico == null) {
            throw new RegraDeNegocioException("O histórico de leitura é obrigatório para este comentário");
        }
        return new Comentario(autor, null, historico, texto, contemSpoiler);
    }

    public void marcarComoSpoiler() {
        this.contemSpoiler = true;
        this.dataAtualizacao = LocalDateTime.now();
    }

    public void desmarcarSpoiler() {
        this.contemSpoiler = false;
        this.dataAtualizacao = LocalDateTime.now();
    }

    public void editarTexto(String novoTexto) {
        if (novoTexto == null || novoTexto.isBlank()) {
            throw new RegraDeNegocioException("O novo texto do comentário não pode ser vazio");
        }
        this.texto = novoTexto.trim();
        this.dataAtualizacao = LocalDateTime.now();
    }

    public void desativar() {
        this.ativo = false;
        this.dataAtualizacao = LocalDateTime.now();
    }

    public void reativar() {
        this.ativo = true;
        this.dataAtualizacao = LocalDateTime.now();
    }

    public DenunciaComentario registrarDenuncia(Usuario denunciante, MotivoDenuncia motivo, String detalhes) {
        if (denunciante == null) {
            throw new RegraDeNegocioException("O usuário denunciante é obrigatório");
        }
        if (this.autor.equals(denunciante)) {
            throw new RegraDeNegocioException("Um usuário não pode denunciar o próprio comentário");
        }

        for (DenunciaComentario d : this.denuncias) {
            if (d.getUsuarioDenunciante().equals(denunciante)) {
                throw new RegraDeNegocioException("Você já denunciou este comentário previamente");
            }
        }

        DenunciaComentario denuncia = new DenunciaComentario(this, denunciante, motivo, detalhes);
        this.denuncias.add(denuncia);
        return denuncia;
    }

    public boolean isMassivamenteDenunciado(int limiar) {
        return this.denuncias.size() >= limiar;
    }

    public boolean isVisivel() {
        return this.ativo && !isMassivamenteDenunciado(5);
    }

    public String obterTextoFormatado(boolean permitirSpoilers) {
        if (this.contemSpoiler && !permitirSpoilers) {
            return "[Conteúdo oculto com aviso de spoiler]";
        }
        return this.texto;
    }

    public void anonimizarAutor() {
        this.autor = null;
    }

    public String getNomeAutorExibicao() {
        return (this.autor != null) ? this.autor.getApelidoOuEmail() : "Usuário Excluído";
    }

    public Usuario getAutor() {
        return autor;
    }

    public Obra getObra() {
        return obra;
    }

    public HistoricoLeitura getHistoricoLeitura() {
        return historicoLeitura;
    }

    public String getTexto() {
        return texto;
    }

    public boolean isContemSpoiler() {
        return contemSpoiler;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public LocalDateTime getDataAtualizacao() {
        return dataAtualizacao;
    }

    public List<DenunciaComentario> getDenuncias() {
        return Collections.unmodifiableList(denuncias);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Comentario that = (Comentario) o;
        if (getId() != null && that.getId() != null) {
            return getId().equals(that.getId());
        }
        return Objects.equals(autor, that.autor) &&
                Objects.equals(texto, that.texto) &&
                Objects.equals(dataCriacao, that.dataCriacao);
    }

    @Override
    public int hashCode() {
        if (getId() != null) {
            return getId().hashCode();
        }
        return Objects.hash(autor, texto, dataCriacao);
    }
}
