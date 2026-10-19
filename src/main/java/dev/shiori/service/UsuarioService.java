package dev.shiori.service;

import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import dev.shiori.domain.enums.Visibilidade;
import dev.shiori.domain.exception.RecursoNaoEncontradoException;
import dev.shiori.domain.model.ItemCatalogo;
import dev.shiori.domain.model.Usuario;
import dev.shiori.dto.catalogo.ItemCatalogoResponse;
import dev.shiori.dto.usuario.UsuarioPerfilPublicoResponse;
import dev.shiori.dto.usuario.UsuarioPerfilResponse;
import dev.shiori.repository.AtividadeFeedRepository;
import dev.shiori.repository.ComentarioRepository;
import dev.shiori.repository.ConexaoUsuarioRepository;
import dev.shiori.repository.HistoricoLeituraRepository;
import dev.shiori.repository.ItemCatalogoRepository;
import dev.shiori.repository.ObraRepository;
import dev.shiori.repository.SugestaoEdicaoObraRepository;
import dev.shiori.repository.TagRepository;
import dev.shiori.repository.UsuarioRepository;

@Service
public class UsuarioService {

    private static final Logger log = LoggerFactory.getLogger(UsuarioService.class);

    private final UsuarioRepository usuarioRepository;
    private final ItemCatalogoRepository itemCatalogoRepository;
    private final ConexaoUsuarioRepository conexaoUsuarioRepository;
    private final ObraRepository obraRepository;
    private final TagRepository tagRepository;
    private final ComentarioRepository comentarioRepository;
    private final SugestaoEdicaoObraRepository sugestaoEdicaoObraRepository;
    private final HistoricoLeituraRepository historicoLeituraRepository;
    private final AtividadeFeedRepository atividadeFeedRepository;
    private final StorageService storageService;

    public UsuarioService(
            UsuarioRepository usuarioRepository,
            ItemCatalogoRepository itemCatalogoRepository,
            ConexaoUsuarioRepository conexaoUsuarioRepository,
            ObraRepository obraRepository,
            TagRepository tagRepository,
            ComentarioRepository comentarioRepository,
            SugestaoEdicaoObraRepository sugestaoEdicaoObraRepository,
            HistoricoLeituraRepository historicoLeituraRepository,
            AtividadeFeedRepository atividadeFeedRepository,
            StorageService storageService) {
        this.usuarioRepository = usuarioRepository;
        this.itemCatalogoRepository = itemCatalogoRepository;
        this.conexaoUsuarioRepository = conexaoUsuarioRepository;
        this.obraRepository = obraRepository;
        this.tagRepository = tagRepository;
        this.comentarioRepository = comentarioRepository;
        this.sugestaoEdicaoObraRepository = sugestaoEdicaoObraRepository;
        this.historicoLeituraRepository = historicoLeituraRepository;
        this.atividadeFeedRepository = atividadeFeedRepository;
        this.storageService = storageService;
    }

    @Transactional
    public UsuarioPerfilResponse atualizarAvatar(Long usuarioId, MultipartFile file) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado"));

        String fotoUrl = storageService.uploadFile(file, "avatares");
        usuario.atualizarFotoUrl(fotoUrl);
        usuarioRepository.save(usuario);

        return UsuarioPerfilResponse.aPartirDe(usuario);
    }

    @Transactional(readOnly = true)
    public UsuarioPerfilResponse buscarPerfilPorId(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado"));
        return UsuarioPerfilResponse.aPartirDe(usuario);
    }

    @Transactional(readOnly = true)
    public UsuarioPerfilPublicoResponse obterPerfilVisivel(String username, Long solicitanteId, Pageable pageable) {
        Usuario alvo = buscarUsuarioPorIdentificador(username);

        boolean ehProprioDono = solicitanteId != null && solicitanteId.equals(alvo.getId());
        boolean saoConexoes = false;

        Collection<Visibilidade> visibilidadesPermitidas;

        if (ehProprioDono) {
            // O próprio usuário enxerga tudo em sua estante
            visibilidadesPermitidas = List.of(Visibilidade.PUBLICO, Visibilidade.RESTRITO, Visibilidade.PRIVADO);
        } else {
            if (solicitanteId != null) {
                saoConexoes = conexaoUsuarioRepository.saoConexoes(solicitanteId, alvo.getId());
            }

            if (saoConexoes) {
                // Amigos com conexão aceita visualizam obras públicas e restritas
                visibilidadesPermitidas = List.of(Visibilidade.PUBLICO, Visibilidade.RESTRITO);
            } else {
                // Visitantes externos enxergam apenas obras públicas
                visibilidadesPermitidas = List.of(Visibilidade.PUBLICO);
            }
        }

        Page<ItemCatalogo> itensPage = itemCatalogoRepository
                .findByUsuarioIdAndVisibilidadeIn(alvo.getId(), visibilidadesPermitidas, pageable);

        Page<ItemCatalogoResponse> responses = itensPage
                .map(item -> ItemCatalogoResponse.aPartirDe(item, ehProprioDono));

        return UsuarioPerfilPublicoResponse.of(alvo, saoConexoes, responses);
    }

    @Transactional
    public void anonimizarContaLGPD(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado"));

        log.info("INICIO_PURGA_LGPD: Anonimizando dados do usuário [id={}, email={}]", usuario.getId(), usuario.getEmail());

        // 1. Preservar integridade do catálogo público: desvincular autoria de Obras e Tags criadas
        obraRepository.desvincularCriador(usuarioId);
        tagRepository.desvincularCriador(usuarioId);

        // 2. Desvincular sugestões de edição comunitárias
        sugestaoEdicaoObraRepository.desvincularUsuarioSugeridor(usuarioId);

        // 3. Desvincular comentários públicos e desativá-los
        comentarioRepository.desvincularAutor(usuarioId);

        // 4. Destruir dados pessoais sociais: atividades do feed, históricos de leitura, estante e conexões
        atividadeFeedRepository.deletarPorUsuarioId(usuarioId);
        historicoLeituraRepository.deletarPorUsuarioId(usuarioId);
        itemCatalogoRepository.deletarPorUsuarioId(usuarioId);
        conexaoUsuarioRepository.deletarTodasConexoesDoUsuario(usuarioId);

        // 5. Anonimizar o registro do usuário e expurgar dados pessoais em repouso
        usuario.anonimizarParaLGPD();
        usuarioRepository.save(usuario);

        log.info("CONCLUSAO_PURGA_LGPD: Conta e dados pessoais destruídos com sucesso para o usuário [id={}]", usuarioId);
    }

    private Usuario buscarUsuarioPorIdentificador(String username) {
        if (username == null || username.isBlank()) {
            throw new RecursoNaoEncontradoException("Identificador de usuário inválido");
        }

        return usuarioRepository.findByApelido(username.trim().toLowerCase())
                .or(() -> {
                    try {
                        Long id = Long.parseLong(username.trim());
                        return usuarioRepository.findById(id);
                    } catch (NumberFormatException e) {
                        return usuarioRepository.findByEmail(username.trim().toLowerCase());
                    }
                })
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado: " + username));
    }
}
