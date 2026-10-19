package dev.shiori.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import dev.shiori.domain.enums.Role;
import dev.shiori.domain.model.Usuario;

@SpringBootTest
@Transactional
class UsuarioRepositoryTest {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Test
    @DisplayName("Deve salvar usuário e recuperar por e-mail com sucesso")
    void deveSalvarERecuperarPorEmail() {
        Usuario usuario = new Usuario("leitor.repo@shiori.dev", "senhaForte123");
        usuario.concluirOnboarding("leitor_repo", "America/Sao_Paulo", null, "Bio de teste");
        usuarioRepository.save(usuario);

        Optional<Usuario> encontrado = usuarioRepository.findByEmail("leitor.repo@shiori.dev");

        assertThat(encontrado).isPresent();
        assertThat(encontrado.get().getApelido()).isEqualTo("leitor_repo");
        assertThat(encontrado.get().getUltimaAtualizacao()).isNotNull();
    }

    @Test
    @DisplayName("Deve verificar existência por e-mail e apelido")
    void deveVerificarExistenciaPorEmailEApelido() {
        Usuario usuario = new Usuario("autor@shiori.dev", "senha123");
        usuario.concluirOnboarding("autor_top", "America/Sao_Paulo", null, null);
        usuarioRepository.save(usuario);

        assertThat(usuarioRepository.existsByEmail("autor@shiori.dev")).isTrue();
        assertThat(usuarioRepository.existsByEmail("inexistente@shiori.dev")).isFalse();
        assertThat(usuarioRepository.existsByApelido("autor_top")).isTrue();
        assertThat(usuarioRepository.existsByApelido("fantasma")).isFalse();
    }

    @Test
    @DisplayName("Deve filtrar usuários ativos e por Role com paginação")
    void deveFiltrarUsuariosAtivosEPorRole() {
        Usuario u1 = new Usuario("u1@shiori.dev", "senha1");
        Usuario u2 = new Usuario("u2@shiori.dev", "senha2");
        u2.desativarConta();
        usuarioRepository.save(u1);
        usuarioRepository.save(u2);

        Page<Usuario> ativos = usuarioRepository.findByAtivoTrue(PageRequest.of(0, 10));
        boolean temU1Ativo = false;
        boolean temU2Ativo = false;
        for (Usuario u : ativos.getContent()) {
            if ("u1@shiori.dev".equals(u.getEmail())) {
                temU1Ativo = true;
            }
            if ("u2@shiori.dev".equals(u.getEmail())) {
                temU2Ativo = true;
            }
        }
        assertThat(temU1Ativo).isTrue();
        assertThat(temU2Ativo).isFalse();

        Page<Usuario> roles = usuarioRepository.findByRole(Role.ROLE_USER, PageRequest.of(0, 10));
        assertThat(roles.getTotalElements()).isGreaterThanOrEqualTo(2);
    }
}
