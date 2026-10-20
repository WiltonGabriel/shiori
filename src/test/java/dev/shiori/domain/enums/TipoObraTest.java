package dev.shiori.domain.enums;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TipoObraTest {

    @Test
    @DisplayName("Deve conter os 7 formatos literários oficiais")
    void deveConterFormatosOficiais() {
        assertThat(TipoObra.FORMATOS_OFICIAIS).containsExactlyInAnyOrder(
                TipoObra.MANGA,
                TipoObra.MANHWA,
                TipoObra.MANHUA,
                TipoObra.LIGHT_NOVEL,
                TipoObra.WEB_NOVEL,
                TipoObra.COMIC,
                TipoObra.LIVRO
        );
    }

    @Test
    @DisplayName("fromString deve mapear 'NOVEL' para LIGHT_NOVEL para manter compatibilidade retroativa")
    void deveMapearNovelParaLightNovel() {
        TipoObra resultado = TipoObra.fromString("NOVEL");
        assertThat(resultado).isEqualTo(TipoObra.LIGHT_NOVEL);

        TipoObra resultadoMinusculo = TipoObra.fromString("novel");
        assertThat(resultadoMinusculo).isEqualTo(TipoObra.LIGHT_NOVEL);
    }

    @Test
    @DisplayName("Deve manter NOVEL como valor de enum deprecado para segurança no JPA e banco de dados")
    @SuppressWarnings("deprecation")
    void deveManterNovelParaSegurancaJPA() {
        TipoObra legado = TipoObra.valueOf("NOVEL");
        assertThat(legado).isNotNull();
        assertThat(legado).isEqualTo(TipoObra.NOVEL);
    }
}
