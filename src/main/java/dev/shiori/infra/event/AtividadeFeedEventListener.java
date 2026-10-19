package dev.shiori.infra.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import dev.shiori.domain.event.AtividadeLeituraEvent;
import dev.shiori.domain.model.AtividadeFeed;
import dev.shiori.domain.model.ItemCatalogo;
import dev.shiori.domain.model.Obra;
import dev.shiori.domain.model.Usuario;
import dev.shiori.repository.AtividadeFeedRepository;
import dev.shiori.repository.ItemCatalogoRepository;
import dev.shiori.repository.ObraRepository;
import dev.shiori.repository.UsuarioRepository;

@Component
public class AtividadeFeedEventListener {

    private static final Logger log = LoggerFactory.getLogger(AtividadeFeedEventListener.class);

    private final AtividadeFeedRepository atividadeFeedRepository;
    private final UsuarioRepository usuarioRepository;
    private final ObraRepository obraRepository;
    private final ItemCatalogoRepository itemCatalogoRepository;

    public AtividadeFeedEventListener(
            AtividadeFeedRepository atividadeFeedRepository,
            UsuarioRepository usuarioRepository,
            ObraRepository obraRepository,
            ItemCatalogoRepository itemCatalogoRepository) {
        this.atividadeFeedRepository = atividadeFeedRepository;
        this.usuarioRepository = usuarioRepository;
        this.obraRepository = obraRepository;
        this.itemCatalogoRepository = itemCatalogoRepository;
    }

    @Async
    @EventListener
    @Transactional
    public void processarEventoFeed(AtividadeLeituraEvent evento) {
        log.info("Processando evento assíncrono de Feed: tipo={}, usuarioId={}, obraId={}",
                evento.tipoAtividade(), evento.usuarioId(), evento.obraId());

        Usuario usuario = usuarioRepository.findById(evento.usuarioId()).orElse(null);
        if (usuario == null) {
            log.warn("Usuário [id={}] não encontrado para registrar atividade no feed", evento.usuarioId());
            return;
        }

        Obra obra = evento.obraId() != null ? obraRepository.findById(evento.obraId()).orElse(null) : null;
        ItemCatalogo item = evento.itemCatalogoId() != null
                ? itemCatalogoRepository.findById(evento.itemCatalogoId()).orElse(null)
                : null;

        String obraTitulo = evento.obraTituloSnapshot() != null ? evento.obraTituloSnapshot()
                : (obra != null ? obra.getTituloPrincipal() : (item != null ? item.getTitulo() : null));
        String obraCapa = evento.obraCapaUrlSnapshot() != null ? evento.obraCapaUrlSnapshot()
                : (obra != null ? obra.getCapaUrl() : (item != null ? item.getCapaUrl() : null));

        AtividadeFeed atividade = new AtividadeFeed(
                usuario,
                evento.tipoAtividade(),
                obra,
                item,
                evento.descricao(),
                obraTitulo,
                obraCapa,
                evento.capituloSnapshot(),
                evento.notaSnapshot()
        );

        atividadeFeedRepository.save(atividade);
        log.info("Atividade de Feed persistida com sucesso [id={}] para usuário [{}]",
                atividade.getId(), usuario.getApelidoOuEmail());
    }
}
