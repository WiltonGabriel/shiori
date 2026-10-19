package dev.shiori.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.shiori.domain.enums.TipoObra;
import dev.shiori.domain.exception.RecursoNaoEncontradoException;
import dev.shiori.domain.exception.RegraDeNegocioException;
import dev.shiori.domain.model.Obra;
import dev.shiori.domain.model.ProvedorIntegracao;
import dev.shiori.infra.integration.ClienteIntegracaoExterna;
import dev.shiori.infra.integration.IntegracaoMetadadosObra;
import dev.shiori.infra.integration.ProvedorIntegracaoFactory;
import dev.shiori.repository.ObraRepository;
import dev.shiori.repository.ProvedorIntegracaoRepository;

/**
 * Serviço de orquestração de buscas e consumo de metadados em provedores externos (AniList, MangaDex, Open Library, Kitsu).
 * Garante alta resiliência (fault-tolerance), cache persistente defensivo e cópia local no Hub.
 */
@Service
@Transactional
public class IntegracaoCatalogoService {

    private static final Logger log = LoggerFactory.getLogger(IntegracaoCatalogoService.class);

    private final ProvedorIntegracaoRepository provedorIntegracaoRepository;
    private final ProvedorIntegracaoFactory provedorIntegracaoFactory;
    private final ObraRepository obraRepository;

    public IntegracaoCatalogoService(
            ProvedorIntegracaoRepository provedorIntegracaoRepository,
            ProvedorIntegracaoFactory provedorIntegracaoFactory,
            ObraRepository obraRepository) {
        this.provedorIntegracaoRepository = provedorIntegracaoRepository;
        this.provedorIntegracaoFactory = provedorIntegracaoFactory;
        this.obraRepository = obraRepository;
    }

