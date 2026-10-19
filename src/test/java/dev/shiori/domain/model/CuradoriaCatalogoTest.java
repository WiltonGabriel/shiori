package dev.shiori.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import dev.shiori.domain.enums.StatusCatalogo;
import dev.shiori.domain.enums.TipoObra;
import dev.shiori.domain.exception.RegraDeNegocioException;

class CuradoriaCatalogoTest {

    private Obra obraManual;
    private Long usuarioAutorId;

    @BeforeEach
    void setUp() {
        usuarioAutorId = 42L;
        obraManual = new Obra(
                "Solo Leveling",
                "Na Honjaman Rebeleob",
                "https://imagem.shiori.dev/capas/solo.jpg",
                "Em um mundo onde caçadores despertam...",
                179,
                TipoObra.MANHWA
        );
    }

    @Test
    @DisplayName("Obra cadastrada manualmente deve nascer com StatusCatalogo PRIVADO")
    void obraManualDeveNascerComStatusPrivado() {
        assertThat(obraManual.getStatusCatalogo()).isEqualTo(StatusCatalogo.PRIVADO);
        assertThat(obraManual.getCriadoPorUsuarioId()).isNull();
        assertThat(obraManual.isVisivelNaHome()).isFalse();
    }

    @Test
    @DisplayName("Obra puxada de API externa deve nascer como OFICIAL")
    void obraDeApiExternaDeveNascerComoOficial() {
        Obra obraExterna = Obra.criarDeApiExterna(
                "One Piece",
                "Wan Pīsu",
                "https://capas.shiori.dev/op.jpg",
                "Luffy parte para o mar...",
                1100,
                TipoObra.MANGA
        );

        assertThat(obraExterna.getStatusCatalogo()).isEqualTo(StatusCatalogo.OFICIAL);
        assertThat(obraExterna.isVisivelNaHome()).isTrue();
    }

    @Test
    @DisplayName("Submeter ao Catálogo Global deve alterar status para COMUNITARIO e gravar o ID do autor")
    void deveSubmeterAoCatalogoGlobalComSucesso() {
        obraManual.submeterAoCatalogoGlobal(usuarioAutorId);

        assertThat(obraManual.getStatusCatalogo()).isEqualTo(StatusCatalogo.COMUNITARIO);
        assertThat(obraManual.getCriadoPorUsuarioId()).isEqualTo(usuarioAutorId);
        assertThat(obraManual.isVisivelNaHome()).isTrue();
    }

    @Test
    @DisplayName("Submissão ao catálogo global sem identificador de autor deve falhar")
    void deveFalharAoSubmeterSemAutor() {
        assertThatThrownBy(() -> obraManual.submeterAoCatalogoGlobal(null))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("identificador do usuário autor é obrigatório");
    }

    @Test
    @DisplayName("Obra COMUNITARIO pode ser promovida para OFICIAL por um administrador")
    void devePromoverObraComunitariaParaOficial() {
        obraManual.submeterAoCatalogoGlobal(usuarioAutorId);
        obraManual.promoverParaOficial();

        assertThat(obraManual.getStatusCatalogo()).isEqualTo(StatusCatalogo.OFICIAL);
        assertThat(obraManual.isVisivelNaHome()).isTrue();
    }

    @Test
    @DisplayName("Não deve permitir promover obra diretamente de PRIVADO para OFICIAL")
    void naoDevePermitirPromoverObraPrivadaDiretamente() {
        assertThatThrownBy(() -> obraManual.promoverParaOficial())
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("Apenas obras comunitárias submetidas podem ser promovidas");
    }

    @Test
    @DisplayName("Não deve permitir submeter obra já OFICIAL")
    void naoDevePermitirSubmeterObraJaOficial() {
        obraManual.submeterAoCatalogoGlobal(usuarioAutorId);
        obraManual.promoverParaOficial();

        assertThatThrownBy(() -> obraManual.submeterAoCatalogoGlobal(usuarioAutorId))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("Esta obra já é oficial do catálogo");
    }

    @Test
    @DisplayName("Deve permitir tornar a obra privada se necessário")
    void devePermitirTornarPrivada() {
        obraManual.submeterAoCatalogoGlobal(usuarioAutorId);
        assertThat(obraManual.getStatusCatalogo()).isEqualTo(StatusCatalogo.COMUNITARIO);

        obraManual.tornarPrivada();
        assertThat(obraManual.getStatusCatalogo()).isEqualTo(StatusCatalogo.PRIVADO);
        assertThat(obraManual.isVisivelNaHome()).isFalse();
    }

