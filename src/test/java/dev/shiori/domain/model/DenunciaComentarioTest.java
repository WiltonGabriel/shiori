package dev.shiori.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import dev.shiori.domain.enums.MotivoDenuncia;
import dev.shiori.domain.enums.StatusDenuncia;
import dev.shiori.domain.enums.TipoObra;
import dev.shiori.domain.exception.RegraDeNegocioException;

class DenunciaComentarioTest {

    private Usuario autor;
    private Usuario denunciante;
    private Obra obra;
    private Comentario comentario;

    @BeforeEach
    void setUp() {
        autor = new Usuario("autor@shiori.dev", "senha", "autor");
        denunciante = new Usuario("moderador@shiori.dev", "senha", "moderador");
        obra = new Obra("Monster", "Monsutā", null, "Sinopse", 162, TipoObra.MANGA);
        comentario = Comentario.criarParaObra(autor, obra, "Comentário com conteúdo impróprio", false);
    }

    @Test
    @DisplayName("Deve registrar denúncia de comentário com status PENDENTE")
    void deveRegistrarDenunciaComSucesso() {
        DenunciaComentario denuncia = comentario.registrarDenuncia(
                denunciante,
                MotivoDenuncia.CONTEUDO_IMPROPRIO,
                "Linguagem agressiva e ameaças no fórum."
        );

        assertThat(denuncia.getComentario()).isEqualTo(comentario);
        assertThat(denuncia.getUsuarioDenunciante()).isEqualTo(denunciante);
        assertThat(denuncia.getMotivo()).isEqualTo(MotivoDenuncia.CONTEUDO_IMPROPRIO);
        assertThat(denuncia.getStatus()).isEqualTo(StatusDenuncia.PENDENTE);
        assertThat(denuncia.getDataCriacao()).isNotNull();
        assertThat(denuncia.getDataResolucao()).isNull();
        assertThat(comentario.getDenuncias()).contains(denuncia);
    }

    @Test
    @DisplayName("Resolver denúncia como PROCEDENTE deve desativar o comentário automaticamente")
    void resolverComoProcedenteDeveDesativarComentario() {
        DenunciaComentario denuncia = comentario.registrarDenuncia(
                denunciante,
                MotivoDenuncia.CONTEUDO_IMPROPRIO,
                "Ameaças graves"
        );

        denuncia.resolverDenuncia(StatusDenuncia.PROCEDENTE, "Comentário banido por violar os termos de uso.");

        assertThat(denuncia.getStatus()).isEqualTo(StatusDenuncia.PROCEDENTE);
        assertThat(denuncia.getParecerModeracao()).contains("Comentário banido");
        assertThat(denuncia.getDataResolucao()).isNotNull();
        assertThat(comentario.isAtivo()).isFalse();
        assertThat(comentario.isVisivel()).isFalse();
    }

    @Test
    @DisplayName("Resolver denúncia como IMPROCEDENTE mantém o comentário ativo")
    void resolverComoImprocedenteMantemComentarioAtivo() {
        DenunciaComentario denuncia = comentario.registrarDenuncia(
                denunciante,
                MotivoDenuncia.SPAM,
                "Acho que é spam"
        );

        denuncia.resolverDenuncia(StatusDenuncia.IMPROCEDENTE, "Não configura spam, é apenas uma opinião forte.");

        assertThat(denuncia.getStatus()).isEqualTo(StatusDenuncia.IMPROCEDENTE);
        assertThat(comentario.isAtivo()).isTrue();
    }

    @Test
    @DisplayName("Não deve permitir resolver denúncia mantendo status PENDENTE ou nulo")
    void naoDevePermitirResolverComStatusInvalido() {
        DenunciaComentario denuncia = comentario.registrarDenuncia(
                denunciante,
                MotivoDenuncia.OUTRO,
                "Suspeito"
        );

        assertThatThrownBy(() -> denuncia.resolverDenuncia(StatusDenuncia.PENDENTE, "Ainda analisando"))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("status de resolução deve ser PROCEDENTE ou IMPROCEDENTE");

        assertThatThrownBy(() -> denuncia.resolverDenuncia(null, null))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("status de resolução deve ser PROCEDENTE ou IMPROCEDENTE");
    }
}
