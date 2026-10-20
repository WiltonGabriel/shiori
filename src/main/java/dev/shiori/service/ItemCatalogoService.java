package dev.shiori.service;

import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.shiori.domain.enums.StatusLeitura;
import dev.shiori.domain.enums.TipoAtividadeFeed;
import dev.shiori.domain.enums.TipoObra;
import dev.shiori.domain.event.AtividadeLeituraEvent;
import dev.shiori.domain.exception.RecursoNaoEncontradoException;
import dev.shiori.domain.exception.RegraDeNegocioException;
import dev.shiori.domain.model.ItemCatalogo;
import dev.shiori.domain.model.Obra;
import dev.shiori.domain.model.Usuario;
import dev.shiori.dto.catalogo.AdicionarItemCatalogoRequest;
import dev.shiori.dto.catalogo.AtualizarVisibilidadeRequest;
import dev.shiori.dto.catalogo.EstanteExportDTO;
import dev.shiori.dto.catalogo.EstanteImportRequest;
import dev.shiori.dto.catalogo.ItemCatalogoResponse;
import dev.shiori.dto.catalogo.RegistrarProgressoRequest;
import dev.shiori.repository.ItemCatalogoRepository;
import dev.shiori.repository.ObraRepository;
import dev.shiori.repository.UsuarioRepository;

@Service
public class ItemCatalogoService {

    private static final Logger log = LoggerFactory.getLogger(ItemCatalogoService.class);

    private final ItemCatalogoRepository itemCatalogoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ObraRepository obraRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final EstantePortabilidadeService estantePortabilidadeService;

    @org.springframework.beans.factory.annotation.Autowired
    public ItemCatalogoService(
            ItemCatalogoRepository itemCatalogoRepository,
            UsuarioRepository usuarioRepository,
            ObraRepository obraRepository,
            ApplicationEventPublisher eventPublisher,
            EstantePortabilidadeService estantePortabilidadeService) {
        this.itemCatalogoRepository = itemCatalogoRepository;
        this.usuarioRepository = usuarioRepository;
        this.obraRepository = obraRepository;
        this.eventPublisher = eventPublisher;
        this.estantePortabilidadeService = estantePortabilidadeService;
    }

    public ItemCatalogoService(
            ItemCatalogoRepository itemCatalogoRepository,
            UsuarioRepository usuarioRepository,
            ObraRepository obraRepository,
            ApplicationEventPublisher eventPublisher) {
        this(itemCatalogoRepository, usuarioRepository, obraRepository, eventPublisher,
                new EstantePortabilidadeService(itemCatalogoRepository, usuarioRepository, obraRepository));
    }

    @Transactional
    public ItemCatalogoResponse adicionarObraEstante(Long usuarioId, AdicionarItemCatalogoRequest request) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado"));

        ItemCatalogo item;
        Long obraId = request.obraId();

        if (obraId != null) {
            Obra obra = obraRepository.findById(obraId)
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Obra não encontrada"));

            if (itemCatalogoRepository.existsByUsuarioIdAndObraId(usuarioId, obraId)) {
                throw new RegraDeNegocioException("Esta obra já está adicionada à sua estante");
            }

            if (usuario.getFormatosPreferidos() != null && !usuario.getFormatosPreferidos().isEmpty()
                    && !usuario.getFormatosPreferidos().contains(obra.getTipoObra())) {
                throw new RegraDeNegocioException(
                        "Não é possível adicionar a obra '" + obra.getTituloPrincipal() + "' à sua estante pois o formato '" +
                        obra.getTipoObra().getDescricao() + "' está desativado em suas preferências de leitura. Habilite-o nas configurações.");
            }

            String titulo = (request.tituloPersonalizado() != null && !request.tituloPersonalizado().isBlank())
                    ? request.tituloPersonalizado().trim()
                    : obra.getTituloPrincipal();

            String capaUrl = (request.capaPersonalizada() != null && !request.capaPersonalizada().isBlank())
                    ? request.capaPersonalizada().trim()
                    : obra.getCapaUrl();

            TipoObra tipo = request.tipoObra() != null ? request.tipoObra() : obra.getTipoObra();
            Double capInicial = request.capituloInicial() != null ? request.capituloInicial() : 0.0;
            Double volInicial = request.volumeInicial() != null ? request.volumeInicial().doubleValue() : 0.0;
            Integer totalCaps = request.totalCapitulosEsperados() != null
                    ? request.totalCapitulosEsperados().intValue()
                    : obra.getTotalCapitulosLancados();
            Integer totalVols = request.totalVolumesEsperados() != null
                    ? request.totalVolumesEsperados()
                    : obra.getTotalVolumes();
            boolean sincronizar = request.sincronizarMetadados() != null ? request.sincronizarMetadados() : true;

            item = new ItemCatalogo(
                    usuario,
                    obra,
                    titulo,
                    capaUrl,
                    tipo,
                    totalCaps,
                    totalVols,
                    capInicial,
                    volInicial,
                    request.visibilidadeOuPadrao(),
                    request.statusLeituraOuPadrao(),
                    request.urlLeitura(),
                    sincronizar
            );
        } else {
            // Obra 100% manual cadastrada diretamente pelo usuário na sua estante soberana
            String titulo = (request.tituloPersonalizado() != null && !request.tituloPersonalizado().isBlank())
                    ? request.tituloPersonalizado().trim()
                    : "Obra Sem Título";
            String capaUrl = (request.capaPersonalizada() != null && !request.capaPersonalizada().isBlank())
                    ? request.capaPersonalizada().trim()
                    : null;
            TipoObra tipo = request.tipoObra() != null ? request.tipoObra() : TipoObra.MANGA;
            Double capInicial = request.capituloInicial() != null ? request.capituloInicial() : 0.0;
            Double volInicial = request.volumeInicial() != null ? request.volumeInicial().doubleValue() : 0.0;
            Integer totalCaps = request.totalCapitulosEsperados() != null
                    ? request.totalCapitulosEsperados().intValue()
                    : null;
            Integer totalVols = request.totalVolumesEsperados() != null
                    ? request.totalVolumesEsperados()
                    : null;

            item = new ItemCatalogo(
                    usuario,
                    titulo,
                    capaUrl,
                    tipo,
                    totalCaps,
                    totalVols,
                    capInicial,
                    volInicial,
                    request.visibilidadeOuPadrao(),
                    request.statusLeituraOuPadrao(),
                    request.urlLeitura()
            );
        }

