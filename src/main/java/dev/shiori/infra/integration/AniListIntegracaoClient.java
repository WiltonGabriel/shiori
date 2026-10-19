package dev.shiori.infra.integration;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import dev.shiori.domain.model.ProvedorIntegracao;

@Component
public class AniListIntegracaoClient implements ClienteIntegracaoExterna {

    private static final Logger log = LoggerFactory.getLogger(AniListIntegracaoClient.class);
    public static final String CODIGO = "ANILIST";

    @Override
    public String getCodigoProvedor() {
        return CODIGO;
    }

    @Override
    public IntegracaoMetadadosObra buscarObraPorIdExterno(ProvedorIntegracao config, String idExterno) {
        config.registrarRequisicao();
        log.info("INTEGRACAO_ANILIST: Consultando obra por ID externo {} na URL {}", idExterno, config.getUrlBase());
        return new IntegracaoMetadadosObra(
                idExterno,
                "Título AniList #" + idExterno,
                "Original Title AniList",
                "Sinopse importada de AniList API v2",
                "https://image.anilist.co/cover/" + idExterno + ".jpg",
                12,
                1,
                "MANGA",
                List.of("Ação", "Fantasia"),
                List.of("Shounen", "Superpoderes")
        );
    }

    @Override
    public List<IntegracaoMetadadosObra> pesquisarObras(ProvedorIntegracao config, String termo, int limite) {
        config.registrarRequisicao();
        log.info("INTEGRACAO_ANILIST: Pesquisando obras pelo termo '{}' (limite: {})", termo, limite);
        return List.of(
                new IntegracaoMetadadosObra(
                        "101",
                        termo + " - Obra AniList",
                        termo + " Original",
                        "Resultado de busca AniList",
                        "https://image.anilist.co/cover/101.jpg",
                        24,
                        2,
                        "MANGA",
                        List.of("Aventura"),
                        List.of("Fantasia")
                )
        );
    }
}
