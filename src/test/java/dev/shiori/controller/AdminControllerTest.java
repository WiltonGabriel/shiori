package dev.shiori.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import dev.shiori.domain.enums.Role;
import dev.shiori.domain.enums.TipoObra;
import dev.shiori.domain.model.Obra;
import dev.shiori.domain.model.Usuario;
import dev.shiori.infra.security.JwtTokenProvider;
import dev.shiori.repository.ObraRepository;
import dev.shiori.repository.UsuarioRepository;
import software.amazon.awssdk.services.s3.S3Client;

@SpringBootTest
@Transactional
@DisplayName("Testes de Integração: AdminController e RBAC")
class AdminControllerTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ObraRepository obraRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private S3Client s3Client;

    private MockMvc mockMvc;
    private String tokenAdmin;
    private String tokenUsuarioComum;
    private Long adminId;
    private Long usuarioComumId;
    private Long obraPrincipalId;
    private Long obraDuplicadaId;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        // 1. Cria Administrador
        Usuario admin = new Usuario("admin.controller@shiori.dev", "senhaForte123", "admin_controller");
        admin.promoverPara(Role.ROLE_ADMIN);
        admin = usuarioRepository.save(admin);
        adminId = admin.getId();
        tokenAdmin = jwtTokenProvider.gerarToken(admin);

        // 2. Cria Usuário Comum (ROLE_USER)
        Usuario usuarioComum = new Usuario("user.controller@shiori.dev", "senhaForte123", "user_controller");
        usuarioComum = usuarioRepository.save(usuarioComum);
        usuarioComumId = usuarioComum.getId();
        tokenUsuarioComum = jwtTokenProvider.gerarToken(usuarioComum);

        // 3. Cria Obras para teste de mesclagem e oficialização
        Obra obra1 = new Obra("Obra Canônica", "Canonical Work", null, "Sinopse principal", 100, TipoObra.MANGA);
        obra1 = obraRepository.save(obra1);
        obraPrincipalId = obra1.getId();

        Obra obra2 = new Obra("Obra Duplicata", "Duplicated Work", null, "Sinopse duplicata", 100, TipoObra.MANGA);
        obra2.submeterAoCatalogoGlobal(usuarioComum.getId());
        obra2 = obraRepository.save(obra2);
        obraDuplicadaId = obra2.getId();
    }

    // ==========================================
    // TESTES DE SEGURANÇA E CONTROLE DE ACESSO (RBAC)
    // ==========================================

    @Test
    @DisplayName("Requisição não autenticada no endpoint administrativo deve retornar 401 Unauthorized")
    void requisicaoNaoAutenticadaDeveRetornar401() throws Exception {
        mockMvc.perform(get("/api/admin/dashboard"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Usuário com ROLE_USER deve receber 403 Forbidden ao tentar acessar o dashboard admin")
    void usuarioRoleUserDeveReceber403NoDashboard() throws Exception {
        mockMvc.perform(get("/api/admin/dashboard")
                        .header("Authorization", "Bearer " + tokenUsuarioComum))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Usuário com ROLE_USER deve receber 403 Forbidden ao tentar mesclar obras")
    void usuarioRoleUserDeveReceber403NaMesclagem() throws Exception {
        String jsonMesclagem = """
                {
                    "obraDuplicadaId": %d,
                    "obraPrincipalId": %d
                }
                """.formatted(obraDuplicadaId, obraPrincipalId);

        mockMvc.perform(post("/api/admin/obras/mesclar")
                        .header("Authorization", "Bearer " + tokenUsuarioComum)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMesclagem))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Usuário com ROLE_USER deve receber 403 Forbidden ao tentar criar gênero")
    void usuarioRoleUserDeveReceber403AoCriarGenero() throws Exception {
        String jsonGenero = """
                {
                    "nome": "Isekai Moderno",
                    "descricao": "Subgênero de fantasia"
                }
                """;

        mockMvc.perform(post("/api/admin/generos")
                        .header("Authorization", "Bearer " + tokenUsuarioComum)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonGenero))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Usuário com ROLE_USER deve receber 403 Forbidden ao tentar listar usuários")
    void usuarioRoleUserDeveReceber403AoListarUsuarios() throws Exception {
        mockMvc.perform(get("/api/admin/usuarios")
                        .header("Authorization", "Bearer " + tokenUsuarioComum))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Usuário com ROLE_USER deve receber 403 Forbidden ao tentar banir outro usuário")
    void usuarioRoleUserDeveReceber403AoBanirUsuario() throws Exception {
        String jsonBan = """
                {
                    "motivo": "Spam constante"
                }
                """;

        mockMvc.perform(put("/api/admin/usuarios/" + usuarioComumId + "/banir")
                        .header("Authorization", "Bearer " + tokenUsuarioComum)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBan))
                .andExpect(status().isForbidden());
    }

    // ==========================================
    // TESTES DE SUCESSO COM ROLE_ADMIN
    // ==========================================

    @Test
    @DisplayName("Administrador com ROLE_ADMIN deve acessar o dashboard com métricas consolidadas")
    void adminDeveAcessarDashboard() throws Exception {
        mockMvc.perform(get("/api/admin/dashboard")
                        .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUsuarios").isNumber())
                .andExpect(jsonPath("$.totalUsuariosAtivos").isNumber())
                .andExpect(jsonPath("$.totalObras").isNumber())
                .andExpect(jsonPath("$.totalSugestoesPendentes").isNumber());
    }

    @Test
    @DisplayName("Administrador deve conseguir listar usuários da plataforma de forma paginada")
    void adminDeveListarUsuarios() throws Exception {
        mockMvc.perform(get("/api/admin/usuarios")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").isNumber());
    }

    @Test
    @DisplayName("Administrador deve conseguir promover usuário alterando sua Role para ROLE_CURADOR")
    void adminDeveAlterarRoleUsuario() throws Exception {
        String jsonRole = """
                {
                    "role": "ROLE_CURADOR"
                }
                """;

        mockMvc.perform(put("/api/admin/usuarios/" + usuarioComumId + "/role")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRole))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("ROLE_CURADOR"));

        Usuario atualizado = usuarioRepository.findById(usuarioComumId).orElseThrow();
        assertThat(atualizado.getRole()).isEqualTo(Role.ROLE_CURADOR);
    }

    @Test
    @DisplayName("Administrador deve conseguir criar novo gênero no catálogo")
    void adminDeveCriarGenero() throws Exception {
        String jsonGenero = """
                {
                    "nome": "Steampunk",
                    "descricao": "Tecnologia a vapor e era vitoriana"
                }
                """;

        mockMvc.perform(post("/api/admin/generos")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonGenero))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("Steampunk"))
                .andExpect(jsonPath("$.slug").value("steampunk"));
    }

    @Test
    @DisplayName("Administrador deve conseguir oficializar obra comunitária")
    void adminDeveOficializarObra() throws Exception {
        mockMvc.perform(put("/api/admin/obras/" + obraDuplicadaId + "/oficializar")
                        .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCatalogo").value("OFICIAL"));
    }

    @Test
    @DisplayName("Administrador deve conseguir mesclar obras com sucesso")
    void adminDeveMesclarObras() throws Exception {
        String jsonMesclagem = """
                {
                    "obraDuplicadaId": %d,
                    "obraPrincipalId": %d
                }
                """.formatted(obraDuplicadaId, obraPrincipalId);

        mockMvc.perform(post("/api/admin/obras/mesclar")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMesclagem))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.obraPrincipalId").value(obraPrincipalId))
                .andExpect(jsonPath("$.obraDuplicadaIdExcluida").value(obraDuplicadaId));

        assertThat(obraRepository.existsById(obraDuplicadaId)).isFalse();
    }

    @Test
    @DisplayName("Administrador deve conseguir banir usuário infrator")
    void adminDeveBanirUsuario() throws Exception {
        String jsonBan = """
                {
                    "motivo": "Discurso de ódio reiterado"
                }
                """;

        mockMvc.perform(put("/api/admin/usuarios/" + usuarioComumId + "/banir")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBan))
                .andExpect(status().isNoContent());

        Usuario banido = usuarioRepository.findById(usuarioComumId).orElseThrow();
        assertThat(banido.isAtivo()).isFalse();
    }

    @Test
    @DisplayName("Usuário comum deve receber 403 ao tentar aplicar strike ou override")
    void usuarioComumNaoPodeAplicarStrikeOuOverride() throws Exception {
        String jsonStrike = """
                {
                    "motivo": "Comportamento hostil"
                }
                """;

        mockMvc.perform(post("/api/admin/usuarios/" + usuarioComumId + "/strikes")
                        .header("Authorization", "Bearer " + tokenUsuarioComum)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonStrike))
                .andExpect(status().isForbidden());

        String jsonOverride = """
                {
                    "tituloPrincipal": "Título Forçado"
                }
                """;

        mockMvc.perform(put("/api/admin/obras/" + obraPrincipalId + "/override")
                        .header("Authorization", "Bearer " + tokenUsuarioComum)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonOverride))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Administrador deve conseguir aplicar override na obra blindando-a contra sync")
    void adminDeveAplicarOverrideNaObra() throws Exception {
        String jsonOverride = """
                {
                    "tituloPrincipal": "Obra com Título Override",
                    "sinopse": "Sinopse auditada manualmente"
                }
                """;

        mockMvc.perform(put("/api/admin/obras/" + obraPrincipalId + "/override")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonOverride))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tituloPrincipal").value("Obra com Título Override"));

        Obra atualizada = obraRepository.findById(obraPrincipalId).orElseThrow();
        assertThat(atualizada.isProtegidoContraSync()).isTrue();
    }

    @Test
    @DisplayName("Administrador deve conseguir aplicar strike a usuário comum")
    void adminDeveAplicarStrike() throws Exception {
        String jsonStrike = """
                {
                    "motivo": "Spam no fórum"
                }
                """;

        mockMvc.perform(post("/api/admin/usuarios/" + usuarioComumId + "/strikes")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonStrike))
                .andExpect(status().isOk());

        Usuario atualizado = usuarioRepository.findById(usuarioComumId).orElseThrow();
        assertThat(atualizado.getStrikes()).isEqualTo(1);
    }

    @Test
    @DisplayName("Administrador deve conseguir listar feature flags e auditoria do banco")
    void adminDeveListarFeatureFlagsEAuditoria() throws Exception {
        mockMvc.perform(get("/api/admin/feature-flags")
                        .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/admin/auditoria")
                        .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isOk());
    }
}
