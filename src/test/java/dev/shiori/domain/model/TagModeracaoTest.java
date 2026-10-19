package dev.shiori.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import dev.shiori.domain.enums.TipoObra;
import dev.shiori.domain.enums.TipoVoto;
import dev.shiori.domain.exception.RegraDeNegocioException;

class TagModeracaoTest {

    private Obra obra;
    private Tag tagIsekai;
    private Tag tagProtagonistaOp;
    private Tag tagSpoilerPesado;
    private Genero generoFantasia;
    private Usuario usuarioAlice;
    private Usuario usuarioBob;
    private Usuario usuarioCarlos;

    @BeforeEach
    void setUp() {
        obra = new Obra(
                "Mushoku Tensei",
                "Mushoku Tensei: Isekai Ittara Honki Dasu",
                null,
                "Sinopse da obra...",
                26,
                TipoObra.NOVEL
        );

        tagIsekai = new Tag("Isekai", 1L);
        tagProtagonistaOp = new Tag("Protagonista OP", 2L);
        tagSpoilerPesado = new Tag("Morte do Herói no Volume 5", 3L);

        generoFantasia = new Genero("Fantasia", "Narrativas mágicas e mundos ficcionais");

        usuarioAlice = new Usuario("alice@shiori.dev", "senha", "alice");
        usuarioBob = new Usuario("bob@shiori.dev", "senha", "bob");
        usuarioCarlos = new Usuario("carlos@shiori.dev", "senha", "carlos");
    }

    @Test
    @DisplayName("Tag deve gerar slug sanitizado automaticamente e exigir campos obrigatórios")
    void tagDeveGerarSlugEValidarInvariantes() {
        Tag novaTag = new Tag("Viagem no Tempo / Sci-Fi!", 10L);

        assertThat(novaTag.getNome()).isEqualTo("Viagem no Tempo / Sci-Fi!");
        assertThat(novaTag.getSlug()).isEqualTo("viagem-no-tempo-sci-fi");
        assertThat(novaTag.getCriadoPorUsuarioId()).isEqualTo(10L);

        assertThatThrownBy(() -> new Tag("   ", 10L))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("nome da tag é obrigatório");

        assertThatThrownBy(() -> new Tag("Tag Válida", null))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("identificador do usuário criador é obrigatório");
    }

    @Test
    @DisplayName("Gênero oficial deve gerar slug automaticamente e exigir nome não vazio")
    void generoDeveGerarSlugEValidarInvariantes() {
        Genero ficcao = new Genero("Ficção Científica", "Exploração de tecnologia e futuro");

        assertThat(ficcao.getNome()).isEqualTo("Ficção Científica");
        assertThat(ficcao.getSlug()).isEqualTo("ficcao-cientifica");
        assertThat(ficcao.getDescricao()).isEqualTo("Exploração de tecnologia e futuro");

        assertThatThrownBy(() -> new Genero(null, "Descrição"))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("nome do gênero é obrigatório");
    }

    @Test
    @DisplayName("Gênero instanciado pelo módulo de integração externa deve gerar slug e descrição padrão")
    void generoCriadoPorIntegracaoExternaDeveSerValido() {
        Genero generoImportado = Genero.criarDeIntegracaoExterna("Cyberpunk");

        assertThat(generoImportado.getNome()).isEqualTo("Cyberpunk");
        assertThat(generoImportado.getSlug()).isEqualTo("cyberpunk");
        assertThat(generoImportado.getDescricao()).contains("integração externa");
    }

    @Test
    @DisplayName("Deve associar e desassociar gêneros oficiais na Obra")
    void deveAssociarEDesassociarGeneros() {
        obra.adicionarGenero(generoFantasia);
        assertThat(obra.getGeneros()).containsExactly(generoFantasia);

        obra.removerGenero(generoFantasia);
        assertThat(obra.getGeneros()).isEmpty();

        assertThatThrownBy(() -> obra.adicionarGenero(null))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("gênero não pode ser nulo");
    }

    @Test
    @DisplayName("Voto em Tag exige que a tag esteja associada previamente à Obra")
    void votoEmTagExigeQueATagEstejaNaObra() {
        assertThatThrownBy(() -> obra.computarVotoTag(tagIsekai, usuarioAlice, TipoVoto.UPVOTE))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("não está associada a esta obra");
    }

    @Test
    @DisplayName("Deve computar Upvotes e Downvotes acumulando o score comunitário")
    void deveComputarUpvotesEDownvotesCorretamente() {
        obra.adicionarTag(tagIsekai);

        obra.computarVotoTag(tagIsekai, usuarioAlice, TipoVoto.UPVOTE);
        obra.computarVotoTag(tagIsekai, usuarioBob, TipoVoto.UPVOTE);
        obra.computarVotoTag(tagIsekai, usuarioCarlos, TipoVoto.DOWNVOTE);

        // 2 Upvotes (+2) e 1 Downvote (-1) = score +1
        assertThat(obra.calcularScoreTag(tagIsekai)).isEqualTo(1);
        assertThat(obra.isTagVisivel(tagIsekai)).isTrue();
    }

    @Test
    @DisplayName("Alteração de voto pelo mesmo usuário deve atualizar sem duplicar registro")
    void alteracaoDeVotoNaoDeveDuplicarRegistro() {
        obra.adicionarTag(tagProtagonistaOp);

        obra.computarVotoTag(tagProtagonistaOp, usuarioAlice, TipoVoto.UPVOTE);
        assertThat(obra.calcularScoreTag(tagProtagonistaOp)).isEqualTo(1);

        // Alice muda de ideia e vota contra a tag
        obra.computarVotoTag(tagProtagonistaOp, usuarioAlice, TipoVoto.DOWNVOTE);
        assertThat(obra.calcularScoreTag(tagProtagonistaOp)).isEqualTo(-1);
    }

    @Test
    @DisplayName("Tag com score negativo deve ser automaticamente oculta da comunidade")
    void tagComScoreNegativoDeveSerOcultada() {
        obra.adicionarTag(tagIsekai);
        obra.adicionarTag(tagSpoilerPesado);

        // Tag Isekai recebe avaliação positiva (score 1)
        obra.computarVotoTag(tagIsekai, usuarioAlice, TipoVoto.UPVOTE);

        // Tag Spoiler recebe votos negativos da comunidade (score -2)
        obra.computarVotoTag(tagSpoilerPesado, usuarioAlice, TipoVoto.DOWNVOTE);
        obra.computarVotoTag(tagSpoilerPesado, usuarioBob, TipoVoto.DOWNVOTE);

        assertThat(obra.calcularScoreTag(tagSpoilerPesado)).isEqualTo(-2);
        assertThat(obra.isTagVisivel(tagSpoilerPesado)).isFalse();

        // O conjunto de tags visíveis deve exibir a tag bem-avaliada e ocultar a negativada
        assertThat(obra.getTagsVisiveis())
                .contains(tagIsekai)
                .doesNotContain(tagSpoilerPesado);
    }

    @Test
    @DisplayName("Remover uma Tag da Obra deve expurgar a tag e todos os seus votos associados")
    void removerTagDeveExpurgarVotosAssociados() {
        obra.adicionarTag(tagIsekai);
        obra.computarVotoTag(tagIsekai, usuarioAlice, TipoVoto.UPVOTE);
        assertThat(obra.getVotosTag()).hasSize(1);

        obra.removerTag(tagIsekai);

        assertThat(obra.getTags()).doesNotContain(tagIsekai);
        assertThat(obra.getVotosTag()).isEmpty();
        assertThat(obra.calcularScoreTag(tagIsekai)).isZero();
    }
}
