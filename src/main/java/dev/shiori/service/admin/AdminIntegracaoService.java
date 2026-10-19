package dev.shiori.service.admin;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.shiori.domain.exception.RecursoNaoEncontradoException;
import dev.shiori.domain.exception.RegraDeNegocioException;
import dev.shiori.domain.model.ProvedorIntegracao;
import dev.shiori.domain.model.Usuario;
import dev.shiori.dto.admin.ProvedorIntegracaoResponse;
import dev.shiori.dto.admin.SalvarProvedorIntegracaoRequest;
import dev.shiori.infra.integration.ClienteIntegracaoExterna;
import dev.shiori.infra.integration.IntegracaoMetadadosObra;
import dev.shiori.infra.integration.ProvedorIntegracaoFactory;
import dev.shiori.repository.ProvedorIntegracaoRepository;

/**
 * Serviço responsável exclusivamente pela Gestão de Provedores de Integração Externa:
 * - Configuração dinâmica de APIs externas (MangaDex, AniList, etc.) via banco de dados com chaves cifradas AES-256.
 * - Controle de cotas e rate limiting diário por provedor.
 * - Despacho de consultas remotas de metadados literários.
 * Adere estritamente ao Princípio da Responsabilidade Única (SRP) e Clean Code.
 */
@Service
@Transactional
public class AdminIntegracaoService {

    private final AdminAuditoriaService auditoriaService;
    private final ProvedorIntegracaoRepository provedorIntegracaoRepository;
    private final ProvedorIntegracaoFactory provedorIntegracaoFactory;

    public AdminIntegracaoService(AdminAuditoriaService auditoriaService,
                                  ProvedorIntegracaoRepository provedorIntegracaoRepository,
                                  ProvedorIntegracaoFactory provedorIntegracaoFactory) {
        this.auditoriaService = auditoriaService;
        this.provedorIntegracaoRepository = provedorIntegracaoRepository;
        this.provedorIntegracaoFactory = provedorIntegracaoFactory;
    }

    @Transactional(readOnly = true)
    public List<ProvedorIntegracaoResponse> listarProvedoresIntegracao(Long adminId) {
        auditoriaService.obterEValidarAdmin(adminId);
        return provedorIntegracaoRepository.findAll().stream()
                .map(ProvedorIntegracaoResponse::deEntidade)
                .toList();
    }

    public ProvedorIntegracaoResponse salvarProvedorIntegracao(SalvarProvedorIntegracaoRequest request, Long adminId) {
        Usuario admin = auditoriaService.obterEValidarAdmin(adminId);
        String codigoNormalizado = request.codigo().trim().toUpperCase();

        Optional<ProvedorIntegracao> existenteOpt = provedorIntegracaoRepository.findByCodigoIgnoreCase(codigoNormalizado);
        ProvedorIntegracao provedor;

        if (existenteOpt.isPresent()) {
            provedor = existenteOpt.get();
            provedor.atualizarConfiguracao(request.nomeExibicao(), request.urlBase(), request.apiKey(), request.limiteDiario());
        } else {
            provedor = new ProvedorIntegracao(codigoNormalizado, request.nomeExibicao(), request.urlBase(), request.apiKey(),
                    request.limiteDiario() != null ? request.limiteDiario() : 10000L);
        }

        ProvedorIntegracao salvo = provedorIntegracaoRepository.save(provedor);
        auditoriaService.registrarAuditoria(admin, "CONFIGURAR_PROVEDOR_INTEGRACAO", "ProvedorIntegracao", String.valueOf(salvo.getId()),
                "Configuração do provedor " + salvo.getCodigo() + " atualizada");

        return ProvedorIntegracaoResponse.deEntidade(salvo);
    }

    public ProvedorIntegracaoResponse alternarStatusProvedor(Long id, boolean ativo, Long adminId) {
        Usuario admin = auditoriaService.obterEValidarAdmin(adminId);
        ProvedorIntegracao provedor = provedorIntegracaoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Provedor não encontrado: ID " + id));

        provedor.alternarStatus(ativo);
        ProvedorIntegracao salvo = provedorIntegracaoRepository.save(provedor);

        auditoriaService.registrarAuditoria(admin, "STATUS_PROVEDOR_INTEGRACAO", "ProvedorIntegracao", String.valueOf(id),
                "Status do provedor " + salvo.getCodigo() + " alterado para ativo=" + ativo);

        return ProvedorIntegracaoResponse.deEntidade(salvo);
    }

    public IntegracaoMetadadosObra consultarObraExterna(String codigoProvedor, String idExterno, Long adminId) {
        auditoriaService.obterEValidarAdmin(adminId);

        ProvedorIntegracao provedor = provedorIntegracaoRepository.findByCodigoIgnoreCase(codigoProvedor)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Provedor de integração não configurado: " + codigoProvedor));

        if (!provedor.isAtivo()) {
            throw new RegraDeNegocioException("O provedor de integração '" + codigoProvedor + "' está temporariamente inativo");
        }
        if (provedor.atingiuLimiteDiario()) {
            throw new RegraDeNegocioException("Limite diário de requisições excedido para o provedor " + codigoProvedor);
        }

        ClienteIntegracaoExterna cliente = provedorIntegracaoFactory.obterCliente(codigoProvedor);
        IntegracaoMetadadosObra metadados = cliente.buscarObraPorIdExterno(provedor, idExterno);
        provedorIntegracaoRepository.save(provedor);

        return metadados;
    }
}
