package dev.shiori.service;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import dev.shiori.domain.enums.Visibilidade;
import dev.shiori.domain.exception.RecursoNaoEncontradoException;
import dev.shiori.domain.exception.RegraDeNegocioException;
import dev.shiori.domain.model.ItemCatalogo;
import dev.shiori.domain.model.Usuario;
import dev.shiori.dto.catalogo.ItemCatalogoResponse;
import dev.shiori.dto.usuario.AlterarEmailRequest;
import dev.shiori.dto.usuario.AlterarSenhaRequest;
import dev.shiori.dto.usuario.AtualizarPerfilRequest;
import dev.shiori.dto.usuario.ConcluirOnboardingRequest;
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
    private final PasswordEncoder passwordEncoder;

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
            StorageService storageService,
            PasswordEncoder passwordEncoder) {
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
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UsuarioPerfilResponse atualizarAvatar(Long usuarioId, MultipartFile file) {
        Usuario usuario = buscarUsuarioPorId(usuarioId);

        String fotoUrl = storageService.uploadFile(file, "avatares");
        usuario.atualizarFotoUrl(fotoUrl);
        usuarioRepository.save(usuario);

        return UsuarioPerfilResponse.aPartirDe(usuario);
    }

    @Transactional(readOnly = true)
    public UsuarioPerfilResponse buscarPerfilPorId(Long usuarioId) {
        Usuario usuario = buscarUsuarioPorId(usuarioId);
        return UsuarioPerfilResponse.aPartirDe(usuario);
    }

    @Transactional
    public UsuarioPerfilResponse concluirOnboarding(Long usuarioId, ConcluirOnboardingRequest request) {
        Usuario usuario = buscarUsuarioPorId(usuarioId);

        String apelidoFormatado = request.apelido().trim().toLowerCase();
        usuarioRepository.findByApelido(apelidoFormatado)
                .filter(u -> !u.getId().equals(usuarioId))
                .ifPresent(u -> {
                    throw new RegraDeNegocioException("O apelido '" + request.apelido() + "' já está em uso por outro usuário.");
                });

        usuario.concluirOnboarding(request.apelido(), request.fusoHorario(), request.fotoUrl(), request.descricao());
        usuarioRepository.save(usuario);

        return UsuarioPerfilResponse.aPartirDe(usuario);
    }

    @Transactional
    public UsuarioPerfilResponse atualizarPerfil(Long usuarioId, AtualizarPerfilRequest request) {
        Usuario usuario = buscarUsuarioPorId(usuarioId);

        if (request.apelido() != null && !request.apelido().isBlank()) {
            String apelidoFormatado = request.apelido().trim().toLowerCase();
            usuarioRepository.findByApelido(apelidoFormatado)
                    .filter(u -> !u.getId().equals(usuarioId))
                    .ifPresent(u -> {
                        throw new RegraDeNegocioException("O apelido '" + request.apelido() + "' já está em uso por outro usuário.");
                    });
        }

        usuario.atualizarPerfil(request.apelido(), request.fotoUrl(), request.descricao(), request.fusoHorario());
        usuarioRepository.save(usuario);

        return UsuarioPerfilResponse.aPartirDe(usuario);
    }

    @Transactional
    public UsuarioPerfilResponse alterarEmail(Long usuarioId, AlterarEmailRequest request) {
        Usuario usuario = buscarUsuarioPorId(usuarioId);

        if (!passwordEncoder.matches(request.senhaAtual(), usuario.getSenha())) {
            throw new RegraDeNegocioException("Senha atual informada está incorreta.");
        }

        String emailFormatado = request.novoEmail().trim().toLowerCase();
        usuarioRepository.findByEmail(emailFormatado)
                .filter(u -> !u.getId().equals(usuarioId))
                .ifPresent(u -> {
                    throw new RegraDeNegocioException("O e-mail '" + request.novoEmail() + "' já está cadastrado.");
                });

        usuario.alterarEmail(request.novoEmail());
        usuarioRepository.save(usuario);

        return UsuarioPerfilResponse.aPartirDe(usuario);
    }

    @Transactional
    public void alterarSenha(Long usuarioId, AlterarSenhaRequest request) {
        Usuario usuario = buscarUsuarioPorId(usuarioId);

        if (!passwordEncoder.matches(request.senhaAtual(), usuario.getSenha())) {
            throw new RegraDeNegocioException("Senha atual informada está incorreta.");
        }

        usuario.atualizarSenhaComHash(request.novaSenha(), passwordEncoder);
        usuarioRepository.save(usuario);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> exportarDados(Long usuarioId) {
        Usuario usuario = buscarUsuarioPorId(usuarioId);
        return usuario.exportarDadosEmJson();
    }

    @Transactional(readOnly = true)
    public UsuarioPerfilPublicoResponse obterPerfilVisivel(String username, Long solicitanteId, Pageable pageable) {
        Usuario alvo = buscarUsuarioPorIdentificador(username);

        boolean ehProprioDono = solicitanteId != null && solicitanteId.equals(alvo.getId());
        boolean saoConexoes = false;

        Collection<Visibilidade> visibilidadesPermitidas;

        if (ehProprioDono) {
            visibilidadesPermitidas = List.of(Visibilidade.PUBLICO, Visibilidade.RESTRITO, Visibilidade.PRIVADO);
        } else {
            if (solicitanteId != null) {
                saoConexoes = conexaoUsuarioRepository.saoConexoes(solicitanteId, alvo.getId());
            }

            if (saoConexoes) {
                visibilidadesPermitidas = List.of(Visibilidade.PUBLICO, Visibilidade.RESTRITO);
            } else {
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
        Usuario usuario = buscarUsuarioPorId(usuarioId);

        log.info("INICIO_PURGA_LGPD: Anonimizando dados do usuário [id={}, email={}]", usuario.getId(), usuario.getEmail());

        obraRepository.desvincularCriador(usuarioId);
        tagRepository.desvincularCriador(usuarioId);
        sugestaoEdicaoObraRepository.desvincularUsuarioSugeridor(usuarioId);
        comentarioRepository.desvincularAutor(usuarioId);

        atividadeFeedRepository.deletarPorUsuarioId(usuarioId);
        historicoLeituraRepository.deletarPorUsuarioId(usuarioId);
        itemCatalogoRepository.deletarPorUsuarioId(usuarioId);
        conexaoUsuarioRepository.deletarTodasConexoesDoUsuario(usuarioId);

        usuario.anonimizarParaLGPD();
        usuarioRepository.save(usuario);

        log.info("CONCLUSAO_PURGA_LGPD: Conta e dados pessoais destruídos com sucesso para o usuário [id={}]", usuarioId);
    }

    private Usuario buscarUsuarioPorId(Long usuarioId) {
        return usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado"));
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