    @Test
    @DisplayName("Regra de Ouro: Obra OFICIAL é imutável para usuários comuns e curadores")
    void regraDeOuroObraOficialImutavelParaUsuariosComuns() {
        Obra obraOficial = Obra.criarDeApiExterna("Berserk", "Beruseruku", null, "Guts busca vingança", 375, TipoObra.MANGA);
        Usuario usuarioComum = new Usuario("comum@shiori.dev", "senha123", "leitor_comum");
        Usuario curador = new Usuario("curador@shiori.dev", "senha123", "curador_top");
        curador.promoverPara(dev.shiori.domain.enums.Role.ROLE_CURADOR);

        assertThatThrownBy(() -> obraOficial.atualizarPorUsuario(
                usuarioComum,
                "Berserk Modificado",
                null,
                null,
                "Nova sinopse",
                380,
                TipoObra.MANGA))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("Obras com status oficial são imutáveis para usuários comuns");

        assertThatThrownBy(() -> obraOficial.atualizarPorUsuario(
                curador,
                "Berserk Curado",
                null,
                null,
                "Nova sinopse",
                380,
                TipoObra.MANGA))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("Obras com status oficial são imutáveis para usuários comuns");
    }

    @Test
    @DisplayName("Regra de Ouro: Apenas administradores podem atualizar obras oficiais diretamente")
    void apenasAdminPodeAtualizarObraOficialDiretamente() {
        Obra obraOficial = Obra.criarDeApiExterna("Berserk", "Beruseruku", null, "Guts busca vingança", 375, TipoObra.MANGA);
        Usuario admin = new Usuario("admin@shiori.dev", "senhaAdmin", "chefe");
        admin.promoverPara(dev.shiori.domain.enums.Role.ROLE_ADMIN);

        obraOficial.atualizarPorUsuario(
                admin,
                "Berserk (Edição Definitiva)",
                "Beruseruku",
                "https://capas.shiori.dev/berserk.jpg",
                "Guts o espadachim negro...",
                380,
                TipoObra.MANGA
        );

        assertThat(obraOficial.getTituloPrincipal()).isEqualTo("Berserk (Edição Definitiva)");
        assertThat(obraOficial.getTotalCapitulosLancados()).isEqualTo(380);
        assertThat(obraOficial.getCapaUrl()).isEqualTo("https://capas.shiori.dev/berserk.jpg");
    }

    @Test
    @DisplayName("Deve adicionar, remover e definir avisos de conteúdo na obra")
    void deveGerenciarAvisosDeConteudoNaObra() {
        obraManual.adicionarAvisoConteudo(dev.shiori.domain.enums.AvisoConteudo.VIOLENCIA);
        obraManual.adicionarAvisoConteudo(dev.shiori.domain.enums.AvisoConteudo.GORE);

        assertThat(obraManual.getAvisosConteudo()).containsExactlyInAnyOrder(
                dev.shiori.domain.enums.AvisoConteudo.VIOLENCIA,
                dev.shiori.domain.enums.AvisoConteudo.GORE
        );

        obraManual.removerAvisoConteudo(dev.shiori.domain.enums.AvisoConteudo.GORE);
        assertThat(obraManual.getAvisosConteudo()).containsExactly(dev.shiori.domain.enums.AvisoConteudo.VIOLENCIA);

        obraManual.definirAvisosConteudo(java.util.Set.of(dev.shiori.domain.enums.AvisoConteudo.CONTEUDO_SEXUAL));
        assertThat(obraManual.getAvisosConteudo()).containsExactly(dev.shiori.domain.enums.AvisoConteudo.CONTEUDO_SEXUAL);
    }

    @Test
    @DisplayName("Deve gerenciar status de publicação, total de volumes e IDs externos de integração")
    void deveGerenciarStatusPublicacaoEIdsExternos() {
        assertThat(obraManual.getStatusPublicacao()).isEqualTo(dev.shiori.domain.enums.StatusPublicacao.EM_LANCAMENTO);

        obraManual.atualizarStatusPublicacao(dev.shiori.domain.enums.StatusPublicacao.CONCLUIDO);
        assertThat(obraManual.getStatusPublicacao()).isEqualTo(dev.shiori.domain.enums.StatusPublicacao.CONCLUIDO);

        obraManual.atualizarTotalVolumes(14);
        assertThat(obraManual.getTotalVolumes()).isEqualTo(14);

        assertThatThrownBy(() -> obraManual.atualizarTotalVolumes(-2))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("não pode ser negativo");

        obraManual.definirAnilistId("105398");
        obraManual.definirMalId("121496");
        obraManual.definirMangadexId("32d76d19-8a05-4db0-9fc2-e0b0648fe9d0");

        assertThat(obraManual.getAnilistId()).isEqualTo("105398");
        assertThat(obraManual.getMalId()).isEqualTo("121496");
        assertThat(obraManual.getMangadexId()).isEqualTo("32d76d19-8a05-4db0-9fc2-e0b0648fe9d0");
    }
}
