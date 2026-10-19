package dev.shiori.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import dev.shiori.domain.exception.RegraDeNegocioException;

class ConsentimentoUsuarioTest {

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuario = new Usuario("leitor@shiori.dev", "senha123", "leitor_privacidade");
    }

    @Test
    @DisplayName("Deve registrar consentimento com cookies necessários sempre verdadeiros e opções granulares")
    void deveRegistrarConsentimentoCorretamente() {
        ConsentimentoUsuario consentimento = new ConsentimentoUsuario(
                usuario,
                "v1.0",
                "v1.0",
                true,
                false,
                true,
                "192.168.1.100",
                "Mozilla/5.0 Chrome/120.0"
        );

        assertThat(consentimento.getUsuario()).isEqualTo(usuario);
        assertThat(consentimento.getVersaoTermosUso()).isEqualTo("v1.0");
        assertThat(consentimento.getVersaoPoliticaPrivacidade()).isEqualTo("v1.0");
        assertThat(consentimento.isCookiesNecessarios()).isTrue();
        assertThat(consentimento.isCookiesAnaliticos()).isTrue();
        assertThat(consentimento.isCookiesMarketing()).isFalse();
        assertThat(consentimento.isCookiesFuncionais()).isTrue();
        assertThat(consentimento.getIpOrigem()).isEqualTo("192.168.1.100");
        assertThat(consentimento.getUserAgent()).contains("Chrome");
        assertThat(consentimento.getDataConsentimento()).isNotNull();
        assertThat(consentimento.isAtivo()).isTrue();
    }

    @Test
    @DisplayName("Criação de consentimento deve falhar sem usuário ou sem versões de termos")
    void deveFalharSemCamposObrigatorios() {
        assertThatThrownBy(() -> new ConsentimentoUsuario(null, "v1.0", "v1.0", false, false, false, null, null))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("usuário titular do consentimento é obrigatório");

        assertThatThrownBy(() -> new ConsentimentoUsuario(usuario, "   ", "v1.0", false, false, false, null, null))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("versão dos termos de uso é obrigatória");

        assertThatThrownBy(() -> new ConsentimentoUsuario(usuario, "v1.0", null, false, false, false, null, null))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("versão da política de privacidade é obrigatória");
    }

    @Test
    @DisplayName("Revogação de consentimento deve marcar ativo como false")
    void deveRevogarConsentimento() {
        ConsentimentoUsuario consentimento = new ConsentimentoUsuario(
                usuario,
                "v1.0",
                "v1.0",
                true,
                true,
                true,
                "10.0.0.1",
                null
        );

        consentimento.revogar();
        assertThat(consentimento.isAtivo()).isFalse();
    }

    @Test
    @DisplayName("Registro de novo consentimento na entidade Usuario deve revogar anterior e manter o mais recente ativo")
    void deveGerenciarHistoricoDeConsentimentosNoUsuario() {
        ConsentimentoUsuario c1 = usuario.registrarConsentimento("v1.0", "v1.0", false, false, false, "10.0.0.1", "Browser A");
        assertThat(c1.isAtivo()).isTrue();
        assertThat(usuario.getConsentimentoAtual()).isEqualTo(c1);

        ConsentimentoUsuario c2 = usuario.registrarConsentimento("v1.1", "v1.1", true, false, true, "10.0.0.2", "Browser B");
        assertThat(c1.isAtivo()).isFalse();
        assertThat(c2.isAtivo()).isTrue();
        assertThat(usuario.getConsentimentoAtual()).isEqualTo(c2);
        assertThat(usuario.getConsentimentos()).containsExactly(c1, c2);
    }
}
