package dev.shiori.domain.model;

import java.text.Normalizer;
import java.util.Objects;
import java.util.regex.Pattern;

import dev.shiori.domain.exception.RegraDeNegocioException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "tb_genero")
public class Genero extends EntidadeBase {

    private static final Pattern NON_LATIN = Pattern.compile("[^\\w-]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]");

    @Column(nullable = false, unique = true, length = 60)
    private String nome;

    @Column(nullable = false, unique = true, length = 60)
    private String slug;

    @Column(length = 255)
    private String descricao;

    protected Genero() {
    }

    public Genero(String nome, String descricao) {
        if (nome == null || nome.isBlank()) {
            throw new RegraDeNegocioException("O nome do gênero é obrigatório");
        }
        this.nome = nome.trim();
        this.slug = gerarSlug(this.nome);
        this.descricao = descricao != null ? descricao.trim() : null;
    }

    public static Genero criarDeIntegracaoExterna(String nome) {
        return new Genero(nome, "Gênero importado automaticamente via integração externa");
    }

    private static String gerarSlug(String input) {
        String nowhitespace = WHITESPACE.matcher(input.trim().toLowerCase()).replaceAll("-");
        String normalized = Normalizer.normalize(nowhitespace, Normalizer.Form.NFD);
        String slug = NON_LATIN.matcher(normalized).replaceAll("");
        return slug.replaceAll("-+", "-");
    }

    public String getNome() {
        return nome;
    }

    public String getSlug() {
        return slug;
    }

    public String getDescricao() {
        return descricao;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Genero genero = (Genero) o;
        if (getId() != null && genero.getId() != null) {
            return getId().equals(genero.getId());
        }
        return Objects.equals(slug, genero.slug);
    }

    @Override
    public int hashCode() {
        if (getId() != null) {
            return getId().hashCode();
        }
        return Objects.hashCode(slug);
    }
}
