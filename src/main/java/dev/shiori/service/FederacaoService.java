package dev.shiori.service;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.shiori.domain.enums.StatusCatalogo;
import dev.shiori.domain.enums.TipoObra;
import dev.shiori.domain.exception.RecursoNaoEncontradoException;
import dev.shiori.domain.exception.RegraDeNegocioException;
import dev.shiori.domain.model.NoFederado;
import dev.shiori.domain.model.Obra;
import dev.shiori.dto.federacao.FederacaoLoteObrasDTO;
import dev.shiori.dto.federacao.NoFederadoRequest;
import dev.shiori.dto.federacao.NoFederadoResponse;
import dev.shiori.dto.federacao.ObraFederadaItemDTO;
import dev.shiori.repository.NoFederadoRepository;
import dev.shiori.repository.ObraRepository;

@Service
@Transactional
public class FederacaoService {

    private static final Logger log = LoggerFactory.getLogger(FederacaoService.class);

    private final NoFederadoRepository noFederadoRepository;
    private final ObraRepository obraRepository;

    public FederacaoService(NoFederadoRepository noFederadoRepository, ObraRepository obraRepository) {
        this.noFederadoRepository = noFederadoRepository;
        this.obraRepository = obraRepository;
    }

    public NoFederadoResponse cadastrarNo(NoFederadoRequest request) {
        if (noFederadoRepository.findByUrlHub(request.urlHub()).isPresent()) {
            throw new RegraDeNegocioException("Já existe um nó vizinho cadastrado com esta URL");
        }

        NoFederado no = new NoFederado(request.nome(), request.urlHub(), request.segredoCompartilhado());
        NoFederado salvo = noFederadoRepository.save(no);
        log.info("Nó federado vizinho cadastrado [id={}, nome='{}', url='{}']", salvo.getId(), salvo.getNome(), salvo.getUrlHub());
        return NoFederadoResponse.aPartirDe(salvo);
    }

    @Transactional(readOnly = true)
    public List<NoFederadoResponse> listarNos() {
        return noFederadoRepository.findAll().stream().map(NoFederadoResponse::aPartirDe).toList();
    }

    public NoFederadoResponse alternarStatusNo(Long id, boolean ativo) {
        NoFederado no = noFederadoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Nó federado não encontrado com ID: " + id));
        no.alternarStatus(ativo);
        NoFederado salvo = noFederadoRepository.save(no);
        return NoFederadoResponse.aPartirDe(salvo);
    }

    public void removerNo(Long id) {
        if (!noFederadoRepository.existsById(id)) {
            throw new RecursoNaoEncontradoException("Nó federado não encontrado com ID: " + id);
        }
        noFederadoRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public FederacaoLoteObrasDTO exportarObrasParaFederacao(String segredo) {
        NoFederado no = noFederadoRepository.findBySegredoCompartilhado(segredo)
                .orElseThrow(() -> new RegraDeNegocioException("Segredo de autenticação de federação inválido"));

        if (!no.isAtivo()) {
            throw new RegraDeNegocioException("Nó federado está inativo");
        }

        List<Obra> oficiais = obraRepository.findByStatusCatalogo(StatusCatalogo.OFICIAL);
        List<ObraFederadaItemDTO> itens = oficiais.stream().map(o -> new ObraFederadaItemDTO(
                o.getTituloPrincipal(),
                o.getTituloOriginal(),
                o.getCapaUrl(),
                o.getSinopse(),
                o.getTotalCapitulosLancados(),
                o.getTotalVolumes(),
                o.getTipoObra(),
                o.getMangadexId(),
                o.getAnilistId(),
                o.getKitsuId(),
                o.getIsbn(),
                o.getOrigemFonte()
        )).toList();

        no.registrarSincronizacaoEnviada(itens.size());
        noFederadoRepository.save(no);

        log.info("FEDERAÇÃO: {} obras oficiais exportadas para o nó vizinho '{}'", itens.size(), no.getNome());
        return new FederacaoLoteObrasDTO(no.getNome(), 3, itens);
    }

    public int receberLoteFederado(String segredo, FederacaoLoteObrasDTO lote) {
        NoFederado no = noFederadoRepository.findBySegredoCompartilhado(segredo)
                .orElseThrow(() -> new RegraDeNegocioException("Segredo de autenticação de federação inválido"));

        if (!no.isAtivo()) {
            throw new RegraDeNegocioException("Nó federado está inativo");
        }

        if (lote == null || lote.obras() == null || lote.obras().isEmpty()) {
            return 0;
        }

        if (lote.hopsRestantes() <= 0) {
            log.warn("FEDERAÇÃO: Lote descartado para evitar loop infinito na malha (hops esgotados)");
            return 0;
        }

        int processadas = 0;
        for (ObraFederadaItemDTO item : lote.obras()) {
            if (item.tituloPrincipal() == null || item.tituloPrincipal().isBlank()) {
                continue;
            }

            Optional<Obra> existente = Optional.empty();
            if (item.mangadexId() != null && !item.mangadexId().isBlank()) {
                existente = obraRepository.findByMangadexId(item.mangadexId().trim());
            } else if (item.anilistId() != null && !item.anilistId().isBlank()) {
                existente = obraRepository.findByAnilistId(item.anilistId().trim());
            } else if (item.kitsuId() != null && !item.kitsuId().isBlank()) {
                existente = obraRepository.findByKitsuId(item.kitsuId().trim());
            } else if (item.isbn() != null && !item.isbn().isBlank()) {
                existente = obraRepository.findByIsbn(item.isbn().trim());
            }

            if (existente.isEmpty()) {
                existente = obraRepository.findByTituloPrincipalIgnoreCase(item.tituloPrincipal().trim());
            }

            if (existente.isPresent()) {
                Obra obra = existente.get();
                // Merge inteligente de metadados faltantes
                obra.atualizarSincronizacaoExterna(item.totalCapitulosLancados(), item.totalVolumes());
                obraRepository.save(obra);
            } else {
                Obra nova = Obra.criarDeFederacao(
                        item.tituloPrincipal().trim(),
                        item.tituloOriginal(),
                        item.capaUrl(),
                        item.sinopse(),
                        item.totalCapitulosLancados(),
                        item.tipoObra() != null ? item.tipoObra() : TipoObra.MANGA
                );
                nova.definirIdentificadoresExternos(
                        item.mangadexId(),
                        item.anilistId(),
                        item.kitsuId(),
                        item.isbn(),
                        item.origemFonte() != null ? item.origemFonte() : "FEDERACAO"
                );
                obraRepository.save(nova);
            }
            processadas++;
        }

        no.registrarSincronizacaoRecebida(processadas);
        noFederadoRepository.save(no);

        log.info("FEDERAÇÃO: {} obras sincronizadas com sucesso a partir do nó vizinho '{}'", processadas, no.getNome());
        return processadas;
    }
}
