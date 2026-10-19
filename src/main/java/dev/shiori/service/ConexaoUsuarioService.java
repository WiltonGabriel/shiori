package dev.shiori.service;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.shiori.domain.enums.StatusConexao;
import dev.shiori.domain.exception.RecursoNaoEncontradoException;
import dev.shiori.domain.exception.RegraDeNegocioException;
import dev.shiori.domain.model.ConexaoUsuario;
import dev.shiori.domain.model.Usuario;
import dev.shiori.dto.conexao.ConexaoResponse;
import dev.shiori.repository.ConexaoUsuarioRepository;
import dev.shiori.repository.UsuarioRepository;

@Service
public class ConexaoUsuarioService {

    private static final Logger log = LoggerFactory.getLogger(ConexaoUsuarioService.class);

    private final ConexaoUsuarioRepository conexaoUsuarioRepository;
    private final UsuarioRepository usuarioRepository;

    public ConexaoUsuarioService(
            ConexaoUsuarioRepository conexaoUsuarioRepository,
            UsuarioRepository usuarioRepository) {
        this.conexaoUsuarioRepository = conexaoUsuarioRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public ConexaoResponse solicitarConexao(Long solicitanteId, Long destinatarioId) {
        if (solicitanteId.equals(destinatarioId)) {
            throw new RegraDeNegocioException("Não é possível solicitar conexão consigo mesmo");
        }

        Optional<ConexaoUsuario> conexaoExistente = conexaoUsuarioRepository.findConexaoEntre(solicitanteId, destinatarioId);
        if (conexaoExistente.isPresent()) {
            ConexaoUsuario existente = conexaoExistente.get();
            if (existente.isAceita()) {
                throw new RegraDeNegocioException("Você já está conectado com este usuário");
            }
            if (existente.isPendente()) {
                throw new RegraDeNegocioException("Já existe uma solicitação de conexão pendente entre estes usuários");
            }
            if (existente.isBloqueada()) {
                throw new RegraDeNegocioException("Não é possível solicitar conexão com este usuário");
            }
        }

        Usuario solicitante = usuarioRepository.findById(solicitanteId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário solicitante não encontrado"));

        Usuario destinatario = usuarioRepository.findById(destinatarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário destinatário não encontrado"));

        ConexaoUsuario novaConexao = new ConexaoUsuario(solicitante, destinatario);
        ConexaoUsuario salva = conexaoUsuarioRepository.save(novaConexao);
        log.info("Solicitação de conexão criada: solicitante=[{}], destinatario=[{}]", solicitanteId, destinatarioId);

        return ConexaoResponse.aPartirDe(salva);
    }

    @Transactional
    public ConexaoResponse aceitarConexao(Long usuarioId, Long conexaoId) {
        ConexaoUsuario conexao = conexaoUsuarioRepository.findById(conexaoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Conexão não encontrada"));

        if (!conexao.getRecebedor().getId().equals(usuarioId)) {
            throw new RegraDeNegocioException("Apenas o destinatário da solicitação pode aceitar a conexão");
        }

        conexao.aceitar();
        ConexaoUsuario salva = conexaoUsuarioRepository.save(conexao);
        log.info("Conexão [id={}] aceita pelo usuário [id={}]", conexaoId, usuarioId);

        return ConexaoResponse.aPartirDe(salva);
    }

    @Transactional
    public ConexaoResponse bloquearConexao(Long usuarioId, Long conexaoId) {
        ConexaoUsuario conexao = conexaoUsuarioRepository.findById(conexaoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Conexão não encontrada"));

        if (!conexao.envolveId(usuarioId)) {
            throw new RegraDeNegocioException("Você não tem permissão para alterar esta conexão");
        }

        conexao.bloquear();
        ConexaoUsuario salva = conexaoUsuarioRepository.save(conexao);
        log.info("Conexão [id={}] bloqueada pelo usuário [id={}]", conexaoId, usuarioId);

        return ConexaoResponse.aPartirDe(salva);
    }

    @Transactional(readOnly = true)
    public Page<ConexaoResponse> listarSolicitacoesPendentes(Long usuarioId, Pageable pageable) {
        return conexaoUsuarioRepository.findByRecebedorIdAndStatus(usuarioId, StatusConexao.PENDENTE, pageable)
                .map(ConexaoResponse::aPartirDe);
    }

    @Transactional(readOnly = true)
    public List<ConexaoResponse> listarConexoesAceitas(Long usuarioId) {
        return conexaoUsuarioRepository.findConexoesAceitas(usuarioId)
                .stream()
                .map(ConexaoResponse::aPartirDe)
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean saoConexoes(Long u1, Long u2) {
        if (u1 == null || u2 == null) {
            return false;
        }
        return conexaoUsuarioRepository.saoConexoes(u1, u2);
    }
}
