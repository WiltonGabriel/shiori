package dev.shiori.service.admin;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.shiori.domain.enums.StatusDenuncia;
import dev.shiori.domain.exception.RecursoNaoEncontradoException;
import dev.shiori.domain.model.DenunciaComentario;
import dev.shiori.domain.model.DenunciaObra;
import dev.shiori.domain.model.Usuario;
import dev.shiori.dto.admin.AdminUsuarioResponse;
import dev.shiori.dto.admin.AlterarRoleUsuarioRequest;
import dev.shiori.dto.admin.AlterarStatusUsuarioRequest;
import dev.shiori.dto.admin.AplicarStrikeRequest;
import dev.shiori.dto.admin.DenunciaComentarioResponse;
import dev.shiori.dto.admin.DenunciaObraResponse;
import dev.shiori.repository.DenunciaComentarioRepository;
import dev.shiori.repository.DenunciaObraRepository;
import dev.shiori.repository.UsuarioRepository;

/**
 * Serviço responsável exclusivamente pela Moderação de Comunidade:
 * - Gestão de status de usuários, papéis (roles), aplicação e remoção de strikes, e banimento.
 * - Resolução e auditoria de denúncias de comentários e obras da comunidade.
 * Adere estritamente ao Princípio da Responsabilidade Única (SRP) e Clean Code.
 */
@Service
@Transactional
public class AdminModeracaoService {

    private final AdminAuditoriaService auditoriaService;
    private final UsuarioRepository usuarioRepository;
    private final DenunciaComentarioRepository denunciaComentarioRepository;
    private final DenunciaObraRepository denunciaObraRepository;

    public AdminModeracaoService(AdminAuditoriaService auditoriaService,
                                 UsuarioRepository usuarioRepository,
                                 DenunciaComentarioRepository denunciaComentarioRepository,
                                 DenunciaObraRepository denunciaObraRepository) {
        this.auditoriaService = auditoriaService;
        this.usuarioRepository = usuarioRepository;
        this.denunciaComentarioRepository = denunciaComentarioRepository;
        this.denunciaObraRepository = denunciaObraRepository;
    }

    @Transactional(readOnly = true)
    public Page<AdminUsuarioResponse> listarUsuarios(Pageable pageable, Long adminId) {
        auditoriaService.obterEValidarAdmin(adminId);
        return usuarioRepository.findAll(pageable).map(AdminUsuarioResponse::deEntidade);
    }

    public AdminUsuarioResponse alterarStatusUsuario(Long usuarioId, AlterarStatusUsuarioRequest request, Long adminId) {
        Usuario admin = auditoriaService.obterEValidarAdmin(adminId);
        Usuario alvo = buscarUsuarioPorId(usuarioId);

        alvo.alterarStatusAtivoPorAdmin(admin, request.ativo(), request.motivo());
        Usuario salvo = usuarioRepository.save(alvo);

        auditoriaService.registrarAuditoria(admin, "ALTERAR_STATUS_USUARIO", "Usuario", String.valueOf(usuarioId),
                "Novo status ativo=" + request.ativo() + ", motivo: " + request.motivo());

        return AdminUsuarioResponse.deEntidade(salvo);
    }

    public AdminUsuarioResponse alterarRoleUsuario(Long usuarioId, AlterarRoleUsuarioRequest request, Long adminId) {
        Usuario admin = auditoriaService.obterEValidarAdmin(adminId);
        Usuario alvo = buscarUsuarioPorId(usuarioId);

        alvo.alterarRolePorAdmin(admin, request.role());
        Usuario salvo = usuarioRepository.save(alvo);

        auditoriaService.registrarAuditoria(admin, "ALTERAR_ROLE_USUARIO", "Usuario", String.valueOf(usuarioId),
                "Nova role=" + request.role());

        return AdminUsuarioResponse.deEntidade(salvo);
    }

    public void banirUsuario(Long usuarioId, String motivo, Long adminId) {
        Usuario admin = auditoriaService.obterEValidarAdmin(adminId);
        Usuario alvo = buscarUsuarioPorId(usuarioId);

        alvo.banirPorAdmin(admin, motivo);
        usuarioRepository.save(alvo);

        auditoriaService.registrarAuditoria(admin, "BANIR_USUARIO", "Usuario", String.valueOf(usuarioId),
                "Usuário banido. Motivo: " + motivo);
    }

