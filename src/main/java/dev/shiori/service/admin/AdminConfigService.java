package dev.shiori.service.admin;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.shiori.domain.enums.NivelBroadcast;
import dev.shiori.domain.exception.RecursoNaoEncontradoException;
import dev.shiori.domain.model.BroadcastSistema;
import dev.shiori.domain.model.FeatureFlag;
import dev.shiori.domain.model.Usuario;
import dev.shiori.dto.admin.AlterarFeatureFlagRequest;
import dev.shiori.dto.admin.BroadcastResponse;
import dev.shiori.dto.admin.CriarBroadcastRequest;
import dev.shiori.dto.admin.FeatureFlagResponse;
import dev.shiori.repository.BroadcastSistemaRepository;
import dev.shiori.repository.FeatureFlagRepository;

/**
 * Serviço responsável exclusivamente por Configurações Globais e Comunicação do Sistema:
 * - Controle de Feature Flags em tempo de execução (Kill Switches, rollout gradual).
 * - Gestão de avisos globais (Broadcasts de Sistema) para incidentes, manutenções e novidades.
 * Adere estritamente ao Princípio da Responsabilidade Única (SRP) e Clean Code.
 */
@Service
@Transactional
public class AdminConfigService {

    private final AdminAuditoriaService auditoriaService;
    private final FeatureFlagRepository featureFlagRepository;
    private final BroadcastSistemaRepository broadcastSistemaRepository;

    public AdminConfigService(AdminAuditoriaService auditoriaService,
                              FeatureFlagRepository featureFlagRepository,
                              BroadcastSistemaRepository broadcastSistemaRepository) {
        this.auditoriaService = auditoriaService;
        this.featureFlagRepository = featureFlagRepository;
        this.broadcastSistemaRepository = broadcastSistemaRepository;
    }

    // ==========================================
    // FEATURE FLAGS
    // ==========================================

    @Transactional(readOnly = true)
    public List<FeatureFlagResponse> listarFeatureFlags(Long adminId) {
        auditoriaService.obterEValidarAdmin(adminId);
        return featureFlagRepository.findAll().stream()
                .map(FeatureFlagResponse::deEntidade)
                .toList();
    }

    public FeatureFlagResponse alternarFeatureFlag(String chave, AlterarFeatureFlagRequest request, Long adminId) {
        Usuario admin = auditoriaService.obterEValidarAdmin(adminId);
        FeatureFlag flag = featureFlagRepository.findByChaveIgnoreCase(chave)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Feature flag não encontrada: " + chave));

        flag.alternarStatus(request.ativado());
        FeatureFlag salva = featureFlagRepository.save(flag);

        auditoriaService.registrarAuditoria(admin, "ALTERAR_FEATURE_FLAG", "FeatureFlag", chave,
                "Feature flag " + chave + " alterada para ativado=" + request.ativado());

        return FeatureFlagResponse.deEntidade(salva);
    }

    @Transactional(readOnly = true)
    public boolean isFeatureFlagAtiva(String chave) {
        Optional<FeatureFlag> flagOpt = featureFlagRepository.findByChaveIgnoreCase(chave);
        return flagOpt.isPresent() && flagOpt.get().isAtivado();
    }

    // ==========================================
    // BROADCASTS DE SISTEMA
    // ==========================================

    public BroadcastResponse criarBroadcast(CriarBroadcastRequest request, Long adminId) {
        Usuario admin = auditoriaService.obterEValidarAdmin(adminId);
        BroadcastSistema broadcast = new BroadcastSistema(
                request.titulo(),
                request.mensagem(),
                request.nivel() != null ? request.nivel() : NivelBroadcast.INFO,
                request.dataExpiracao(),
                admin
        );

        BroadcastSistema salvo = broadcastSistemaRepository.save(broadcast);
        auditoriaService.registrarAuditoria(admin, "CRIAR_BROADCAST", "BroadcastSistema", String.valueOf(salvo.getId()),
                "Broadcast criado: " + salvo.getTitulo());

        return BroadcastResponse.deEntidade(salvo);
    }

    @Transactional(readOnly = true)
    public Page<BroadcastResponse> listarBroadcasts(Pageable pageable, Long adminId) {
        auditoriaService.obterEValidarAdmin(adminId);
        return broadcastSistemaRepository.findAllByOrderByDataCriacaoDesc(pageable)
                .map(BroadcastResponse::deEntidade);
    }

    @Transactional(readOnly = true)
    public List<BroadcastResponse> listarBroadcastsAtivos() {
        List<BroadcastSistema> broadcasts = broadcastSistemaRepository.findByAtivoTrueOrderByDataCriacaoDesc();
        List<BroadcastResponse> vigentes = new ArrayList<>();
        for (BroadcastSistema broadcast : broadcasts) {
            if (broadcast.isVigente()) {
                vigentes.add(BroadcastResponse.deEntidade(broadcast));
            }
        }
        return vigentes;
    }

    public void desativarBroadcast(Long id, Long adminId) {
        Usuario admin = auditoriaService.obterEValidarAdmin(adminId);
        BroadcastSistema broadcast = broadcastSistemaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Broadcast não encontrado: ID " + id));

        broadcast.desativar();
        broadcastSistemaRepository.save(broadcast);

        auditoriaService.registrarAuditoria(admin, "DESATIVAR_BROADCAST", "BroadcastSistema", String.valueOf(id),
                "Broadcast desativado: " + broadcast.getTitulo());
    }
}
