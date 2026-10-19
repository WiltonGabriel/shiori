package dev.shiori.controller;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

import dev.shiori.domain.enums.TipoAtividadeFeed;
import dev.shiori.domain.enums.TipoObra;
import dev.shiori.domain.model.AtividadeFeed;
import dev.shiori.domain.model.Obra;
import dev.shiori.domain.model.Usuario;
import dev.shiori.repository.AtividadeFeedRepository;
import dev.shiori.repository.ObraRepository;
import dev.shiori.repository.UsuarioRepository;
import software.amazon.awssdk.services.s3.S3Client;

@SpringBootTest
@Transactional
@DisplayName("Testes de Integração: FeedController")
class FeedControllerTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ObraRepository obraRepository;

    @Autowired
    private AtividadeFeedRepository atividadeFeedRepository;

    @MockitoBean
    private S3Client s3Client;

    private MockMvc mockMvc;
    private String jwtToken;
    private Long usuarioId;

    @BeforeEach
    void setUp() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        String reg = """
                {
                    "email": "leitor.feed@shiori.dev",
                    "senha": "senhaSegura123",
                    "apelido": "leitor_feed"
                }
                """;

        MvcResult res = mockMvc.perform(post("/api/auth/registrar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reg))
                .andExpect(status().isCreated())
                .andReturn();

        jwtToken = res.getResponse().getContentAsString().split("\"token\":\"")[1].split("\"")[0];
        usuarioId = Long.parseLong(res.getResponse().getContentAsString().split("\"usuarioId\":")[1].split(",")[0]);
    }

    @Test
    @DisplayName("GET /api/feed deve retornar a timeline paginada do usuário autenticado")
    void deveRetornarTimelinePaginada() throws Exception {
        Usuario usuario = usuarioRepository.findById(usuarioId).orElseThrow();
        Obra obra = obraRepository.save(new Obra("Fullmetal Alchemist", "Hagane no Renkinjutsushi", "https://capas.shiori.dev/fma.jpg", "Irmãos Elric", 108, TipoObra.MANGA));

        AtividadeFeed atividade = new AtividadeFeed(
                usuario,
                TipoAtividadeFeed.CONCLUIU_LEITURA,
                obra,
                null,
                "leitor_feed concluiu a leitura de Fullmetal Alchemist!"
        );
        atividadeFeedRepository.save(atividade);

        mockMvc.perform(get("/api/feed")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].usuarioApelido").value("leitor_feed"))
                .andExpect(jsonPath("$.content[0].obraTitulo").value("Fullmetal Alchemist"))
                .andExpect(jsonPath("$.content[0].tipoAtividade").value("CONCLUIU_LEITURA"));
    }

    @Test
    @DisplayName("GET /api/feed sem autenticação deve retornar 401 com RFC 9457")
    void deveRetornar401SemAutenticacao() throws Exception {
        mockMvc.perform(get("/api/feed"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.title").value("Não Autorizado"));
    }
}
