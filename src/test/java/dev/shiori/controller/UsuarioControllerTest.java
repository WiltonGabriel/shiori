package dev.shiori.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import dev.shiori.domain.model.Usuario;
import dev.shiori.repository.UsuarioRepository;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

@SpringBootTest
@Transactional
@DisplayName("Testes de Integração: UsuarioController")
class UsuarioControllerTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private UsuarioRepository usuarioRepository;

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

        // Cadastra um usuário para os testes e obtém o token JWT
        String jsonRegistro = """
                {
                    "email": "avatar.leitor@shiori.dev",
                    "senha": "senhaSegura123",
                    "apelido": "avatar_leitor"
                }
                """;

        MvcResult result = mockMvc.perform(post("/api/auth/registrar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRegistro))
                .andExpect(status().isCreated())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        jwtToken = responseBody.split("\"token\":\"")[1].split("\"")[0];
        String idStr = responseBody.split("\"usuarioId\":")[1].split(",")[0];
        usuarioId = Long.parseLong(idStr);
    }

    @Test
    @DisplayName("POST /api/usuarios/me/avatar deve fazer upload com sucesso e atualizar fotoUrl")
    void deveFazerUploadDeAvatarComSucesso() throws Exception {
        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenReturn(PutObjectResponse.builder().build());

        MockMultipartFile avatarFile = new MockMultipartFile(
                "file",
                "avatar.png",
                "image/png",
                "dados-imagem-avatar".getBytes()
        );

        mockMvc.perform(multipart("/api/usuarios/me/avatar")
                        .file(avatarFile)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(usuarioId))
                .andExpect(jsonPath("$.email").value("avatar.leitor@shiori.dev"))
                .andExpect(jsonPath("$.apelido").value("avatar_leitor"))
                .andExpect(jsonPath("$.fotoUrl").isString())
                .andExpect(jsonPath("$.fotoUrl").value(org.hamcrest.Matchers.containsString("avatares/")));

        Usuario usuarioAtualizado = usuarioRepository.findById(usuarioId).orElseThrow();
        assertThat(usuarioAtualizado.getFotoUrl()).isNotNull();
        assertThat(usuarioAtualizado.getFotoUrl()).contains("avatares/");
    }

    @Test
    @DisplayName("POST /api/usuarios/me/avatar sem autenticação deve retornar 401 Unauthorized com RFC 9457")
    void deveRetornar401AoTentarUploadSemAutenticacao() throws Exception {
        MockMultipartFile avatarFile = new MockMultipartFile(
                "file",
                "avatar.png",
                "image/png",
                "dados".getBytes()
        );

        mockMvc.perform(multipart("/api/usuarios/me/avatar")
                        .file(avatarFile))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.title").value("Não Autorizado"));
    }

    @Test
    @DisplayName("POST /api/usuarios/me/avatar com extensão inválida (.pdf) deve retornar 415 com RFC 9457")
    void deveRetornar415ParaExtensaoNaoSuportada() throws Exception {
        MockMultipartFile pdfFile = new MockMultipartFile(
                "file",
                "documento.pdf",
                "application/pdf",
                "conteudo-pdf".getBytes()
        );

        mockMvc.perform(multipart("/api/usuarios/me/avatar")
                        .file(pdfFile)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.status").value(415))
                .andExpect(jsonPath("$.title").value("Mídia Não Suportada"))
                .andExpect(jsonPath("$.detail").value(org.hamcrest.Matchers.containsString("Extensão de imagem 'pdf' não suportada")));
    }

    @Test
    @DisplayName("POST /api/usuarios/me/avatar com arquivo vazio deve retornar 400 com RFC 9457")
    void deveRetornar400ParaArquivoVazio() throws Exception {
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file",
                "vazio.png",
                "image/png",
                new byte[0]
        );

        mockMvc.perform(multipart("/api/usuarios/me/avatar")
                        .file(emptyFile)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.title").value("Arquivo Inválido"));
    }

    @Test
    @DisplayName("GET /api/usuarios/me deve retornar perfil completo do usuário autenticado")
    void deveRetornarPerfilCompletoDoUsuarioLogado() throws Exception {
        mockMvc.perform(get("/api/usuarios/me")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(usuarioId))
                .andExpect(jsonPath("$.email").value("avatar.leitor@shiori.dev"))
                .andExpect(jsonPath("$.apelido").value("avatar_leitor"))
                .andExpect(jsonPath("$.ativo").value(true));
    }
}
