package dev.shiori.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.shiori.domain.enums.StatusCatalogo;
import dev.shiori.domain.exception.RecursoNaoEncontradoException;
import dev.shiori.domain.model.Obra;
import dev.shiori.dto.ObraCadastroRequest;
import dev.shiori.dto.ObraResponse;
import dev.shiori.repository.ObraRepository;

@Service
@Transactional(readOnly = true)
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
                dados.totalCapitulosLancados(),
                dados.tipoObra()
        );

        Obra obraSalva = obraRepository.save(obra);
        return ObraResponse.fromEntity(obraSalva);
    }

    @Transactional
    public ObraResponse submeterAoCatalogoGlobal(Long obraId, Long usuarioId) {
        Obra obra = buscarObraPorId(obraId);
        obra.submeterAoCatalogoGlobal(usuarioId);
        Obra obraSalva = obraRepository.save(obra);
        return ObraResponse.fromEntity(obraSalva);
    }

    @Transactional
    public ObraResponse promoverParaOficial(Long obraId) {
        Obra obra = buscarObraPorId(obraId);
        obra.promoverParaOficial();
        Obra obraSalva = obraRepository.save(obra);
        return ObraResponse.fromEntity(obraSalva);
    }

    public List<ObraResponse> listarTodas(StatusCatalogo statusFiltro) {
        List<Obra> obras;

        if (statusFiltro == null) {
            obras = obraRepository.findAll();
        } else {
            obras = obraRepository.findByStatusCatalogo(statusFiltro);
        }

        return obras.stream()
                .map(ObraResponse::fromEntity)
                .toList();
    }

    public List<ObraResponse> listarObrasDaHome() {
        List<Obra> obras = obraRepository.findByStatusCatalogoIn(
                List.of(StatusCatalogo.OFICIAL, StatusCatalogo.COMUNITARIO)
        );
        return obras.stream()
                .map(ObraResponse::fromEntity)
                .toList();
    }

    public ObraResponse buscarPorId(Long obraId) {
        Obra obra = buscarObraPorId(obraId);
        return ObraResponse.fromEntity(obra);
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
