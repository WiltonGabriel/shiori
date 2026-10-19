package dev.shiori.domain.model;

import java.text.Normalizer;
import java.util.Objects;
import java.util.regex.Pattern;

import dev.shiori.domain.exception.RegraDeNegocioException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "tb_tag")
public class Tag extends EntidadeBase {

    private static final Pattern NON_LATIN = Pattern.compile("[^\\w-]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]");

    @Column(nullable = false, unique = true, length = 60)
    private String nome;

    @Column(nullable = false, unique = true, length = 60)
    private String slug;

    @Column(nullable = true)
    private Long criadoPorUsuarioId;

    protected Tag() {
    }

    public Tag(String nome, Long criadoPorUsuarioId) {
        if (nome == null || nome.isBlank()) {
            throw new RegraDeNegocioException("O nome da tag é obrigatório");
        }
        if (criadoPorUsuarioId == null) {
            throw new RegraDeNegocioException("O identificador do usuário criador é obrigatório");
        }
        this.nome = nome.trim();
        this.slug = gerarSlug(this.nome);
        this.criadoPorUsuarioId = criadoPorUsuarioId;
    }

    private static String gerarSlug(String input) {
        String nowhitespace = WHITESPACE.matcher(input.trim().toLowerCase()).replaceAll("-");
        String normalized = Normalizer.normalize(nowhitespace, Normalizer.Form.NFD);
        String slug = NON_LATIN.matcher(normalized).replaceAll("");
        return slug.replaceAll("-+", "-");
    }

    public void anonimizarCriador() {
        this.criadoPorUsuarioId = null;
    }

    public String getNome() {
        return nome;
    }

    public String getSlug() {
        return slug;
    }

    public Long getCriadoPorUsuarioId() {
        return criadoPorUsuarioId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tag tag = (Tag) o;
        if (getId() != null && tag.getId() != null) {
            return getId().equals(tag.getId());
        }
        return Objects.equals(slug, tag.slug);
    }

    @Override
    public int hashCode() {
        if (getId() != null) {
            return getId().hashCode();
        }
        return Objects.hashCode(slug);
    }
}
