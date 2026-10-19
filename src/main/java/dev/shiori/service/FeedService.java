package dev.shiori.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.shiori.domain.model.ConexaoUsuario;
import dev.shiori.dto.feed.AtividadeFeedResponse;
import dev.shiori.repository.AtividadeFeedRepository;
import dev.shiori.repository.ConexaoUsuarioRepository;

@Service
public class FeedService {

    private final AtividadeFeedRepository atividadeFeedRepository;
    private final ConexaoUsuarioRepository conexaoUsuarioRepository;

    public FeedService(
            AtividadeFeedRepository atividadeFeedRepository,
            ConexaoUsuarioRepository conexaoUsuarioRepository) {
        this.atividadeFeedRepository = atividadeFeedRepository;
        this.conexaoUsuarioRepository = conexaoUsuarioRepository;
    }

    @Transactional(readOnly = true)
    public Page<AtividadeFeedResponse> obterFeed(Long usuarioId, Pageable pageable) {
        List<ConexaoUsuario> conexoes = conexaoUsuarioRepository.findConexoesAceitas(usuarioId);

        List<Long> autorIds = new ArrayList<>();
        autorIds.add(usuarioId); // Inclui as atividades do próprio usuário logado

        for (ConexaoUsuario conexao : conexoes) {
            Long outroId = conexao.getSolicitante().getId().equals(usuarioId)
                    ? conexao.getRecebedor().getId()
                    : conexao.getSolicitante().getId();
            autorIds.add(outroId);
        }

        return atividadeFeedRepository.findByUsuarioIdInOrderByDataCriacaoDesc(autorIds, pageable)
                .map(AtividadeFeedResponse::aPartirDe);
    }

    @Transactional(readOnly = true)
    public Page<AtividadeFeedResponse> obterAtividadesDeUsuario(Long usuarioId, Pageable pageable) {
        return atividadeFeedRepository.findByUsuarioIdOrderByDataCriacaoDesc(usuarioId, pageable)
                .map(AtividadeFeedResponse::aPartirDe);
    }
}
