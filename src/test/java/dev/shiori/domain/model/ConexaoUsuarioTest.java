package dev.shiori.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import dev.shiori.domain.enums.StatusConexao;
import dev.shiori.domain.exception.RegraDeNegocioException;

class ConexaoUsuarioTest {

    private Usuario alice;
    private Usuario bob;

    @BeforeEach
    void setUp() {
        alice = new Usuario("alice@shiori.dev", "senha", "alice");
        bob = new Usuario("bob@shiori.dev", "senha", "bob");
    }

    @Test
    @DisplayName("Conexão deve nascer com status PENDENTE e data de criação")
    void deveCriarConexaoComStatusPendente() {
        ConexaoUsuario conexao = new ConexaoUsuario(alice, bob);

        assertThat(conexao.getSolicitante()).isEqualTo(alice);
        assertThat(conexao.getRecebedor()).isEqualTo(bob);
        assertThat(conexao.getStatus()).isEqualTo(StatusConexao.PENDENTE);
        assertThat(conexao.isPendente()).isTrue();
        assertThat(conexao.getDataCriacao()).isNotNull();
    }

    @Test
    @DisplayName("Não deve permitir conexão de usuário consigo mesmo")
    void naoDevePermitirConexaoConsigoMesmo() {
        assertThatThrownBy(() -> new ConexaoUsuario(alice, alice))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("não pode estabelecer uma conexão consigo mesmo");
    }

    @Test
    @DisplayName("Deve aceitar conexão alterando status para ACEITA")
    void deveAceitarConexao() {
        ConexaoUsuario conexao = new ConexaoUsuario(alice, bob);
        conexao.aceitar();

        assertThat(conexao.getStatus()).isEqualTo(StatusConexao.ACEITA);
        assertThat(conexao.isAceita()).isTrue();
        assertThat(conexao.getDataAtualizacao()).isNotNull();
    }

    @Test
    @DisplayName("Deve bloquear conexão")
    void deveBloquearConexao() {
        ConexaoUsuario conexao = new ConexaoUsuario(alice, bob);
        conexao.bloquear();

        assertThat(conexao.getStatus()).isEqualTo(StatusConexao.BLOQUEADA);
        assertThat(conexao.isBloqueada()).isTrue();

        // Não pode ser aceita se bloqueada
        assertThatThrownBy(() -> conexao.aceitar())
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("bloqueada não pode ser aceita diretamente");
    }

    @Test
    @DisplayName("Deve identificar envolvimento e obter o outro usuário")
    void deveIdentificarEnvolvimentoEOutroUsuario() {
        ConexaoUsuario conexao = new ConexaoUsuario(alice, bob);

        assertThat(conexao.envolve(alice)).isTrue();
        assertThat(conexao.envolve(bob)).isTrue();
        assertThat(conexao.envolve(null)).isFalse();

        assertThat(conexao.obterOutroUsuario(alice)).isEqualTo(bob);
        assertThat(conexao.obterOutroUsuario(bob)).isEqualTo(alice);

        Usuario estranho = new Usuario("estranho@shiori.dev", "senha", "estranho");
        assertThatThrownBy(() -> conexao.obterOutroUsuario(estranho))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("não faz parte desta conexão");
    }
}
