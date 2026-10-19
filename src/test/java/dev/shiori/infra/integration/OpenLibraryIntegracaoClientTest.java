package dev.shiori.infra.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import dev.shiori.domain.model.ProvedorIntegracao;

class OpenLibraryIntegracaoClientTest {

    private OpenLibraryIntegracaoClient client;
    private ProvedorIntegracao config;

    @BeforeEach
    void setUp() {
        client = new OpenLibraryIntegracaoClient(new ObjectMapper());
        config = new ProvedorIntegracao("OPENLIBRARY", "Open Library API", "https://openlibrary.org", null, 20000L);
    }

    @Test
    @DisplayName("Código do provedor deve ser OPENLIBRARY")
    void deveRetornarCodigoProvedor() {
        assertThat(client.getCodigoProvedor()).isEqualTo("OPENLIBRARY");
    }

    @Test
    @DisplayName("Pesquisa deve registrar requisição e retornar formato LIVRO com resiliência")
    void devePesquisarLivrosComResiliencia() {
        long reqsAntes = config.getRequisicoesHoje();
        List<IntegracaoMetadadosObra> resultados = client.pesquisarObras(config, "Dom Casmurro", 5);

        assertThat(config.getRequisicoesHoje()).isEqualTo(reqsAntes + 1);
        assertThat(resultados).isNotEmpty();
        assertThat(resultados.get(0).tipoObra()).isEqualTo("LIVRO");
        assertThat(resultados.get(0).provedorCodigo()).isEqualTo("OPENLIBRARY");
    }

    @Test
    @DisplayName("Buscar obra por ID deve registrar requisição e retornar metadados com resiliência")
    void deveBuscarObraPorIdComResiliencia() {
        long reqsAntes = config.getRequisicoesHoje();
        IntegracaoMetadadosObra obra = client.buscarObraPorIdExterno(config, "OL45804W");

        assertThat(config.getRequisicoesHoje()).isEqualTo(reqsAntes + 1);
        assertThat(obra).isNotNull();
        assertThat(obra.tipoObra()).isEqualTo("LIVRO");
        assertThat(obra.provedorCodigo()).isEqualTo("OPENLIBRARY");
    }
}
