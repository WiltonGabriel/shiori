package dev.shiori.service;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.shiori.domain.enums.StatusLeitura;
import dev.shiori.domain.enums.TipoObra;
import dev.shiori.domain.enums.Visibilidade;
import dev.shiori.domain.exception.RecursoNaoEncontradoException;
import dev.shiori.domain.model.ItemCatalogo;
import dev.shiori.domain.model.Obra;
import dev.shiori.domain.model.Usuario;
import dev.shiori.dto.catalogo.EstanteExportDTO;
import dev.shiori.dto.catalogo.EstanteImportRequest;
import dev.shiori.repository.ItemCatalogoRepository;
import dev.shiori.repository.ObraRepository;
import dev.shiori.repository.UsuarioRepository;

/**
 * Serviço com responsabilidade única (SRP) sobre a portabilidade,
 * exportação e importação de dados da estante pessoal do usuário.
 */
@Service
public class EstantePortabilidadeService {

    private static final Logger log = LoggerFactory.getLogger(EstantePortabilidadeService.class);

    private final ItemCatalogoRepository itemCatalogoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ObraRepository obraRepository;

    public EstantePortabilidadeService(
            ItemCatalogoRepository itemCatalogoRepository,
            UsuarioRepository usuarioRepository,
            ObraRepository obraRepository) {
        this.itemCatalogoRepository = itemCatalogoRepository;
        this.usuarioRepository = usuarioRepository;
        this.obraRepository = obraRepository;
    }

    @Transactional(readOnly = true)
    public EstanteExportDTO exportarEstante(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado"));

        List<ItemCatalogo> itens = itemCatalogoRepository.findByUsuarioId(usuarioId);
        List<EstanteExportDTO.EstanteItemExportDTO> itensExport = itens.stream().map(item -> {
            List<EstanteExportDTO.HistoricoCicloExportDTO> historicos = item.getHistoricosLeitura().stream()
                    .map(h -> new EstanteExportDTO.HistoricoCicloExportDTO(
                            h.getCapituloInicial(),
                            h.getCapituloFinal(),
                            h.getAvaliacaoCiclo(),
                            h.getAnotacoes(),
                            h.getDataLeitura()
                    )).toList();

            return new EstanteExportDTO.EstanteItemExportDTO(
                    item.getTitulo(),
                    item.getCapaUrl(),
                    item.getTipoObra(),
                    item.getProgressoAtual(),
                    item.getVolumeAtual() != null ? item.getVolumeAtual().intValue() : null,
                    item.getTotalCapitulos() != null ? item.getTotalCapitulos().doubleValue() : null,
                    item.getTotalVolumes(),
                    item.getStatusLeitura(),
                    item.getVisibilidade(),
                    item.getAvaliacaoPessoal(),
                    item.getUrlLeitura(),
                    item.isSincronizarMetadados(),
                    item.getObra() != null ? item.getObra().getId() : null,
                    historicos
            );
        }).toList();

        log.info("Exportação da estante realizada para o usuário [id={}, itens={}]", usuarioId, itensExport.size());

        return new EstanteExportDTO(
                "1.0",
                LocalDateTime.now(),
                usuario.getApelidoOuEmail(),
                itensExport.size(),
                itensExport
        );
    }

    @Transactional
    public int importarEstante(Long usuarioId, EstanteImportRequest request) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado"));

        if (request.itens() == null || request.itens().isEmpty()) {
            return 0;
        }

        int importados = 0;
        for (EstanteExportDTO.EstanteItemExportDTO itemDto : request.itens()) {
            if (itemDto.titulo() == null || itemDto.titulo().isBlank()) {
                continue;
            }

            Obra obra = null;
            if (itemDto.obraGlobalId() != null) {
                obra = obraRepository.findById(itemDto.obraGlobalId()).orElse(null);
            }

            Integer totalCaps = itemDto.totalCapitulos() != null
                    ? Integer.valueOf(itemDto.totalCapitulos().intValue())
                    : (obra != null ? obra.getTotalCapitulosLancados() : null);
            Integer totalVols = itemDto.totalVolumes() != null
                    ? itemDto.totalVolumes()
                    : (obra != null ? obra.getTotalVolumes() : null);
            Double capAtual = itemDto.progressoAtual() != null ? itemDto.progressoAtual() : 0.0;
            Double volAtual = itemDto.volumeAtual() != null ? itemDto.volumeAtual().doubleValue() : 0.0;
            Visibilidade visibilidade = itemDto.visibilidade() != null ? itemDto.visibilidade() : Visibilidade.PRIVADO;
            StatusLeitura status = itemDto.statusLeitura() != null ? itemDto.statusLeitura() : StatusLeitura.LENDO;
            boolean sincronizar = itemDto.sincronizarMetadados() != null ? itemDto.sincronizarMetadados() : false;

            ItemCatalogo item;
            if (obra != null) {
                item = new ItemCatalogo(
                        usuario,
                        obra,
                        itemDto.titulo(),
                        itemDto.capaUrl(),
                        itemDto.tipoObra() != null ? itemDto.tipoObra() : obra.getTipoObra(),
                        totalCaps,
                        totalVols,
                        capAtual,
                        volAtual,
                        visibilidade,
                        status,
                        itemDto.urlLeitura(),
                        sincronizar
                );
            } else {
                item = new ItemCatalogo(
                        usuario,
                        itemDto.titulo(),
                        itemDto.capaUrl(),
                        itemDto.tipoObra() != null ? itemDto.tipoObra() : TipoObra.MANGA,
                        totalCaps,
                        totalVols,
                        capAtual,
                        volAtual,
                        visibilidade,
                        status,
                        itemDto.urlLeitura()
                );
            }

            if (itemDto.nota() != null) {
                item.avaliar(itemDto.nota());
            }

            if (itemDto.historicos() != null) {
                for (var h : itemDto.historicos()) {
                    item.restaurarCicloLeitura(h.capituloInicial(), h.capituloFinal(), h.avaliacaoCiclo(), h.anotacoes(), h.dataLeitura());
                }
            }

            itemCatalogoRepository.save(item);
            importados++;
        }

        log.info("Importação de estante concluída para o usuário [id={}, importados={}]", usuarioId, importados);
        return importados;
    }
}
