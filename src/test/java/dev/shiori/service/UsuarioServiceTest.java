package dev.shiori.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import dev.shiori.domain.exception.RecursoNaoEncontradoException;
import dev.shiori.domain.model.Usuario;
import dev.shiori.dto.usuario.UsuarioPerfilResponse;
import dev.shiori.repository.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes Unitários: UsuarioService")
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private StorageService storageService;

    @InjectMocks
    private UsuarioService usuarioService;

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuario = new Usuario("usuario@shiori.dev", "senha-criptografada", "leitordashiori");
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
}
