package dev.shiori.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import dev.shiori.domain.enums.TipoObra;
import dev.shiori.domain.enums.Visibilidade;
import dev.shiori.domain.exception.RegraDeNegocioException;

class UsuarioTest {

    private Usuario usuario;
    private Usuario amigo;

    @BeforeEach
    void setUp() {
        usuario = new Usuario("leitor@shiori.dev", "hash_senha_secreta");
        amigo = new Usuario("amigo@shiori.dev", "hash_senha_amigo", "amigoleitor");
    }

    @Test
    @DisplayName("Usuário recém-criado deve iniciar com onboarding pendente")
    void usuarioRecemCriadoDeveTerOnboardingPendente() {
        assertThat(usuario.getEmail()).isEqualTo("leitor@shiori.dev");
        assertThat(usuario.isOnboardingConcluido()).isFalse();
        assertThat(usuario.getApelido()).isNull();
        assertThat(usuario.getFusoHorario()).isEqualTo("America/Sao_Paulo");
        assertThat(usuario.isAtivo()).isTrue();
        assertThat(usuario.getDataCriacao()).isNotNull();
    }

    @Test
    @DisplayName("Concluir onboarding deve definir apelido e marcar onboardingConcluido como true")
    void deveConcluirOnboardingComSucesso() {
        usuario.concluirOnboarding(
                "shiori_fan",
                "America/Sao_Paulo",
                "https://avatar.shiori.dev/fan.png",
                "Apaixonado por light novels e mangás"
        );

        assertThat(usuario.isOnboardingConcluido()).isTrue();
        assertThat(usuario.getApelido()).isEqualTo("shiori_fan");
        assertThat(usuario.getFotoUrl()).isEqualTo("https://avatar.shiori.dev/fan.png");
        assertThat(usuario.getDescricao()).contains("light novels");
    }

    @Test
    @DisplayName("Concluir onboarding sem apelido deve lançar exceção de regra de negócio")
    void naoDevePermitirConcluirOnboardingSemApelido() {
        assertThatThrownBy(() -> usuario.concluirOnboarding("   ", null, null, null))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("apelido é obrigatório");
    }

    @Test
    @DisplayName("Validação de e-mail e senha no cadastro")
    void deveValidarEmailESenha() {
        assertThatThrownBy(() -> new Usuario("emailinvalido", "senha"))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("e-mail válido é obrigatório");

        assertThatThrownBy(() -> new Usuario("valido@shiori.dev", "   "))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("senha é obrigatória");
    }

    @Test
    @DisplayName("Solicitar conexão deve criar ConexaoUsuario com status PENDENTE")
    void deveSolicitarConexaoComOutroUsuario() {
        ConexaoUsuario conexao = usuario.solicitarConexaoCom(amigo);

        assertThat(conexao).isNotNull();
        assertThat(conexao.getSolicitante()).isEqualTo(usuario);
        assertThat(conexao.getRecebedor()).isEqualTo(amigo);
        assertThat(conexao.isPendente()).isTrue();
        assertThat(usuario.isConectadoCom(amigo)).isFalse();

        // Ao aceitar a conexão, ambos passam a ser conectados
        conexao.aceitar();
        assertThat(usuario.isConectadoCom(amigo)).isTrue();
        assertThat(amigo.isConectadoCom(usuario)).isTrue();
    }

    @Test
    @DisplayName("Não deve permitir solicitar conexão consigo mesmo")
    void naoDevePermitirConectarConsigoMesmo() {
        assertThatThrownBy(() -> usuario.solicitarConexaoCom(usuario))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("não pode solicitar conexão consigo mesmo");
    }

    @Test
    @DisplayName("Deve adicionar e remover ItemCatalogo da estante do usuário")
    void deveGerenciarItensNaEstante() {
        Obra obra = new Obra("Frieren", "Sousou no Frieren", null, "Sinopse", 130, TipoObra.MANGA);
        ItemCatalogo item = new ItemCatalogo(usuario, obra, Visibilidade.PUBLICO, null);

        usuario.adicionarItemCatalogo(item);
        assertThat(usuario.getEstante()).contains(item);

        usuario.removerItemCatalogo(item);
        assertThat(usuario.getEstante()).isEmpty();
    }

