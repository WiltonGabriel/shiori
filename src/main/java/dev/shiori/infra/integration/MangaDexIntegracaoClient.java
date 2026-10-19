package dev.shiori.infra.integration;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import dev.shiori.domain.model.ProvedorIntegracao;

@Component
public class MangaDexIntegracaoClient implements ClienteIntegracaoExterna {

    private static final Logger log = LoggerFactory.getLogger(MangaDexIntegracaoClient.class);
    public static final String CODIGO = "MANGADEX";

    @Override
    public String getCodigoProvedor() {
        return CODIGO;
    }

    @Override
    public IntegracaoMetadadosObra buscarObraPorIdExterno(ProvedorIntegracao config, String idExterno) {
        config.registrarRequisicao();
        log.info("INTEGRACAO_MANGADEX: Consultando obra por UUID {} na URL {}", idExterno, config.getUrlBase());
        return new IntegracaoMetadadosObra(
                CODIGO,
                idExterno,
                "Título MangaDex #" + idExterno,
                "Original Title MangaDex",
                "Sinopse oficial obtida da MangaDex API v5",
                "https://uploads.mangadex.org/covers/" + idExterno + ".jpg",
                50,
                5,
                "MANGA",
                List.of("Drama", "Mistério"),
                List.of("Seinen", "Psicológico")
        );
    }

    @Override
    public List<IntegracaoMetadadosObra> pesquisarObras(ProvedorIntegracao config, String termo, int limite) {
        config.registrarRequisicao();
        log.info("INTEGRACAO_MANGADEX: Pesquisando obras pelo termo '{}' (limite: {})", termo, limite);
        return List.of(
                new IntegracaoMetadadosObra(
                        CODIGO,
                        "202",
                        termo + " - MangaDex Edition",
                        termo + " Raw",
                        "Resultado de busca MangaDex",
                        "https://uploads.mangadex.org/covers/202.jpg",
                        100,
                        10,
                        "MANGA",
                        List.of("Sobrenatural"),
                        List.of("Ação")
                )
        );
    }
}
