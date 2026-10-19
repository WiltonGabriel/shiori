package dev.shiori.infra.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import dev.shiori.domain.enums.TipoObra;
import dev.shiori.domain.model.ProvedorIntegracao;

class KitsuIntegracaoClientTest {

    private KitsuIntegracaoClient client;
    private ProvedorIntegracao config;

    @BeforeEach
    void setUp() {
        client = new KitsuIntegracaoClient(new ObjectMapper());
        config = new ProvedorIntegracao("KITSU", "Kitsu API", "https://kitsu.io/api/edge", null, 15000L);
    }

    @Test
    @DisplayName("Código do provedor deve ser KITSU")
    void deveRetornarCodigoProvedor() {
        assertThat(client.getCodigoProvedor()).isEqualTo("KITSU");
    }

    @Test
    @DisplayName("Pesquisa deve registrar requisição e retornar obras com formato categorizado (Light Novel)")
    void devePesquisarNovelsComResiliencia() {
        long reqsAntes = config.getRequisicoesHoje();
        List<IntegracaoMetadadosObra> resultados = client.pesquisarObras(config, "Sword Art Online", 5);

        assertThat(config.getRequisicoesHoje()).isEqualTo(reqsAntes + 1);
        assertThat(resultados).isNotEmpty();
        assertThat(resultados.get(0).provedorCodigo()).isEqualTo("KITSU");
        assertThat(resultados.get(0).tipoObra()).isIn(TipoObra.LIGHT_NOVEL.name(), TipoObra.WEB_NOVEL.name(), TipoObra.MANGA.name());
    }

    @Test
    @DisplayName("Buscar obra por ID externo na Kitsu deve registrar requisição e retornar metadados")
    void deveBuscarObraPorIdComResiliencia() {
        long reqsAntes = config.getRequisicoesHoje();
        IntegracaoMetadadosObra obra = client.buscarObraPorIdExterno(config, "14242");

        assertThat(config.getRequisicoesHoje()).isEqualTo(reqsAntes + 1);
        assertThat(obra).isNotNull();
        assertThat(obra.provedorCodigo()).isEqualTo("KITSU");
        assertThat(obra.tipoObra()).isIn(TipoObra.LIGHT_NOVEL.name(), TipoObra.WEB_NOVEL.name(), TipoObra.MANGA.name());
    }
}
