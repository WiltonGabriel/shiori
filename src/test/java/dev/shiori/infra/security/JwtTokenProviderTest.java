package dev.shiori.infra.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import dev.shiori.domain.enums.Role;
import dev.shiori.domain.model.Usuario;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;
    private final String secret = "ShioriUltraSecretJwtSigningKeyWithAtLeast256BitsLengthForHmacSha256Security!";
    private final long expirationMs = 3600_000L; // 1 hora

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider(secret, expirationMs);
    }

    @Test
    @DisplayName("Deve gerar token JWT válido e extrair claims com sucesso")
    void deveGerarETestartokenValido() {
        Usuario usuario = new Usuario("leitor.jwt@shiori.dev", "senha123");
        usuario.concluirOnboarding("leitor_jwt", "America/Sao_Paulo", null, "Bio");

        String token = jwtTokenProvider.gerarToken(usuario);

        assertThat(token).isNotBlank();
        assertThat(jwtTokenProvider.validarToken(token)).isTrue();
        assertThat(jwtTokenProvider.extrairEmail(token)).isEqualTo("leitor.jwt@shiori.dev");
        assertThat(jwtTokenProvider.extrairRole(token)).isEqualTo(Role.ROLE_USER.name());
        assertThat(jwtTokenProvider.extrairOnboardingConcluido(token)).isTrue();
    }

    @Test
    @DisplayName("Deve invalidar token malformado ou nulo")
    void deveInvalidarTokenInvalido() {
        assertThat(jwtTokenProvider.validarToken(null)).isFalse();
        assertThat(jwtTokenProvider.validarToken("")).isFalse();
        assertThat(jwtTokenProvider.validarToken("token.invalido.malformado")).isFalse();
    }

    @Test
    @DisplayName("Deve invalidar token expirado")
    void deveInvalidarTokenExpirado() {
        // Provider com 1ms de expiração
        JwtTokenProvider providerExpirado = new JwtTokenProvider(secret, 1L);
        Usuario usuario = new Usuario("expirado@shiori.dev", "senha123");

        String token = providerExpirado.gerarToken(usuario);

        try {
            Thread.sleep(10);
        } catch (InterruptedException ignored) {
        }

        assertThat(providerExpirado.validarToken(token)).isFalse();
    }
}
