package dev.shiori.infra.integration;

import java.util.List;

import dev.shiori.domain.model.ProvedorIntegracao;

public interface ClienteIntegracaoExterna {

    String getCodigoProvedor();

    IntegracaoMetadadosObra buscarObraPorIdExterno(ProvedorIntegracao config, String idExterno);

    List<IntegracaoMetadadosObra> pesquisarObras(ProvedorIntegracao config, String termo, int limite);
}
