package dev.shiori.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import dev.shiori.domain.enums.Role;
import dev.shiori.domain.enums.StatusSugestao;
import dev.shiori.domain.enums.TipoObra;
import dev.shiori.domain.exception.RegraDeNegocioException;

class SugestaoEdicaoObraTest {

    private Obra obraOficial;
    private Usuario usuarioSugeridor;
    private Usuario admin;
    private Usuario curador;

    @BeforeEach
    void setUp() {
        obraOficial = Obra.criarDeApiExterna(
                "Vinland Saga",
                "Vinrando Saga",
                "https://capas.shiori.dev/vinland_antiga.jpg",
                "Thorfinn em busca de vingança",
                200,
                TipoObra.MANGA
        );

        usuarioSugeridor = new Usuario("sugeridor@shiori.dev", "senha123", "leitor_ativo");
        
        admin = new Usuario("admin@shiori.dev", "senhaAdmin", "admin_supremo");
        admin.promoverPara(Role.ROLE_ADMIN);

        curador = new Usuario("curador@shiori.dev", "senhaCurador", "curador_chefe");
        curador.promoverPara(Role.ROLE_CURADOR);
    }

    @Test
    @DisplayName("Deve criar sugestão de edição com status PENDENTE e campos sugeridos")
    void deveCriarSugestaoComStatusPendente() {
        SugestaoEdicaoObra sugestao = new SugestaoEdicaoObra(
                obraOficial,
                usuarioSugeridor,
                "Vinland Saga (Edição Oficial)",
                "Vinland Saga",
                "https://capas.shiori.dev/vinland_nova.jpg",
                "Thorfinn busca uma terra sem escravidão e guerras",
                210,
                TipoObra.MANGA,
                "Atualização da sinopse e contagem de capítulos lançados"
        );

        assertThat(sugestao.getObra()).isEqualTo(obraOficial);
        assertThat(sugestao.getUsuarioSugeridor()).isEqualTo(usuarioSugeridor);
        assertThat(sugestao.getTituloPrincipalSugerido()).isEqualTo("Vinland Saga (Edição Oficial)");
        assertThat(sugestao.getTotalCapitulosSugerido()).isEqualTo(210);
        assertThat(sugestao.getStatus()).isEqualTo(StatusSugestao.PENDENTE);
        assertThat(sugestao.getDataCriacao()).isNotNull();
        assertThat(sugestao.getAdminAvaliador()).isNull();
        assertThat(sugestao.getDataAvaliacao()).isNull();
    }

    @Test
    @DisplayName("Criação de sugestão deve falhar quando obra ou usuário sugeridor forem nulos")
    void deveFalharSemObraOuUsuario() {
        assertThatThrownBy(() -> new SugestaoEdicaoObra(
                null,
                usuarioSugeridor,
                "Novo Titulo",
                null,
                null,
                null,
                null,
                null,
                "Justificativa"))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("A obra referenciada é obrigatória");

        assertThatThrownBy(() -> new SugestaoEdicaoObra(
                obraOficial,
                null,
                "Novo Titulo",
                null,
                null,
                null,
                null,
                null,
                "Justificativa"))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("O usuário autor da sugestão é obrigatório");
    }

    @Test
    @DisplayName("Criação de sugestão deve falhar se nenhum campo de alteração for informado")
    void deveFalharSemCamposSugeridos() {
        assertThatThrownBy(() -> new SugestaoEdicaoObra(
                obraOficial,
                usuarioSugeridor,
                "   ",
                null,
                null,
                null,
                null,
                null,
                "Sem campos preenchidos"))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("Ao menos um campo deve ser sugerido");
    }

    @Test
    @DisplayName("Criação de sugestão deve falhar se total de capítulos sugerido for negativo")
    void deveFalharComCapitulosNegativos() {
        assertThatThrownBy(() -> new SugestaoEdicaoObra(
                obraOficial,
                usuarioSugeridor,
                null,
                null,
                null,
                null,
                -10,
                null,
                "Capítulo negativo"))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("total de capítulos não pode ser negativo");
    }

