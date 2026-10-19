package dev.shiori.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.shiori.domain.enums.TipoObra;
import dev.shiori.domain.exception.RecursoNaoEncontradoException;
import dev.shiori.domain.exception.RegraDeNegocioException;
import dev.shiori.domain.model.ProvedorIntegracao;
import dev.shiori.infra.integration.ClienteIntegracaoExterna;
import dev.shiori.infra.integration.IntegracaoMetadadosObra;
import dev.shiori.infra.integration.ProvedorIntegracaoFactory;
import dev.shiori.repository.ProvedorIntegracaoRepository;

/**
 * Serviço de orquestração de buscas e consumo de metadados em provedores externos (AniList, MangaDex, Open Library).
 * Garante alta resiliência (fault-tolerance): falhas individuais ou limites de cota não travam a busca global.
 */
@Service
@Transactional
public class IntegracaoCatalogoService {

    private static final Logger log = LoggerFactory.getLogger(IntegracaoCatalogoService.class);

    private final ProvedorIntegracaoRepository provedorIntegracaoRepository;
    private final ProvedorIntegracaoFactory provedorIntegracaoFactory;

    public IntegracaoCatalogoService(ProvedorIntegracaoRepository provedorIntegracaoRepository,
                                     ProvedorIntegracaoFactory provedorIntegracaoFactory) {
        this.provedorIntegracaoRepository = provedorIntegracaoRepository;
        this.provedorIntegracaoFactory = provedorIntegracaoFactory;
    }

    /**
     * Consulta os provedores externos ativos de forma resiliente, agregando sugestões para o cadastro de obras.
     * Caso um provedor falhe ou esteja com limite diário esgotado, a consulta continua com os demais.
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
}