    /**
     * Consulta os provedores externos ativos de forma resiliente, persistindo cópia defensiva no banco interno do Hub.
     */
    public List<IntegracaoMetadadosObra> pesquisarEmProvedoresExternos(String termo, Integer limite, Set<TipoObra> formatosPermitidos) {
        if (termo == null || termo.trim().isBlank()) {
            return Collections.emptyList();
        }

        int maxPorProvedor = (limite != null && limite > 0) ? Math.min(limite, 10) : 5;
        List<ProvedorIntegracao> provedores = provedorIntegracaoRepository.findAll();
        List<IntegracaoMetadadosObra> agregados = new ArrayList<>();

        for (ProvedorIntegracao provedor : provedores) {
            if (!provedor.isAtivo()) {
                log.debug("INTEGRACAO: Provedor {} inativo. Pulando.", provedor.getCodigo());
                continue;
            }
            if (provedor.atingiuLimiteDiario()) {
                log.warn("INTEGRACAO: Provedor {} atingiu cota diária ({} reqs). Pulando.", provedor.getCodigo(), provedor.getLimiteDiario());
                continue;
            }

            try {
                ClienteIntegracaoExterna cliente = provedorIntegracaoFactory.obterCliente(provedor.getCodigo());
                List<IntegracaoMetadadosObra> resultados = cliente.pesquisarObras(provedor, termo.trim(), maxPorProvedor);
                provedorIntegracaoRepository.save(provedor);

                if (resultados != null) {
                    for (IntegracaoMetadadosObra item : resultados) {
                        TipoObra tipo = TipoObra.fromString(item.tipoObra());
                        if (formatosPermitidos == null || formatosPermitidos.isEmpty() || (tipo != null && formatosPermitidos.contains(tipo))) {
                            agregados.add(item);
                            // Cópia defensiva imediata no Hub
                            try {
                                salvarOuAtualizarCopiaDefensiva(item);
                            } catch (Exception ex) {
                                log.warn("CÓPIA DEFENSIVA: Falha ao persistir obra externa '{}': {}", item.tituloPrincipal(), ex.getMessage());
                            }
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("INTEGRACAO: Erro ao consultar provedor {}: {}. Continuando com outros provedores.", provedor.getCodigo(), e.getMessage());
            }
        }

        return agregados;
    }

    /**
     * Consulta detalhada de uma obra por ID externo em um provedor específico para preenchimento automático de formulário.
     */
    public IntegracaoMetadadosObra consultarObraPorIdExterno(String codigoProvedor, String idExterno) {
        ProvedorIntegracao provedor = provedorIntegracaoRepository.findByCodigoIgnoreCase(codigoProvedor)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Provedor não encontrado: " + codigoProvedor));

        if (!provedor.isAtivo()) {
            throw new RegraDeNegocioException("O provedor '" + codigoProvedor + "' está inativo");
        }
        if (provedor.atingiuLimiteDiario()) {
            throw new RegraDeNegocioException("Limite diário esgotado para o provedor " + codigoProvedor);
        }

        try {
            ClienteIntegracaoExterna cliente = provedorIntegracaoFactory.obterCliente(codigoProvedor);
            IntegracaoMetadadosObra metadados = cliente.buscarObraPorIdExterno(provedor, idExterno);
            provedorIntegracaoRepository.save(provedor);

            if (metadados != null) {
                try {
                    salvarOuAtualizarCopiaDefensiva(metadados);
                } catch (Exception ex) {
                    log.warn("CÓPIA DEFENSIVA: Falha ao persistir metadados detalhados de '{}': {}", metadados.tituloPrincipal(), ex.getMessage());
                }
            }

            return metadados;
        } catch (RegraDeNegocioException | RecursoNaoEncontradoException e) {
            throw e;
        } catch (Exception e) {
            log.error("INTEGRACAO: Falha ao consultar obra '{}' no provedor '{}': {}", idExterno, codigoProvedor, e.getMessage());
            throw new dev.shiori.domain.exception.IntegracaoExternaException(
                    "O provedor " + codigoProvedor + " está temporariamente indisponível. Tente novamente em instantes.",
                    org.springframework.http.HttpStatus.BAD_GATEWAY
            );
        }
    }

    @Transactional
    public Obra salvarOuAtualizarCopiaDefensiva(IntegracaoMetadadosObra item) {
        if (item == null || item.tituloPrincipal() == null || item.tituloPrincipal().isBlank()) {
            return null;
        }

        String provedor = item.provedorCodigo() != null ? item.provedorCodigo().toLowerCase() : "";
        Optional<Obra> existente = Optional.empty();

        if (provedor.contains("mangadex")) {
            existente = obraRepository.findByMangadexId(item.idExterno());
        } else if (provedor.contains("anilist")) {
            existente = obraRepository.findByAnilistId(item.idExterno());
        } else if (provedor.contains("kitsu")) {
            existente = obraRepository.findByKitsuId(item.idExterno());
        } else if (provedor.contains("openlibrary")) {
            existente = obraRepository.findByIsbn(item.idExterno());
        }

        if (existente.isEmpty()) {
            existente = obraRepository.findByTituloPrincipalIgnoreCase(item.tituloPrincipal().trim());
        }

        TipoObra tipo = TipoObra.fromString(item.tipoObra());
        if (tipo == null) {
            tipo = TipoObra.MANGA;
        }

        if (existente.isPresent()) {
            Obra obra = existente.get();
            obra.atualizarSincronizacaoExterna(item.totalCapitulosLancados(), item.totalVolumes());
            return obraRepository.save(obra);
        } else {
            Obra novaObra = new Obra(
                    item.tituloPrincipal().trim(),
                    item.tituloOriginal(),
                    item.capaUrl(),
                    item.sinopse(),
                    item.totalCapitulosLancados(),
                    tipo
            );
            novaObra.promoverParaOficial();
            String mangadexId = provedor.contains("mangadex") ? item.idExterno() : null;
            String anilistId = provedor.contains("anilist") ? item.idExterno() : null;
            String kitsuId = provedor.contains("kitsu") ? item.idExterno() : null;
            String isbn = provedor.contains("openlibrary") ? item.idExterno() : null;
            String origem = !provedor.isBlank() ? provedor.toUpperCase() : "EXTERNO";

            novaObra.definirIdentificadoresExternos(mangadexId, anilistId, kitsuId, isbn, origem);
            log.info("CÓPIA DEFENSIVA: Obra externa '{}' gravada no Hub com origem [{}]",
                    novaObra.getTituloPrincipal(), origem);
            return obraRepository.save(novaObra);
        }
    }
}