        ItemCatalogo salvo = itemCatalogoRepository.save(item);
        log.info("Item [id={}] adicionado à estante do usuário [id={}] (obraId={})", salvo.getId(), usuarioId, obraId);

        // Publica evento assíncrono para o feed com snapshots de resiliência
        eventPublisher.publishEvent(new AtividadeLeituraEvent(
                usuario.getId(),
                obraId,
                salvo.getId(),
                TipoAtividadeFeed.ADICIONOU_ESTANTE,
                String.format("%s adicionou %s à sua estante", usuario.getApelidoOuEmail(), salvo.getTitulo()),
                salvo.getTitulo(),
                salvo.getCapaUrl(),
                salvo.getProgressoAtual(),
                salvo.getAvaliacaoPessoal()
        ));

        return ItemCatalogoResponse.aPartirDe(salvo, true);
    }

    @Transactional
    public ItemCatalogoResponse registrarProgresso(Long usuarioId, Long itemId, RegistrarProgressoRequest request) {
        ItemCatalogo item = itemCatalogoRepository.findComDetalhesById(itemId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Item do catálogo não encontrado"));

        validarPropriedadeItem(usuarioId, item);

        // Ciclo elástico de leitura
        if (request.capituloAtual() > item.getProgressoAtual()) {
            item.registrarCicloLeitura(request.capituloAtual(), request.nota(), request.anotacao());
        } else if (request.capituloAtual().equals(item.getProgressoAtual())) {
            var ciclos = item.getHistoricosLeitura();
            if (!ciclos.isEmpty()) {
                var ultimo = ciclos.get(ciclos.size() - 1);
                ultimo.atualizarAvaliacaoEAnotacoes(request.nota(), request.anotacao());
            }
        } else {
            item.atualizarProgresso(request.capituloAtual());
        }

        if (request.volumeAtual() != null) {
            item.atualizarVolumeAtual(request.volumeAtual());
        }

        if (request.nota() != null) {
            item.avaliar(request.nota());
        }

        if (request.statusLeitura() != null) {
            item.alterarStatusLeitura(request.statusLeitura());
        }

        ItemCatalogo salvo = itemCatalogoRepository.save(item);
        Usuario usuario = item.getUsuario();
        Long obraId = item.getObra() != null ? item.getObra().getId() : null;
        String titulo = salvo.getTitulo();
        String capaUrl = salvo.getCapaUrl();
        Double capitulo = request.capituloAtual();
        Double nota = salvo.getAvaliacaoPessoal();

        // Disparo assíncrono de eventos de feed baseado em regras de negócio
        if (salvo.getStatusLeitura() == StatusLeitura.TERMINOU) {
            eventPublisher.publishEvent(new AtividadeLeituraEvent(
                    usuario.getId(),
                    obraId,
                    salvo.getId(),
                    TipoAtividadeFeed.CONCLUIU_LEITURA,
                    String.format("%s concluiu a leitura de %s!", usuario.getApelidoOuEmail(), titulo),
                    titulo,
                    capaUrl,
                    capitulo,
                    nota
            ));
        } else if (request.nota() != null && request.nota() == 10.0) {
            eventPublisher.publishEvent(new AtividadeLeituraEvent(
                    usuario.getId(),
                    obraId,
                    salvo.getId(),
                    TipoAtividadeFeed.AVALIOU_OBRA,
                    String.format(Locale.US, "%s avaliou %s com nota máxima 10/10!", usuario.getApelidoOuEmail(), titulo),
                    titulo,
                    capaUrl,
                    capitulo,
                    nota
            ));
        } else {
            eventPublisher.publishEvent(new AtividadeLeituraEvent(
                    usuario.getId(),
                    obraId,
                    salvo.getId(),
                    TipoAtividadeFeed.ATUALIZOU_PROGRESSO,
                    String.format(Locale.US, "%s alcançou o capítulo %.0f de %s", usuario.getApelidoOuEmail(), request.capituloAtual(), titulo),
                    titulo,
                    capaUrl,
                    capitulo,
                    nota
            ));
        }

        return ItemCatalogoResponse.aPartirDe(salvo, true);
    }

    @Transactional
    public ItemCatalogoResponse alterarVisibilidade(Long usuarioId, Long itemId, AtualizarVisibilidadeRequest request) {
        ItemCatalogo item = itemCatalogoRepository.findById(itemId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Item do catálogo não encontrado"));

        validarPropriedadeItem(usuarioId, item);

        item.alterarVisibilidade(request.visibilidade());
        ItemCatalogo salvo = itemCatalogoRepository.save(item);
        log.info("Visibilidade do item [id={}] alterada para [{}]", itemId, request.visibilidade());

        return ItemCatalogoResponse.aPartirDe(salvo, true);
    }

    @Transactional
    public ItemCatalogoResponse desvincularObra(Long usuarioId, Long itemId) {
        ItemCatalogo item = itemCatalogoRepository.findById(itemId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Item do catálogo não encontrado"));

        validarPropriedadeItem(usuarioId, item);
        item.desvincularObraGlobal();
        ItemCatalogo salvo = itemCatalogoRepository.save(item);
        log.info("Obra desvinculada do item [id={}] pelo usuário [id={}]", itemId, usuarioId);

        return ItemCatalogoResponse.aPartirDe(salvo, true);
    }

    @Transactional
    public ItemCatalogoResponse vincularObra(Long usuarioId, Long itemId, Long obraId, Boolean sincronizar) {
        ItemCatalogo item = itemCatalogoRepository.findById(itemId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Item do catálogo não encontrado"));

        validarPropriedadeItem(usuarioId, item);

        Obra obra = obraRepository.findById(obraId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Obra global não encontrada"));

        item.vincularObraGlobal(obra, sincronizar != null ? sincronizar : true);
        ItemCatalogo salvo = itemCatalogoRepository.save(item);
        log.info("Item [id={}] vinculado à obra [id={}] pelo usuário [id={}]", itemId, obraId, usuarioId);

        return ItemCatalogoResponse.aPartirDe(salvo, true);
    }

    @Transactional
    public ItemCatalogoResponse atualizarMetaNovosCapitulos(Long usuarioId, Long itemId) {
        ItemCatalogo item = itemCatalogoRepository.findById(itemId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Item do catálogo não encontrado"));

        validarPropriedadeItem(usuarioId, item);
        item.atualizarMetaCapitulosGlobal();
        ItemCatalogo salvo = itemCatalogoRepository.save(item);
        log.info("Meta de capítulos atualizada no item [id={}]", itemId);

        return ItemCatalogoResponse.aPartirDe(salvo, true);
    }

    @Transactional(readOnly = true)
    public EstanteExportDTO exportarEstante(Long usuarioId) {
        return estantePortabilidadeService.exportarEstante(usuarioId);
    }

    @Transactional
    public int importarEstante(Long usuarioId, EstanteImportRequest request) {
        return estantePortabilidadeService.importarEstante(usuarioId, request);
    }

    @Transactional(readOnly = true)
    public ItemCatalogoResponse buscarPorId(Long usuarioId, Long itemId) {
        ItemCatalogo item = itemCatalogoRepository.findComDetalhesById(itemId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Item do catálogo não encontrado"));

        boolean ehProprietario = item.getUsuario().getId().equals(usuarioId);
        return ItemCatalogoResponse.aPartirDe(item, ehProprietario);
    }

    @Transactional(readOnly = true)
    public Page<ItemCatalogoResponse> listarMinhaEstante(Long usuarioId, Pageable pageable) {
        Usuario usuario = usuarioRepository.findById(usuarioId).orElse(null);
        if (usuario != null && usuario.getFormatosPreferidos() != null && !usuario.getFormatosPreferidos().isEmpty()) {
            return itemCatalogoRepository.findByUsuarioIdAndObraTipoObraIn(usuarioId, usuario.getFormatosPreferidos(), pageable)
                    .map(item -> ItemCatalogoResponse.aPartirDe(item, true));
        }
        return itemCatalogoRepository.findByUsuarioId(usuarioId, pageable)
                .map(item -> ItemCatalogoResponse.aPartirDe(item, true));
    }

    @Transactional
    public void removerItem(Long usuarioId, Long itemId) {
        ItemCatalogo item = itemCatalogoRepository.findById(itemId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Item do catálogo não encontrado"));

        validarPropriedadeItem(usuarioId, item);
        itemCatalogoRepository.delete(item);
        log.info("Item [id={}] removido da estante do usuário [id={}]", itemId, usuarioId);
    }

    private void validarPropriedadeItem(Long usuarioId, ItemCatalogo item) {
        if (!item.getUsuario().getId().equals(usuarioId)) {
            throw new RegraDeNegocioException("Você não tem permissão para manipular este item da estante");
        }
    }
}
