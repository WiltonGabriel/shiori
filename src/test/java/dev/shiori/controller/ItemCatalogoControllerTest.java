package dev.shiori.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

import dev.shiori.domain.enums.TipoObra;
import dev.shiori.domain.model.Obra;
import dev.shiori.repository.ItemCatalogoRepository;
import dev.shiori.repository.ObraRepository;
import software.amazon.awssdk.services.s3.S3Client;

@SpringBootTest
@Transactional
@DisplayName("Testes de Integração: ItemCatalogoController")
class ItemCatalogoControllerTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ObraRepository obraRepository;

    @Autowired
    private ItemCatalogoRepository itemCatalogoRepository;

    @MockitoBean
    private S3Client s3Client;

    private MockMvc mockMvc;
    private String jwtToken;
    private Long obraId;

    @BeforeEach
    void setUp() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        // 1. Cadastra usuário e obtém JWT
        String jsonRegistro = """
                {
                    "email": "leitor.catalogo@shiori.dev",
                    "senha": "senhaSegura123",
                    "apelido": "leitor_catalogo"
                }
                """;

        MvcResult result = mockMvc.perform(post("/api/auth/registrar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRegistro))
                .andExpect(status().isCreated())
                .andReturn();

        jwtToken = result.getResponse().getContentAsString().split("\"token\":\"")[1].split("\"")[0];

        // 2. Cria uma Obra no repositório
        Obra obra = new Obra("Vinland Saga", "Vinrando Saga", "https://capas.shiori.dev/vs.jpg", "Vikings históricos", 200, TipoObra.MANGA);
        Obra salva = obraRepository.save(obra);
        obraId = salva.getId();
    }

    @Test
    @DisplayName("POST /api/catalogo deve adicionar obra à estante com sucesso")
    void deveAdicionarObraEstanteComSucesso() throws Exception {
        String body = String.format("""
                {
                    "obraId": %d,
                    "visibilidade": "PUBLICO",
                    "statusLeitura": "LENDO",
                    "urlLeitura": "https://leitura.shiori.dev/vs"
                }
                """, obraId);

        mockMvc.perform(post("/api/catalogo")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.obraId").value(obraId))
                .andExpect(jsonPath("$.obraTitulo").value("Vinland Saga"))
                .andExpect(jsonPath("$.visibilidade").value("PUBLICO"))
                .andExpect(jsonPath("$.statusLeitura").value("LENDO"))
                .andExpect(jsonPath("$.urlLeitura").value("https://leitura.shiori.dev/vs"));
    }

    @Test
    @DisplayName("POST /api/catalogo/{id}/progresso deve registrar ciclo de leitura e atualizar progresso")
    void deveRegistrarProgressoComCicloElastico() throws Exception {
        // Primeiro adiciona na estante
        String bodyAdicao = String.format("""
                {
                    "obraId": %d,
                    "visibilidade": "PUBLICO",
                    "statusLeitura": "LENDO"
                }
                """, obraId);

        MvcResult result = mockMvc.perform(post("/api/catalogo")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyAdicao))
                .andExpect(status().isCreated())
                .andReturn();

        String idStr = result.getResponse().getContentAsString().split("\"id\":")[1].split(",")[0];
        Long itemId = Long.parseLong(idStr);

        // Registra progresso
        String bodyProgresso = """
                {
                    "capituloAtual": 15.0,
                    "volumeAtual": 2.0,
                    "nota": 9.0,
                    "anotacao": "Início do arco do prólogo sensacional",
                    "statusLeitura": "LENDO"
                }
                """;

        mockMvc.perform(post("/api/catalogo/" + itemId + "/progresso")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyProgresso))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.progressoAtual").value(15.0))
                .andExpect(jsonPath("$.volumeAtual").value(2.0))
                .andExpect(jsonPath("$.avaliacaoPessoal").value(9.0))
                .andExpect(jsonPath("$.historicosLeitura").isArray())
                .andExpect(jsonPath("$.historicosLeitura[0].capituloInicial").value(0.0))
                .andExpect(jsonPath("$.historicosLeitura[0].capituloFinal").value(15.0));
    }

    @Test
    @DisplayName("PATCH /api/catalogo/{id}/visibilidade deve atualizar visibilidade")
    void deveAtualizarVisibilidadeComSucesso() throws Exception {
        String bodyAdicao = String.format("""
                {
                    "obraId": %d,
                    "visibilidade": "PUBLICO"
                }
                """, obraId);

        MvcResult result = mockMvc.perform(post("/api/catalogo")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyAdicao))
                .andExpect(status().isCreated())
                .andReturn();

        String idStr = result.getResponse().getContentAsString().split("\"id\":")[1].split(",")[0];
        Long itemId = Long.parseLong(idStr);

        String bodyPatch = """
                {
                    "visibilidade": "PRIVADO"
                }
                """;

        mockMvc.perform(patch("/api/catalogo/" + itemId + "/visibilidade")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyPatch))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.visibilidade").value("PRIVADO"));
    }

    @Test
    @DisplayName("GET /api/catalogo/minha-estante deve listar obras do usuário autenticado")
    void deveListarMinhaEstante() throws Exception {
        String bodyAdicao = String.format("""
                {
                    "obraId": %d,
                    "visibilidade": "PUBLICO"
                }
                """, obraId);

        mockMvc.perform(post("/api/catalogo")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyAdicao))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/catalogo/minha-estante")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].obraTitulo").value("Vinland Saga"));
    }

    @Test
    @DisplayName("DELETE /api/catalogo/{id} deve remover item da estante")
    void deveRemoverItemDaEstante() throws Exception {
        String bodyAdicao = String.format("""
                {
                    "obraId": %d,
                    "visibilidade": "PUBLICO"
                }
                """, obraId);

        MvcResult result = mockMvc.perform(post("/api/catalogo")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyAdicao))
                .andExpect(status().isCreated())
                .andReturn();

        String idStr = result.getResponse().getContentAsString().split("\"id\":")[1].split(",")[0];
        Long itemId = Long.parseLong(idStr);

        mockMvc.perform(delete("/api/catalogo/" + itemId)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNoContent());

        assertThat(itemCatalogoRepository.findById(itemId)).isEmpty();
    }

    @Test
    @DisplayName("POST /api/catalogo sem token deve retornar 401 Unauthorized com RFC 9457")
    void deveRetornar401SemAutenticacao() throws Exception {
        mockMvc.perform(post("/api/catalogo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.title").value("Não Autorizado"));
    }
}
