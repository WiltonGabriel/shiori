package dev.shiori.infra.bootstrap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.security.crypto.password.PasswordEncoder;

import dev.shiori.domain.enums.Role;
import dev.shiori.domain.model.Usuario;
import dev.shiori.repository.FeatureFlagRepository;
import dev.shiori.repository.ProvedorIntegracaoRepository;
import dev.shiori.repository.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes Unitários: AdminBootstrapRunner")
class AdminBootstrapRunnerTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private FeatureFlagRepository featureFlagRepository;

    @Mock
    private ProvedorIntegracaoRepository provedorIntegracaoRepository;

    @Test
    @DisplayName("Deve provisionar sys_overseer quando ele ainda não existir no sistema")
    void deveProvisionarSysOverseerQuandoNaoExistir() {
        when(usuarioRepository.existsByApelido("sys_overseer")).thenReturn(false);
        when(usuarioRepository.existsByEmail("sys_overseer@shiori.dev")).thenReturn(false);
        when(passwordEncoder.encode("minhaSenha16Chars")).thenReturn("$2a$10$hashedPasswordHere");

        AdminBootstrapRunner runner = new AdminBootstrapRunner(
                usuarioRepository,
                passwordEncoder,
                featureFlagRepository,
                provedorIntegracaoRepository,
                "minhaSenha16Chars"
        );

        runner.run(new DefaultApplicationArguments());

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());

        Usuario salvo = captor.getValue();
        assertThat(salvo.getApelido()).isEqualTo("sys_overseer");
        assertThat(salvo.getEmail()).isEqualTo("sys_overseer@shiori.dev");
        assertThat(salvo.getRole()).isEqualTo(Role.ROLE_ADMIN);
        assertThat(salvo.isAdmin()).isTrue();
        assertThat(salvo.getPontosConfianca()).isGreaterThanOrEqualTo(1000);
        assertThat(salvo.getSenha()).isEqualTo("$2a$10$hashedPasswordHere");
    }

    @Test
    @DisplayName("Não deve recriar sys_overseer se ele já existir no banco")
    void naoDeveRecriarSysOverseerSeJaExistir() {
        when(usuarioRepository.existsByApelido("sys_overseer")).thenReturn(true);

        AdminBootstrapRunner runner = new AdminBootstrapRunner(
                usuarioRepository,
                passwordEncoder,
                featureFlagRepository,
                provedorIntegracaoRepository,
                "minhaSenha16Chars"
        );

        runner.run(new DefaultApplicationArguments());

        verify(usuarioRepository, never()).save(any());
    }
}