    @Test
    @DisplayName("Administrador deve aprovar sugestão, aplicando modificações na obra e premiando usuário com pontos de confiança")
    void adminDeveAprovarSugestaoEAtualizarObra() {
        SugestaoEdicaoObra sugestao = obraOficial.sugerirEdicao(
                usuarioSugeridor,
                "Vinland Saga: Arco Báltico",
                null,
                "https://capas.shiori.dev/baltico.jpg",
                "Sinopse atualizada do arco báltico",
                215,
                null,
                "Atualização canônica"
        );

        sugestao.aprovar(admin, "Sugestão consistente com as fontes oficiais");

        assertThat(sugestao.getStatus()).isEqualTo(StatusSugestao.APROVADA);
        assertThat(sugestao.getAdminAvaliador()).isEqualTo(admin);
        assertThat(sugestao.getParecerAdmin()).isEqualTo("Sugestão consistente com as fontes oficiais");
        assertThat(sugestao.getDataAvaliacao()).isNotNull();

        // Verifica que a obra oficial foi devidamente atualizada
        assertThat(obraOficial.getTituloPrincipal()).isEqualTo("Vinland Saga: Arco Báltico");
        assertThat(obraOficial.getCapaUrl()).isEqualTo("https://capas.shiori.dev/baltico.jpg");
        assertThat(obraOficial.getSinopse()).isEqualTo("Sinopse atualizada do arco báltico");
        assertThat(obraOficial.getTotalCapitulosLancados()).isEqualTo(215);

        // Verifica que o sugeridor recebeu pontos de confiança
        assertThat(usuarioSugeridor.getPontosConfianca()).isEqualTo(10);
    }

    @Test
    @DisplayName("Administrador deve rejeitar sugestão sem alterar obra e sem pontuar usuário")
    void adminDeveRejeitarSugestao() {
        SugestaoEdicaoObra sugestao = obraOficial.sugerirEdicao(
                usuarioSugeridor,
                "Vinland Saga Fake",
                null,
                null,
                "Sinopse inventada",
                999,
                null,
                "Vandalismo"
        );

        sugestao.rejeitar(admin, "Dados não condizem com a publicação oficial");

        assertThat(sugestao.getStatus()).isEqualTo(StatusSugestao.REJEITADA);
        assertThat(sugestao.getAdminAvaliador()).isEqualTo(admin);
        assertThat(sugestao.getParecerAdmin()).isEqualTo("Dados não condizem com a publicação oficial");
        assertThat(sugestao.getDataAvaliacao()).isNotNull();

        // A obra oficial permanece inalterada
        assertThat(obraOficial.getTituloPrincipal()).isEqualTo("Vinland Saga");
        assertThat(obraOficial.getTotalCapitulosLancados()).isEqualTo(200);

        // O usuário não ganha pontos
        assertThat(usuarioSugeridor.getPontosConfianca()).isEqualTo(0);
    }

    @Test
    @DisplayName("Usuários sem ROLE_ADMIN não podem aprovar ou rejeitar sugestões")
    void usuariosNaoAdminNaoPodemAvaliarSugestoes() {
        SugestaoEdicaoObra sugestao = obraOficial.sugerirEdicao(
                usuarioSugeridor,
                "Titulo Sugerido",
                null,
                null,
                null,
                null,
                null,
                "Motivo"
        );

        assertThatThrownBy(() -> sugestao.aprovar(usuarioSugeridor, "Tentativa indevida"))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("Apenas administradores podem avaliar sugestões de edição");

        assertThatThrownBy(() -> sugestao.aprovar(curador, "Curador tentando aprovar"))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("Apenas administradores podem avaliar sugestões de edição");

        assertThatThrownBy(() -> sugestao.rejeitar(usuarioSugeridor, "Tentativa indevida"))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("Apenas administradores podem avaliar sugestões de edição");
    }

    @Test
    @DisplayName("Não deve permitir avaliar uma sugestão já aprovada ou rejeitada")
    void naoDevePermitirReavaliarSugestao() {
        SugestaoEdicaoObra sugestao = obraOficial.sugerirEdicao(
                usuarioSugeridor,
                "Titulo Sugerido",
                null,
                null,
                null,
                null,
                null,
                "Motivo"
        );

        sugestao.aprovar(admin, "Aprovada");

        assertThatThrownBy(() -> sugestao.aprovar(admin, "Segunda tentativa"))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("Esta sugestão já foi avaliada anteriormente");

        assertThatThrownBy(() -> sugestao.rejeitar(admin, "Tentativa de rejeição"))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("Esta sugestão já foi avaliada anteriormente");
    }
}
