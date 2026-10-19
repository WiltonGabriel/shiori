package dev.shiori.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import dev.shiori.domain.enums.StatusLeitura;
import dev.shiori.domain.enums.TipoObra;
import dev.shiori.domain.enums.Visibilidade;
import dev.shiori.domain.exception.RecursoNaoEncontradoException;
import dev.shiori.domain.model.ItemCatalogo;
import dev.shiori.domain.model.Obra;
import dev.shiori.domain.model.Usuario;
import dev.shiori.dto.usuario.UsuarioPerfilPublicoResponse;
import dev.shiori.dto.usuario.UsuarioPerfilResponse;
import dev.shiori.repository.AtividadeFeedRepository;
import dev.shiori.repository.ComentarioRepository;
import dev.shiori.repository.ConexaoUsuarioRepository;
import dev.shiori.repository.HistoricoLeituraRepository;
import dev.shiori.repository.ItemCatalogoRepository;
import dev.shiori.repository.ObraRepository;
import dev.shiori.repository.SugestaoEdicaoObraRepository;
import dev.shiori.repository.TagRepository;
import dev.shiori.repository.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes Unitários: UsuarioService")
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private ItemCatalogoRepository itemCatalogoRepository;

    @Mock
    private ConexaoUsuarioRepository conexaoUsuarioRepository;

    @Mock
    private ObraRepository obraRepository;

    @Mock
    private TagRepository tagRepository;

    @Mock
    private ComentarioRepository comentarioRepository;

    @Mock
    private SugestaoEdicaoObraRepository sugestaoEdicaoObraRepository;

    @Mock
    private HistoricoLeituraRepository historicoLeituraRepository;

    @Mock
    private AtividadeFeedRepository atividadeFeedRepository;

    @Mock
    private StorageService storageService;

    @Captor
    private org.mockito.ArgumentCaptor<Collection<Visibilidade>> visCaptor;

    @InjectMocks
    private UsuarioService usuarioService;

    private Usuario usuario;
    private Obra obra;

    @BeforeEach
    void setUp() {
        usuario = new Usuario("usuario@shiori.dev", "senha-criptografada", "leitordashiori");
        ReflectionTestUtils.setField(usuario, "id", 1L);

        obra = new Obra("Berserk", "Beruseruku", "https://capas.shiori.dev/berserk.jpg", "Sinopse", 364, TipoObra.MANGA);
        ReflectionTestUtils.setField(obra, "id", 100L);
    }

    @Test
    @DisplayName("Deve atualizar o avatar do usuário com sucesso")
    void deveAtualizarAvatarComSucesso() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "avatar.png",
                "image/png",
                "conteudo-imagem".getBytes()
        );

        String urlPublica = "http://localhost:9000/shiori-media/avatares/uuid-gerado.png";

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(storageService.uploadFile(file, "avatares")).thenReturn(urlPublica);
        when(usuarioRepository.save(usuario)).thenReturn(usuario);

        UsuarioPerfilResponse response = usuarioService.atualizarAvatar(1L, file);

        assertThat(response).isNotNull();
        assertThat(response.fotoUrl()).isEqualTo(urlPublica);
        assertThat(usuario.getFotoUrl()).isEqualTo(urlPublica);
        verify(usuarioRepository).save(usuario);
    }

    @Test
    @DisplayName("Deve lançar RecursoNaoEncontradoException ao atualizar avatar de usuário inexistente")
    void deveLancarRecursoNaoEncontradoExceptionAoAtualizarAvatarDeInexistente() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "avatar.png",
                "image/png",
                "dados".getBytes()
        );

        when(usuarioRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioService.atualizarAvatar(999L, file))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessageContaining("Usuário não encontrado");
    }

    @Test
    @DisplayName("Deve buscar perfil por ID com sucesso")
    void deveBuscarPerfilPorIdComSucesso() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

        UsuarioPerfilResponse response = usuarioService.buscarPerfilPorId(1L);

        assertThat(response).isNotNull();
        assertThat(response.email()).isEqualTo("usuario@shiori.dev");
        assertThat(response.apelido()).isEqualTo("leitordashiori");
    }

    @Test
    @DisplayName("Perfil Visível: Quando solicitante é o próprio dono, busca todas as visibilidades (PUBLICO, RESTRITO, PRIVADO)")
    void perfilVisivelParaProprioDono() {
        when(usuarioRepository.findByApelido("leitordashiori")).thenReturn(Optional.of(usuario));

        Pageable pageable = PageRequest.of(0, 10);
        ItemCatalogo item = new ItemCatalogo(usuario, obra, Visibilidade.PRIVADO, StatusLeitura.LENDO, "https://link-secreto.com");
        ReflectionTestUtils.setField(item, "id", 50L);

        when(itemCatalogoRepository.findByUsuarioIdAndVisibilidadeIn(eq(1L), visCaptor.capture(), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(item), pageable, 1));

        UsuarioPerfilPublicoResponse response = usuarioService.obterPerfilVisivel("leitordashiori", 1L, pageable);

        assertThat(response).isNotNull();
        assertThat(response.apelido()).isEqualTo("leitordashiori");
        assertThat(visCaptor.getValue()).containsExactlyInAnyOrder(Visibilidade.PUBLICO, Visibilidade.RESTRITO, Visibilidade.PRIVADO);
        assertThat(response.estante().getContent().get(0).urlLeitura()).isEqualTo("https://link-secreto.com");
    }

    @Test
    @DisplayName("Perfil Visível: Quando solicitante é amigo conectado (ACEITA), busca PUBLICO e RESTRITO, e mascara urlLeitura")
    void perfilVisivelParaAmigoConectado() {
        when(usuarioRepository.findByApelido("leitordashiori")).thenReturn(Optional.of(usuario));
        when(conexaoUsuarioRepository.saoConexoes(2L, 1L)).thenReturn(true);

        Pageable pageable = PageRequest.of(0, 10);
        ItemCatalogo itemRestrito = new ItemCatalogo(usuario, obra, Visibilidade.RESTRITO, StatusLeitura.LENDO, "https://link-secreto.com");
        ReflectionTestUtils.setField(itemRestrito, "id", 50L);

        when(itemCatalogoRepository.findByUsuarioIdAndVisibilidadeIn(eq(1L), visCaptor.capture(), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(itemRestrito), pageable, 1));

        UsuarioPerfilPublicoResponse response = usuarioService.obterPerfilVisivel("leitordashiori", 2L, pageable);

        assertThat(response).isNotNull();
        assertThat(response.saoConexoes()).isTrue();
        assertThat(visCaptor.getValue()).containsExactlyInAnyOrder(Visibilidade.PUBLICO, Visibilidade.RESTRITO);
        // Privacy by Design: Amigo NÃO tem acesso a urlLeitura
        assertThat(response.estante().getContent().get(0).urlLeitura()).isNull();
    }

    @Test
    @DisplayName("Perfil Visível: Quando solicitante é estranho ou anônimo, busca estritamente PUBLICO")
    void perfilVisivelParaEstranhoOuAnonimo() {
        when(usuarioRepository.findByApelido("leitordashiori")).thenReturn(Optional.of(usuario));

        Pageable pageable = PageRequest.of(0, 10);
        when(itemCatalogoRepository.findByUsuarioIdAndVisibilidadeIn(eq(1L), visCaptor.capture(), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(), pageable, 0));

        UsuarioPerfilPublicoResponse response = usuarioService.obterPerfilVisivel("leitordashiori", null, pageable);

        assertThat(response).isNotNull();
        assertThat(response.saoConexoes()).isFalse();
        assertThat(visCaptor.getValue()).containsExactly(Visibilidade.PUBLICO);
    }

    @Test
    @DisplayName("LGPD: Anonimização de conta deve expurgar dados pessoais e preservar integridade do catálogo global")
    void anonimizarContaLgpdDeveDesvincularObrasEExpurgarDadosPessoais() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

        usuarioService.anonimizarContaLGPD(1L);

        // 1. Catálogo e moderação pública preservados desvinculando autoria
        verify(obraRepository).desvincularCriador(1L);
        verify(tagRepository).desvincularCriador(1L);
        verify(sugestaoEdicaoObraRepository).desvincularUsuarioSugeridor(1L);
        verify(comentarioRepository).desvincularAutor(1L);

        // 2. Dados pessoais sociais e vínculos destruídos
        verify(atividadeFeedRepository).deletarPorUsuarioId(1L);
        verify(historicoLeituraRepository).deletarPorUsuarioId(1L);
        verify(itemCatalogoRepository).deletarPorUsuarioId(1L);
        verify(conexaoUsuarioRepository).deletarTodasConexoesDoUsuario(1L);

        // 3. Usuário anonimizado no banco
        verify(usuarioRepository).save(usuario);
        assertThat(usuario.isAtivo()).isFalse();
        assertThat(usuario.getSenha()).isEqualTo("CONTA_EXCLUIDA");
        assertThat(usuario.getEmail()).contains("@anon.shiori.dev");
        assertThat(usuario.getApelido()).contains("excluido_");
    }
}