    @Test
    @DisplayName("LGPD: Anonimização de conta deve desativar e expurgar dados sensíveis")
    void deveAnonimizarContaParaLGPD() {
        usuario.concluirOnboarding("antigo_apelido", "UTC", "https://foto.png", "Bio");
        usuario.anonimizarParaLGPD();

        assertThat(usuario.isAtivo()).isFalse();
        assertThat(usuario.getEmail()).contains("@anon.shiori.dev");
        assertThat(usuario.getSenha()).isEqualTo("CONTA_EXCLUIDA");
        assertThat(usuario.getFotoUrl()).isNull();
        assertThat(usuario.getDescricao()).isNull();
    }

    @Test
    @DisplayName("LGPD: Exportação de dados em JSON deve conter informações estruturadas da conta")
    void deveExportarDadosEmJson() {
        usuario.concluirOnboarding("leitor_export", "America/Sao_Paulo", null, "Bio exportável");
        Map<String, Object> dados = usuario.exportarDadosEmJson();

        assertThat(dados).isNotNull();
        assertThat(dados.get("email")).isEqualTo("leitor@shiori.dev");
        assertThat(dados.get("apelido")).isEqualTo("leitor_export");
        assertThat(dados.get("onboardingConcluido")).isEqualTo(true);
        assertThat(dados.get("role")).isEqualTo("ROLE_USER");
        assertThat(dados.get("pontosConfianca")).isEqualTo(0);
        assertThat(dados.get("totalItensEstante")).isEqualTo(0);
    }

    @Test
    @DisplayName("Usuário recém-criado deve possuir ROLE_USER e zero pontos de confiança")
    void deveIniciarComRoleUserEZeroPontos() {
        assertThat(usuario.getRole()).isEqualTo(dev.shiori.domain.enums.Role.ROLE_USER);
        assertThat(usuario.getPontosConfianca()).isEqualTo(0);
        assertThat(usuario.isAdmin()).isFalse();
        assertThat(usuario.isCurador()).isFalse();
    }

    @Test
    @DisplayName("Promoção de usuário deve alterar sua Role e validar permissões administrativas")
    void devePromoverUsuarioEValidarPermissoes() {
        usuario.promoverPara(dev.shiori.domain.enums.Role.ROLE_CURADOR);
        assertThat(usuario.getRole()).isEqualTo(dev.shiori.domain.enums.Role.ROLE_CURADOR);
        assertThat(usuario.isCurador()).isTrue();
        assertThat(usuario.isAdmin()).isFalse();

        usuario.promoverPara(dev.shiori.domain.enums.Role.ROLE_ADMIN);
        assertThat(usuario.getRole()).isEqualTo(dev.shiori.domain.enums.Role.ROLE_ADMIN);
        assertThat(usuario.isAdmin()).isTrue();
        assertThat(usuario.isCurador()).isTrue();
    }

    @Test
    @DisplayName("Não deve permitir promover para Role nula")
    void naoDevePermitirPromoverParaRoleNula() {
        assertThatThrownBy(() -> usuario.promoverPara(null))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("A nova role não pode ser nula");
    }

    @Test
    @DisplayName("Adição de pontos de confiança deve acumular corretamente e rejeitar pontuação negativa")
    void deveGerenciarPontosDeConfianca() {
        usuario.adicionarPontosConfianca(10);
        usuario.adicionarPontosConfianca(25);
        assertThat(usuario.getPontosConfianca()).isEqualTo(35);

        assertThatThrownBy(() -> usuario.adicionarPontosConfianca(-5))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("Não é possível adicionar pontos negativos");
    }

    @Test
    @DisplayName("Segurança: Criação de usuário e atualização de senha com BCryptPasswordEncoder")
    void deveCriarEAtualizarSenhaComBCrypt() {
        org.springframework.security.crypto.password.PasswordEncoder encoder =
                new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();

        Usuario u = Usuario.criarComSenhaPura("seguro@shiori.dev", "minhasenhasecreta", encoder);
        assertThat(u.getSenha()).startsWith("$2a$");
        assertThat(encoder.matches("minhasenhasecreta", u.getSenha())).isTrue();

        u.atualizarSenhaComHash("novasenhaforte", encoder);
        assertThat(encoder.matches("novasenhaforte", u.getSenha())).isTrue();
        assertThat(encoder.matches("minhasenhasecreta", u.getSenha())).isFalse();
    }

