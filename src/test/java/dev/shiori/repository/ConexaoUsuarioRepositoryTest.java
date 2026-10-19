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

import dev.shiori.domain.enums.StatusConexao;
import dev.shiori.domain.model.ConexaoUsuario;
import dev.shiori.domain.model.Usuario;

@SpringBootTest
@Transactional
class ConexaoUsuarioRepositoryTest {

    @Autowired
    private ConexaoUsuarioRepository conexaoUsuarioRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    private Usuario alice;
    private Usuario bob;

    @BeforeEach
    void setup() {
        alice = usuarioRepository.save(new Usuario("alice@shiori.dev", "senha123"));
        bob = usuarioRepository.save(new Usuario("bob@shiori.dev", "senha123"));
    }

    @Test
    @DisplayName("Deve gerenciar ciclo de vida da conexão e verificar se são conexões")
    void deveGerenciarConexao() {
        ConexaoUsuario conexao = new ConexaoUsuario(alice, bob);
        conexaoUsuarioRepository.save(conexao);

        Optional<ConexaoUsuario> pendente = conexaoUsuarioRepository.findConexaoEntre(alice.getId(), bob.getId());
        assertThat(pendente).isPresent();
        assertThat(conexaoUsuarioRepository.saoConexoes(alice.getId(), bob.getId())).isFalse();

        conexao.aceitar();
        conexaoUsuarioRepository.save(conexao);

        assertThat(conexaoUsuarioRepository.saoConexoes(alice.getId(), bob.getId())).isTrue();
        assertThat(conexaoUsuarioRepository.saoConexoes(bob.getId(), alice.getId())).isTrue();

        List<ConexaoUsuario> aceitas = conexaoUsuarioRepository.findConexoesAceitas(alice.getId());
        assertThat(aceitas).hasSize(1);
    }

    @Test
    @DisplayName("Deve buscar conexões pendentes por solicitante ou recebedor")
    void deveBuscarConexoesPendentes() {
        ConexaoUsuario conexao = new ConexaoUsuario(alice, bob);
        conexaoUsuarioRepository.save(conexao);

        List<ConexaoUsuario> recebidas = conexaoUsuarioRepository.findByRecebedorIdAndStatus(bob.getId(), StatusConexao.PENDENTE);
        assertThat(recebidas).hasSize(1);

        List<ConexaoUsuario> enviadas = conexaoUsuarioRepository.findBySolicitanteIdAndStatus(alice.getId(), StatusConexao.PENDENTE);
        assertThat(enviadas).hasSize(1);
    }
}
