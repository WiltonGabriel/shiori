package dev.shiori.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

import dev.shiori.domain.model.Obra;
import dev.shiori.domain.enums.StatusLeitura;
import dev.shiori.domain.exception.RecursoNaoEncontradoException;

import dev.shiori.dto.ObraCadastroRequest;
import dev.shiori.dto.NovoCicloRequest;
import dev.shiori.dto.AtualizarLeituraRequest;
import dev.shiori.dto.ObraResponse;

import dev.shiori.repository.ObraRepository;

@Service
public class ObraService {

    private final ObraRepository obraRepository;

    public ObraService(ObraRepository obraRepository) {
        this.obraRepository = obraRepository;
    }

    @Transactional
    public ObraResponse cadastrarObra(ObraCadastroRequest dados) {
        Obra obra = new Obra(
                dados.tituloPrincipal(),
                dados.tituloOriginal(),
                dados.capaUrl(),
                dados.sinopse(),
                dados.notaGeral(),
                dados.statusObra(),
                dados.statusLeitura(),
                dados.avaliacaoPessoal());

        Obra obraSalva = obraRepository.save(obra);
        return ObraResponse.fromEntity(obraSalva);
    }

    @Transactional
    public ObraResponse registrarCiclo(Long obraId, NovoCicloRequest dados) {
        Obra obra = buscarObraPorId(obraId);

        obra.registrarCicloLeitura(
                dados.capituloAtingido(),
                dados.nota(),
                dados.anotacoes());

        Obra obraSalva = obraRepository.save(obra);
        return ObraResponse.fromEntity(obraSalva);
    }

    @Transactional(readOnly = true)
    public List<ObraResponse> listarTodas(StatusLeitura statusFiltro) {
        List<Obra> obras;

        if (statusFiltro == null) {
            obras = obraRepository.findAll();
        } else {
            obras = obraRepository.findByStatusLeitura(statusFiltro);
        }

        return obras.stream()
                .map(ObraResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public ObraResponse buscarPorId(Long obraId) {
        Obra obra = buscarObraPorId(obraId);
        return ObraResponse.fromEntity(obra);
    }

    @Transactional
    public ObraResponse atualizarLeitura(Long obraId, AtualizarLeituraRequest dados) {
        Obra obra = buscarObraPorId(obraId);

        obra.atualizarDadosLeitura(dados.statusLeitura(), dados.avaliacaoPessoal());

        Obra obraSalva = obraRepository.save(obra);
        return ObraResponse.fromEntity(obraSalva);
    }

    @Transactional
    public void excluirObra(Long obraId) {
        if (!obraRepository.existsById(obraId)) {
            throw new RecursoNaoEncontradoException("Obra não encontrada com ID: " + obraId);
        }

        obraRepository.deleteById(obraId);
    }

    private Obra buscarObraPorId(Long id) {
        return obraRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Obra não encontrada com ID: " + id));
    }
}