    @Test
    @DisplayName("ANPD: Auditoria de acesso administrativo aos dados e configurações de usuários")
    void deveRegistrarAuditoriaAoAcessarDadosPorAdmin() {
        Usuario admin = new Usuario("admin.audit@shiori.dev", "hash_admin");
        admin.promoverPara(dev.shiori.domain.enums.Role.ROLE_ADMIN);

        Map<String, Object> dados = usuario.exportarDadosPorAdmin(admin);
        assertThat(dados).isNotNull();
        assertThat(dados.get("email")).isEqualTo("leitor@shiori.dev");

        usuario.registrarAcessoAdministrativo(admin, "Investigação de conformidade e suporte");

        assertThatThrownBy(() -> usuario.exportarDadosPorAdmin(amigo))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("Apenas administradores");

        assertThatThrownBy(() -> usuario.registrarAcessoAdministrativo(amigo, "Acesso não autorizado"))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("Apenas administradores");
    }

    @Test
    @DisplayName("LGPD: Direito ao Esquecimento purga dados pessoais e vínculos comunitários sem quebrar o catálogo global")
    void direitoAoEsquecimentoDevePurgarDadosPessoaisEPreservarCatalogo() {
        Obra obraGlobal = Obra.criarDeApiExterna("Chainsaw Man", "Chenso Man", null, "Denji", 150, TipoObra.MANGA);
        ItemCatalogo item = new ItemCatalogo(usuario, obraGlobal, Visibilidade.PUBLICO, null);
        usuario.adicionarItemCatalogo(item);
        usuario.solicitarConexaoCom(amigo);
        usuario.registrarConsentimento("v1.0", "v1.0", true, false, false, "127.0.0.1", "TestAgent");

        Obra obraCriadaPeloUsuario = new Obra("Obra do Usuario", null, null, "Sinopse", 10, TipoObra.LIVRO);
        obraCriadaPeloUsuario.submeterAoCatalogoGlobal(100L);
        Tag tagCriadaPeloUsuario = new Tag("cyberpunk", 100L);
        Comentario comentarioCriado = Comentario.criarParaObra(usuario, obraGlobal, "Excelente obra!", false);

        usuario.anonimizarParaLGPD();

        assertThat(usuario.isAtivo()).isFalse();
        assertThat(usuario.getEmail()).startsWith("anonimizado_");
        assertThat(usuario.getSenha()).isEqualTo("CONTA_EXCLUIDA");
        assertThat(usuario.getApelido()).startsWith("excluido_");
        assertThat(usuario.getFotoUrl()).isNull();
        assertThat(usuario.getDescricao()).isNull();
        assertThat(usuario.getPontosConfianca()).isEqualTo(0);
        assertThat(usuario.getEstante()).isEmpty();
        assertThat(usuario.getConexoesEnviadas()).isEmpty();
        assertThat(usuario.getConsentimentos()).isEmpty();

        obraCriadaPeloUsuario.anonimizarCriador();
        tagCriadaPeloUsuario.anonimizarCriador();
        comentarioCriado.anonimizarAutor();

        assertThat(obraCriadaPeloUsuario.getCriadoPorUsuarioId()).isNull();
        assertThat(obraCriadaPeloUsuario.isVisivelNaHome()).isTrue();
        assertThat(tagCriadaPeloUsuario.getCriadoPorUsuarioId()).isNull();
        assertThat(tagCriadaPeloUsuario.getNome()).isEqualTo("cyberpunk");
        assertThat(comentarioCriado.getAutor()).isNull();
        assertThat(comentarioCriado.getNomeAutorExibicao()).isEqualTo("Usuário Excluído");
        assertThat(comentarioCriado.getTexto()).isEqualTo("Excelente obra!");
    }
}
