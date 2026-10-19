package dev.shiori.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.shiori.domain.enums.StatusCatalogo;
import dev.shiori.domain.enums.StatusPareamento;
import dev.shiori.domain.exception.RecursoNaoEncontradoException;
import dev.shiori.domain.exception.RegraDeNegocioException;
import dev.shiori.domain.model.Obra;
import dev.shiori.domain.model.PareamentoInstancia;
import dev.shiori.dto.ObraResponse;
import dev.shiori.dto.hub.DecisaoPareamentoRequest;
import dev.shiori.dto.hub.PareamentoResponse;
import dev.shiori.dto.hub.SolicitacaoPareamentoRequest;
import dev.shiori.dto.hub.SubmissaoObraAnonimaRequest;
import dev.shiori.repository.ObraRepository;
import dev.shiori.repository.PareamentoInstanciaRepository;

@Service
@Transactional(readOnly = true)
public class PareamentoService {

    private static final Logger log = LoggerFactory.getLogger(PareamentoService.class);

    private final PareamentoInstanciaRepository pareamentoRepository;
    private final ObraRepository obraRepository;

    public PareamentoService(PareamentoInstanciaRepository pareamentoRepository, ObraRepository obraRepository) {
        this.pareamentoRepository = pareamentoRepository;
        this.obraRepository = obraRepository;
    }

    @Transactional
    public PareamentoResponse solicitarPareamento(SolicitacaoPareamentoRequest request) {
        return pareamentoRepository.findByInstanceUuid(request.instanceUuid())
                .map(existente -> {
                    log.info("Handshake recebido para instância existente [uuid={}, apelido={}, status={}]",
                            existente.getInstanceUuid(), existente.getApelidoPublico(), existente.getStatus());
                    return PareamentoResponse.aPartirDe(existente);
                })
                .orElseGet(() -> {
                    PareamentoInstancia nova = new PareamentoInstancia(request.instanceUuid(), request.apelidoPublico());
                    PareamentoInstancia salva = pareamentoRepository.save(nova);
                    log.info("Nova solicitação de pareamento registrada [uuid={}, apelido={}]",
                            salva.getInstanceUuid(), salva.getApelidoPublico());
                    return PareamentoResponse.aPartirDe(salva);
                });
    }

    public PareamentoResponse consultarStatus(String instanceUuid) {
        PareamentoInstancia instancia = pareamentoRepository.findByInstanceUuid(instanceUuid)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Instância não encontrada para o UUID informado"));
        return PareamentoResponse.aPartirDe(instancia);
    }

    @Transactional
    public PareamentoResponse decidirPareamento(Long id, DecisaoPareamentoRequest request) {
        PareamentoInstancia instancia = pareamentoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Solicitação de pareamento não encontrada com ID: " + id));

        switch (request.status()) {
            case AUTORIZADO -> instancia.autorizar();
            case SUSPENSO -> {
                int dias = (request.diasSuspensao() != null && request.diasSuspensao() > 0) ? request.diasSuspensao() : 7;
                instancia.suspender(dias, request.motivo());
            }
            case BANIDO -> instancia.banir(request.motivo());
            default -> throw new RegraDeNegocioException("Status de decisão inválido: " + request.status());
        }

        PareamentoInstancia salva = pareamentoRepository.save(instancia);
        log.info("Decisão de pareamento aplicada: [id={}, uuid={}, status={}]",
                salva.getId(), salva.getInstanceUuid(), salva.getStatus());
        return PareamentoResponse.aPartirDe(salva);
    }

    public Page<PareamentoResponse> listarInstancias(StatusPareamento status, Pageable pageable) {
        if (status != null) {
            return pareamentoRepository.findByStatus(status, pageable).map(PareamentoResponse::aPartirDe);
        }
        return pareamentoRepository.findAll(pageable).map(PareamentoResponse::aPartirDe);
    }

    @Transactional
    public ObraResponse submeterObraAnonima(SubmissaoObraAnonimaRequest request) {
        PareamentoInstancia instancia = pareamentoRepository.findByInstanceUuid(request.instanceUuid())
                .orElseThrow(() -> new RegraDeNegocioException("Instância não pareada no Hub. Solicite pareamento primeiro."));

        if (!instancia.isAtiva()) {
            instancia.registrarTentativaSpam();
            pareamentoRepository.save(instancia);
            throw new RegraDeNegocioException("Instância não possui permissão ativa para submissão (Status: " +
                    instancia.getStatus().getDescricao() + ")");
        }

        Obra obra = new Obra(
                request.tituloPrincipal(),
                request.tituloOriginal(),
                request.capaUrl(),
                request.sinopse(),
                request.totalCapitulosLancados(),
                request.tipoObra()
        );

        obra.submeterAnonimamenteAoCatalogoGlobal();

        Obra obraSalva = obraRepository.save(obra);
        instancia.incrementarSubmissaoValida();
        pareamentoRepository.save(instancia);

        log.info("Obra anônima [id={}, titulo='{}'] submetida via instância [uuid={}, apelido='{}']",
                obraSalva.getId(), obraSalva.getTituloPrincipal(), instancia.getInstanceUuid(), instancia.getApelidoPublico());

        return ObraResponse.fromEntity(obraSalva);
    }
}
