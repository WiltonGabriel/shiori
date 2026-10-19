package dev.shiori.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import dev.shiori.domain.enums.MotivoDenuncia;
import dev.shiori.domain.enums.StatusLeitura;
import dev.shiori.domain.enums.TipoObra;
import dev.shiori.domain.enums.Visibilidade;
import dev.shiori.domain.exception.RegraDeNegocioException;

class ComentarioTest {

    private Usuario autor;
    private Usuario leitorOutro;
    private Obra obra;
    private ItemCatalogo item;
    private HistoricoLeitura historico;

    @BeforeEach
    void setUp() {
        autor = new Usuario("autor@shiori.dev", "senha", "autor_comentario");
        leitorOutro = new Usuario("leitor@shiori.dev", "senha", "leitor_critico");

        obra = new Obra("Vinland Saga", "Vinrando Saga", null, "Vingança viking...", 200, TipoObra.MANGA);
        item = new ItemCatalogo(autor, obra, Visibilidade.PUBLICO, StatusLeitura.LENDO);
        historico = item.registrarCicloLeitura(50.0, 10.0, "Fim do prólogo com Askeladd");
    }

    @Test
    @DisplayName("Deve criar comentário vinculado à Obra (fórum geral) com sucesso")
    void deveCriarComentarioParaObra() {
        Comentario comentario = Comentario.criarParaObra(
                autor,
                obra,
                "Que obra-prima este arco do prólogo!",
                false
        );

        assertThat(comentario.getAutor()).isEqualTo(autor);
        assertThat(comentario.getObra()).isEqualTo(obra);
        assertThat(comentario.getHistoricoLeitura()).isNull();
        assertThat(comentario.getTexto()).isEqualTo("Que obra-prima este arco do prólogo!");
        assertThat(comentario.isContemSpoiler()).isFalse();
        assertThat(comentario.isAtivo()).isTrue();
        assertThat(comentario.getDataCriacao()).isNotNull();
        assertThat(comentario.isVisivel()).isTrue();
    }

    @Test
    @DisplayName("Deve criar comentário vinculado ao HistoricoLeitura (feed do amigo)")
    void deveCriarComentarioParaHistorico() {
        Comentario comentario = Comentario.criarParaHistorico(
                leitorOutro,
                historico,
                "Concordo demais com a sua nota de 10!",
                false
        );

        assertThat(comentario.getAutor()).isEqualTo(leitorOutro);
        assertThat(comentario.getHistoricoLeitura()).isEqualTo(historico);
        assertThat(comentario.getObra()).isNull();
        assertThat(comentario.getTexto()).contains("Concordo demais");
    }

    @Test
    @DisplayName("Comentário sem vínculo com Obra ou HistoricoLeitura deve falhar")
    void deveFalharSemAlvoDeVinculo() {
        assertThatThrownBy(() -> new Comentario(autor, null, null, "Texto sem alvo", false))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("deve estar vinculado a uma obra ou a um histórico");
    }

    @Test
    @DisplayName("Texto do comentário não pode ser nulo ou vazio")
    void deveValidarTextoNaoVazio() {
        assertThatThrownBy(() -> Comentario.criarParaObra(autor, obra, "   ", false))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("texto do comentário não pode ser vazio");
    }

    @Test
    @DisplayName("Flag anti-spoiler deve ocultar o texto formatado para quem desativou spoilers")
    void deveTratarFlagAntiSpoiler() {
        Comentario comSpoiler = Comentario.criarParaObra(
                autor,
                obra,
                "Cuidado: o personagem X morre no capítulo 54!",
                true
        );

        assertThat(comSpoiler.isContemSpoiler()).isTrue();
        assertThat(comSpoiler.obterTextoFormatado(false)).isEqualTo("[Conteúdo oculto com aviso de spoiler]");
        assertThat(comSpoiler.obterTextoFormatado(true)).contains("o personagem X morre");

        // Altera para não-spoiler
        comSpoiler.desmarcarSpoiler();
        assertThat(comSpoiler.isContemSpoiler()).isFalse();
        assertThat(comSpoiler.obterTextoFormatado(false)).contains("o personagem X morre");
    }

    @Test
    @DisplayName("Não deve permitir que o autor denuncie o próprio comentário")
    void naoDevePermitirAutoDenuncia() {
        Comentario comentario = Comentario.criarParaObra(autor, obra, "Meu comentário", false);

        assertThatThrownBy(() -> comentario.registrarDenuncia(autor, MotivoDenuncia.SPAM, "Detalhes"))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("não pode denunciar o próprio comentário");
    }

    @Test
    @DisplayName("Não deve permitir denúncia duplicada pelo mesmo usuário")
    void naoDevePermitirDenunciaDuplicada() {
        Comentario comentario = Comentario.criarParaObra(autor, obra, "Texto polêmico", false);

        comentario.registrarDenuncia(leitorOutro, MotivoDenuncia.CONTEUDO_IMPROPRIO, "Linguagem ofensiva");

        assertThatThrownBy(() -> comentario.registrarDenuncia(leitorOutro, MotivoDenuncia.SPAM, "Outra tentativa"))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("já denunciou este comentário previamente");
    }

    @Test
    @DisplayName("Comentário massivamente denunciado deve ser considerado oculto / não visível")
    void comentarioMassivamenteDenunciadoDeveSerOcultado() {
        Comentario comentario = Comentario.criarParaObra(autor, obra, "Spam de links externos", false);

        for (int i = 1; i <= 5; i++) {
            Usuario denunciante = new Usuario("user" + i + "@shiori.dev", "senha", "user" + i);
            comentario.registrarDenuncia(denunciante, MotivoDenuncia.SPAM, "Link malicioso " + i);
        }

        assertThat(comentario.getDenuncias()).hasSize(5);
        assertThat(comentario.isMassivamenteDenunciado(5)).isTrue();
        assertThat(comentario.isVisivel()).isFalse();
    }
}
