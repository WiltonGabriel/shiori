package dev.shiori.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import dev.shiori.domain.model.ConsentimentoUsuario;
import dev.shiori.domain.model.Usuario;

@SpringBootTest
@Transactional
class ConsentimentoUsuarioRepositoryTest {

    @Autowired
    private ConsentimentoUsuarioRepository consentimentoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    private Usuario usuario;

    @BeforeEach
    void setup() {
        usuario = usuarioRepository.save(new Usuario("consentimento.repo@shiori.dev", "senha123"));
    }

    @Test
    @DisplayName("Deve registrar e recuperar consentimento ativo do usuário")
    void deveRegistrarERecuperarConsentimentoAtivo() {
        ConsentimentoUsuario c1 = new ConsentimentoUsuario(usuario, "1.0", "1.0", false, false, false, "127.0.0.1", "Mozilla");
        consentimentoRepository.save(c1);

        Optional<ConsentimentoUsuario> ativo = consentimentoRepository.findByUsuarioIdAndAtivoTrue(usuario.getId());
        assertThat(ativo).isPresent();
        assertThat(ativo.get().getVersaoTermosUso()).isEqualTo("1.0");

        c1.revogar();
        consentimentoRepository.save(c1);

        ConsentimentoUsuario c2 = new ConsentimentoUsuario(usuario, "2.0", "2.0", true, false, false, "127.0.0.1", "Mozilla");
        consentimentoRepository.save(c2);

        Optional<ConsentimentoUsuario> novoAtivo = consentimentoRepository.findByUsuarioIdAndAtivoTrue(usuario.getId());
        assertThat(novoAtivo).isPresent();
        assertThat(novoAtivo.get().getVersaoTermosUso()).isEqualTo("2.0");

        List<ConsentimentoUsuario> historico = consentimentoRepository.findByUsuarioIdOrderByDataConsentimentoDesc(usuario.getId());
        assertThat(historico).hasSize(2);
    }
}
