package dev.shiori.controller;

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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import software.amazon.awssdk.services.s3.S3Client;

@SpringBootTest
@Transactional
@DisplayName("Testes de Integração: ConexaoController")
class ConexaoControllerTest {

    @Autowired
    private WebApplicationContext context;

    @MockitoBean
    private S3Client s3Client;

    private MockMvc mockMvc;
    private String tokenA;
    private Long idA;
    private String tokenB;
    private Long idB;

    @BeforeEach
    void setUp() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        // Cadastra usuário A
        String regA = """
                {
                    "email": "userA@shiori.dev",
                    "senha": "senhaSegura123",
                    "apelido": "usuarioA"
                }
                """;
        MvcResult resA = mockMvc.perform(post("/api/auth/registrar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(regA))
                .andExpect(status().isCreated())
                .andReturn();

        tokenA = resA.getResponse().getContentAsString().split("\"token\":\"")[1].split("\"")[0];
        idA = Long.parseLong(resA.getResponse().getContentAsString().split("\"usuarioId\":")[1].split(",")[0]);

        // Cadastra usuário B
        String regB = """
                {
                    "email": "userB@shiori.dev",
                    "senha": "senhaSegura123",
                    "apelido": "usuarioB"
                }
                """;
        MvcResult resB = mockMvc.perform(post("/api/auth/registrar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(regB))
                .andExpect(status().isCreated())
                .andReturn();

        tokenB = resB.getResponse().getContentAsString().split("\"token\":\"")[1].split("\"")[0];
        idB = Long.parseLong(resB.getResponse().getContentAsString().split("\"usuarioId\":")[1].split(",")[0]);
    }

    @Test
    @DisplayName("Ciclo de Conexão: Solicitar -> Listar Pendente -> Aceitar -> Listar Ativas -> Bloquear")
    void cicloCompletoDeConexao() throws Exception {
        // 1. Usuário A solicita conexão com Usuário B
        MvcResult solRes = mockMvc.perform(post("/api/conexoes/solicitar/" + idB)
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.solicitanteId").value(idA))
                .andExpect(jsonPath("$.recebedorId").value(idB))
                .andExpect(jsonPath("$.status").value("PENDENTE"))
                .andReturn();

        String conexaoIdStr = solRes.getResponse().getContentAsString().split("\"id\":")[1].split(",")[0];
        Long conexaoId = Long.parseLong(conexaoIdStr);

        // 2. Usuário B lista suas solicitações pendentes
        mockMvc.perform(get("/api/conexoes/pendentes")
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].solicitanteApelido").value("usuarioa"));

        // 3. Usuário B aceita a solicitação
        mockMvc.perform(put("/api/conexoes/" + conexaoId + "/aceitar")
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACEITA"));

        // 4. Usuário A lista suas conexões aceitas
        mockMvc.perform(get("/api/conexoes")
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].status").value("ACEITA"));

        // 5. Usuário A bloqueia a conexão
        mockMvc.perform(put("/api/conexoes/" + conexaoId + "/bloquear")
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("BLOQUEADA"));
    }

    @Test
    @DisplayName("Não deve permitir solicitar conexão para si mesmo")
    void naoDevePermitirSolicitarConexaoParaSiMesmo() throws Exception {
        mockMvc.perform(post("/api/conexoes/solicitar/" + idA)
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("Não é possível solicitar conexão consigo mesmo"));
    }
}