    public AdminUsuarioResponse aplicarStrikeUsuario(Long usuarioId, AplicarStrikeRequest request, Long adminId) {
        Usuario admin = auditoriaService.obterEValidarAdmin(adminId);
        Usuario alvo = buscarUsuarioPorId(usuarioId);

        int novoTotalStrikes = alvo.aplicarStrikePorAdmin(admin, request.motivo());
        Usuario salvo = usuarioRepository.save(alvo);

        auditoriaService.registrarAuditoria(admin, "APLICAR_STRIKE", "Usuario", String.valueOf(usuarioId),
                "Strike aplicado. Total=" + novoTotalStrikes + ", motivo: " + request.motivo());

        return AdminUsuarioResponse.deEntidade(salvo);
    }

    public AdminUsuarioResponse removerStrikeUsuario(Long usuarioId, String motivo, Long adminId) {
        Usuario admin = auditoriaService.obterEValidarAdmin(adminId);
        Usuario alvo = buscarUsuarioPorId(usuarioId);

        alvo.zerarOuRemoverStrikePorAdmin(admin, motivo);
        Usuario salvo = usuarioRepository.save(alvo);

        auditoriaService.registrarAuditoria(admin, "REMOVER_STRIKE", "Usuario", String.valueOf(usuarioId),
                "Strike removido. Total restante=" + salvo.getStrikes() + ", motivo: " + motivo);

        return AdminUsuarioResponse.deEntidade(salvo);
    }

    @Transactional(readOnly = true)
    public Page<DenunciaComentarioResponse> listarDenunciasComentariosPendentes(Pageable pageable, Long adminId) {
        auditoriaService.obterEValidarAdmin(adminId);
        return denunciaComentarioRepository.findByStatus(StatusDenuncia.PENDENTE, pageable)
                .map(DenunciaComentarioResponse::deEntidade);
    }

    public DenunciaComentarioResponse resolverDenunciaComentario(Long denunciaId, boolean aceitar, String parecer, Long adminId) {
        Usuario admin = auditoriaService.obterEValidarAdmin(adminId);
        DenunciaComentario denuncia = denunciaComentarioRepository.findById(denunciaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Denúncia de comentário não encontrada: ID " + denunciaId));

        StatusDenuncia novoStatus = aceitar ? StatusDenuncia.PROCEDENTE : StatusDenuncia.IMPROCEDENTE;
        denuncia.resolverDenuncia(novoStatus, parecer);

        DenunciaComentario salva = denunciaComentarioRepository.save(denuncia);
        auditoriaService.registrarAuditoria(admin, "RESOLVER_DENUNCIA_COMENTARIO", "DenunciaComentario", String.valueOf(salva.getId()),
                "Denúncia resolvida como status=" + novoStatus + ", parecer: " + parecer);

        return DenunciaComentarioResponse.deEntidade(salva);
    }

    @Transactional(readOnly = true)
    public Page<DenunciaObraResponse> listarDenunciasObrasPendentes(Pageable pageable, Long adminId) {
        auditoriaService.obterEValidarAdmin(adminId);
        return denunciaObraRepository.findByStatus(StatusDenuncia.PENDENTE, pageable)
                .map(DenunciaObraResponse::deEntidade);
    }

    public DenunciaObraResponse resolverDenunciaObra(Long denunciaId, boolean aceitar, String parecer, Long adminId) {
        Usuario admin = auditoriaService.obterEValidarAdmin(adminId);
        DenunciaObra denuncia = denunciaObraRepository.findById(denunciaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Denúncia de obra não encontrada: ID " + denunciaId));

        StatusDenuncia novoStatus = aceitar ? StatusDenuncia.PROCEDENTE : StatusDenuncia.IMPROCEDENTE;
        denuncia.resolverDenuncia(novoStatus, parecer);

        DenunciaObra salva = denunciaObraRepository.save(denuncia);
        auditoriaService.registrarAuditoria(admin, "RESOLVER_DENUNCIA_OBRA", "DenunciaObra", String.valueOf(salva.getId()),
                "Denúncia resolvida como status=" + novoStatus + ", parecer: " + parecer);

        return DenunciaObraResponse.deEntidade(salva);
    }

    private Usuario buscarUsuarioPorId(Long usuarioId) {
        return usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado: ID " + usuarioId));
    }
}
