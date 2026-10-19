package dev.shiori.service.admin;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.shiori.domain.enums.StatusDenuncia;
import dev.shiori.domain.enums.StatusSugestao;
import dev.shiori.dto.admin.AdminDashboardResponse;
import dev.shiori.repository.DenunciaComentarioRepository;
import dev.shiori.repository.DenunciaObraRepository;
import dev.shiori.repository.ItemCatalogoRepository;
import dev.shiori.repository.ObraRepository;
import dev.shiori.repository.SugestaoEdicaoObraRepository;
import dev.shiori.repository.UsuarioRepository;

/**
 * Serviço responsável exclusivamente pela consolidação e exibição de métricas analíticas do Dashboard Administrativo.
 * Adere estritamente ao Princípio da Responsabilidade Única (SRP) e Clean Code.
 */
@Service
@Transactional(readOnly = true)
public class AdminDashboardService {

    private final AdminAuditoriaService auditoriaService;
    private final UsuarioRepository usuarioRepository;
    private final ObraRepository obraRepository;
    private final ItemCatalogoRepository itemCatalogoRepository;
    private final SugestaoEdicaoObraRepository sugestaoEdicaoObraRepository;
    private final DenunciaComentarioRepository denunciaComentarioRepository;
    private final DenunciaObraRepository denunciaObraRepository;

    public AdminDashboardService(AdminAuditoriaService auditoriaService,
                                 UsuarioRepository usuarioRepository,
                                 ObraRepository obraRepository,
                                 ItemCatalogoRepository itemCatalogoRepository,
                                 SugestaoEdicaoObraRepository sugestaoEdicaoObraRepository,
                                 DenunciaComentarioRepository denunciaComentarioRepository,
                                 DenunciaObraRepository denunciaObraRepository) {
        this.auditoriaService = auditoriaService;
        this.usuarioRepository = usuarioRepository;
        this.obraRepository = obraRepository;
        this.itemCatalogoRepository = itemCatalogoRepository;
        this.sugestaoEdicaoObraRepository = sugestaoEdicaoObraRepository;
        this.denunciaComentarioRepository = denunciaComentarioRepository;
        this.denunciaObraRepository = denunciaObraRepository;
    }

    public AdminDashboardResponse obterDashboard(Long adminId) {
        auditoriaService.obterEValidarAdmin(adminId);

        long totalUsuarios = usuarioRepository.count();
        long totalUsuariosAtivos = usuarioRepository.countByAtivoTrue();
        long totalObras = obraRepository.count();
        long totalItensEstante = itemCatalogoRepository.count();
        long totalSugestoesPendentes = sugestaoEdicaoObraRepository.countByStatus(StatusSugestao.PENDENTE);
        long totalDenunciasComentariosPendentes = denunciaComentarioRepository.countByStatus(StatusDenuncia.PENDENTE);
        long totalDenunciasObrasPendentes = denunciaObraRepository.countByStatus(StatusDenuncia.PENDENTE);

        return new AdminDashboardResponse(
                totalUsuarios,
                totalUsuariosAtivos,
                totalObras,
                totalItensEstante,
                totalSugestoesPendentes,
                totalDenunciasComentariosPendentes,
                totalDenunciasObrasPendentes
        );
    }
}
