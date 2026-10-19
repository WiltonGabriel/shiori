package dev.shiori.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.shiori.domain.enums.NivelBroadcast;
import dev.shiori.domain.enums.StatusDenuncia;
import dev.shiori.domain.enums.StatusSugestao;
import dev.shiori.domain.exception.RecursoNaoEncontradoException;
import dev.shiori.domain.exception.RegraDeNegocioException;
import dev.shiori.domain.model.BroadcastSistema;
import dev.shiori.domain.model.DenunciaComentario;
import dev.shiori.domain.model.DenunciaObra;
import dev.shiori.domain.model.FeatureFlag;
import dev.shiori.domain.model.Genero;
import dev.shiori.domain.model.ItemCatalogo;
import dev.shiori.domain.model.Obra;
import dev.shiori.domain.model.ProvedorIntegracao;
import dev.shiori.domain.model.RegistroAuditoria;
import dev.shiori.domain.model.SugestaoEdicaoObra;
import dev.shiori.domain.model.Tag;
import dev.shiori.domain.model.Usuario;
import dev.shiori.domain.model.VotoTag;
import dev.shiori.dto.ObraResponse;
import dev.shiori.dto.admin.AdminDashboardResponse;
import dev.shiori.dto.admin.AdminUsuarioResponse;
import dev.shiori.dto.admin.AlterarFeatureFlagRequest;
import dev.shiori.dto.admin.AlterarRoleUsuarioRequest;
import dev.shiori.dto.admin.AlterarStatusUsuarioRequest;
import dev.shiori.dto.admin.AplicarStrikeRequest;
import dev.shiori.dto.admin.AvaliarSugestaoRequest;
import dev.shiori.dto.admin.BroadcastResponse;
import dev.shiori.dto.admin.CriarBroadcastRequest;
import dev.shiori.dto.admin.CriarGeneroRequest;
import dev.shiori.dto.admin.DenunciaComentarioResponse;
import dev.shiori.dto.admin.DenunciaObraResponse;
import dev.shiori.dto.admin.FeatureFlagResponse;
import dev.shiori.dto.admin.GeneroResponse;
import dev.shiori.dto.admin.MesclarObrasRequest;
import dev.shiori.dto.admin.MesclarObrasResponse;
import dev.shiori.dto.admin.MesclarTagsRequest;
import dev.shiori.dto.admin.MesclarTagsResponse;
import dev.shiori.dto.admin.OverrideObraRequest;
import dev.shiori.dto.admin.ProvedorIntegracaoResponse;
import dev.shiori.dto.admin.RegistroAuditoriaResponse;
import dev.shiori.dto.admin.SalvarProvedorIntegracaoRequest;
import dev.shiori.dto.admin.SugestaoEdicaoResponse;
import dev.shiori.infra.integration.ClienteIntegracaoExterna;
import dev.shiori.infra.integration.IntegracaoMetadadosObra;
import dev.shiori.infra.integration.ProvedorIntegracaoFactory;
import dev.shiori.repository.AtividadeFeedRepository;
import dev.shiori.repository.BroadcastSistemaRepository;
import dev.shiori.repository.ComentarioRepository;
import dev.shiori.repository.DenunciaComentarioRepository;
import dev.shiori.repository.DenunciaObraRepository;
import dev.shiori.repository.FeatureFlagRepository;
import dev.shiori.repository.GeneroRepository;
import dev.shiori.repository.ItemCatalogoRepository;
import dev.shiori.repository.ObraRepository;
import dev.shiori.repository.ProvedorIntegracaoRepository;
import dev.shiori.repository.RegistroAuditoriaRepository;
import dev.shiori.repository.SugestaoEdicaoObraRepository;
import dev.shiori.repository.TagRepository;
import dev.shiori.repository.UsuarioRepository;
import dev.shiori.repository.VotoTagRepository;

@Service
@Transactional
public class AdminService {

    private static final Logger log = LoggerFactory.getLogger(AdminService.class);

    private final ObraRepository obraRepository;
    private final ItemCatalogoRepository itemCatalogoRepository;
    private final ComentarioRepository comentarioRepository;
    private final SugestaoEdicaoObraRepository sugestaoEdicaoObraRepository;
    private final AtividadeFeedRepository atividadeFeedRepository;
    private final GeneroRepository generoRepository;
    private final DenunciaComentarioRepository denunciaComentarioRepository;
    private final DenunciaObraRepository denunciaObraRepository;
    private final UsuarioRepository usuarioRepository;
    private final TagRepository tagRepository;
    private final VotoTagRepository votoTagRepository;
    private final ProvedorIntegracaoRepository provedorIntegracaoRepository;
    private final FeatureFlagRepository featureFlagRepository;
    private final BroadcastSistemaRepository broadcastSistemaRepository;
    private final RegistroAuditoriaRepository registroAuditoriaRepository;
    private final ProvedorIntegracaoFactory provedorIntegracaoFactory;

    public AdminService(
            ObraRepository obraRepository,
            ItemCatalogoRepository itemCatalogoRepository,
            ComentarioRepository comentarioRepository,
            SugestaoEdicaoObraRepository sugestaoEdicaoObraRepository,
            AtividadeFeedRepository atividadeFeedRepository,
            GeneroRepository generoRepository,
            DenunciaComentarioRepository denunciaComentarioRepository,
            DenunciaObraRepository denunciaObraRepository,
            UsuarioRepository usuarioRepository,
            TagRepository tagRepository,
            VotoTagRepository votoTagRepository,
            ProvedorIntegracaoRepository provedorIntegracaoRepository,
            FeatureFlagRepository featureFlagRepository,
            BroadcastSistemaRepository broadcastSistemaRepository,
            RegistroAuditoriaRepository registroAuditoriaRepository,
            ProvedorIntegracaoFactory provedorIntegracaoFactory) {
        this.obraRepository = obraRepository;
        this.itemCatalogoRepository = itemCatalogoRepository;
        this.comentarioRepository = comentarioRepository;
        this.sugestaoEdicaoObraRepository = sugestaoEdicaoObraRepository;
        this.atividadeFeedRepository = atividadeFeedRepository;
        this.generoRepository = generoRepository;
        this.denunciaComentarioRepository = denunciaComentarioRepository;
        this.denunciaObraRepository = denunciaObraRepository;
        this.usuarioRepository = usuarioRepository;
        this.tagRepository = tagRepository;
        this.votoTagRepository = votoTagRepository;
        this.provedorIntegracaoRepository = provedorIntegracaoRepository;
        this.featureFlagRepository = featureFlagRepository;
        this.broadcastSistemaRepository = broadcastSistemaRepository;
        this.registroAuditoriaRepository = registroAuditoriaRepository;
        this.provedorIntegracaoFactory = provedorIntegracaoFactory;
    }

    // ==========================================
    // 1. DASHBOARD & MÉTRICAS
    // ==========================================

    @Transactional(readOnly = true)
    public AdminDashboardResponse obterDashboard(Long adminId) {
        validarAdmin(obterAdmin(adminId));

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

    // ==========================================
    // 2. GESTÃO DE USUÁRIOS & STRIKES
    // ==========================================

    @Transactional(readOnly = true)
    public Page<AdminUsuarioResponse> listarUsuarios(Pageable pageable, Long adminId) {
        validarAdmin(obterAdmin(adminId));
        return usuarioRepository.findAll(pageable).map(AdminUsuarioResponse::deEntidade);
    }

    public AdminUsuarioResponse alterarStatusUsuario(Long usuarioId, AlterarStatusUsuarioRequest request, Long adminId) {
        Usuario admin = obterAdmin(adminId);
        Usuario alvo = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado: ID " + usuarioId));

        alvo.alterarStatusAtivoPorAdmin(admin, request.ativo(), request.motivo());
        Usuario salvo = usuarioRepository.save(alvo);

        registrarAuditoria(admin, "ALTERAR_STATUS_USUARIO", "Usuario", String.valueOf(usuarioId),
                "Novo status ativo=" + request.ativo() + ", motivo: " + request.motivo());

        return AdminUsuarioResponse.deEntidade(salvo);
    }

    public AdminUsuarioResponse alterarRoleUsuario(Long usuarioId, AlterarRoleUsuarioRequest request, Long adminId) {
        Usuario admin = obterAdmin(adminId);
        Usuario alvo = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado: ID " + usuarioId));

        alvo.alterarRolePorAdmin(admin, request.role());
        Usuario salvo = usuarioRepository.save(alvo);

        registrarAuditoria(admin, "ALTERAR_ROLE_USUARIO", "Usuario", String.valueOf(usuarioId),
                "Nova role=" + request.role());

        return AdminUsuarioResponse.deEntidade(salvo);
    }

    public void banirUsuario(Long usuarioId, String motivo, Long adminId) {
        Usuario admin = obterAdmin(adminId);
        Usuario alvo = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado: ID " + usuarioId));

        alvo.banirPorAdmin(admin, motivo);
        usuarioRepository.save(alvo);

        registrarAuditoria(admin, "BANIR_USUARIO", "Usuario", String.valueOf(usuarioId),
                "Usuário banido. Motivo: " + motivo);
    }

    public AdminUsuarioResponse aplicarStrikeUsuario(Long usuarioId, AplicarStrikeRequest request, Long adminId) {
        Usuario admin = obterAdmin(adminId);
        Usuario alvo = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado: ID " + usuarioId));

        int novoTotalStrikes = alvo.aplicarStrikePorAdmin(admin, request.motivo());
        Usuario salvo = usuarioRepository.save(alvo);

        registrarAuditoria(admin, "APLICAR_STRIKE", "Usuario", String.valueOf(usuarioId),
                "Strike aplicado. Total=" + novoTotalStrikes + ", motivo: " + request.motivo());

        return AdminUsuarioResponse.deEntidade(salvo);
    }

    public AdminUsuarioResponse removerStrikeUsuario(Long usuarioId, String motivo, Long adminId) {
        Usuario admin = obterAdmin(adminId);
        Usuario alvo = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado: ID " + usuarioId));

        alvo.zerarOuRemoverStrikePorAdmin(admin, motivo);
        Usuario salvo = usuarioRepository.save(alvo);

        registrarAuditoria(admin, "REMOVER_STRIKE", "Usuario", String.valueOf(usuarioId),
                "Strike removido. Total restante=" + salvo.getStrikes() + ", motivo: " + motivo);

        return AdminUsuarioResponse.deEntidade(salvo);
    }

    // ==========================================
    // 3. CURADORIA DO CATÁLOGO & OVERRIDE
    // ==========================================

    public MesclarObrasResponse mesclarObras(MesclarObrasRequest request, Long adminId) {
        Usuario admin = obterAdmin(adminId);
        Long obraDuplicadaId = request.obraDuplicadaId();
        Long obraPrincipalId = request.obraPrincipalId();

        if (obraDuplicadaId.equals(obraPrincipalId)) {
            throw new RegraDeNegocioException("A obra duplicada e a obra principal não podem ser a mesma obra");
        }

        Obra duplicada = obraRepository.findById(obraDuplicadaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Obra duplicada não encontrada: ID " + obraDuplicadaId));
        Obra principal = obraRepository.findById(obraPrincipalId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Obra principal não encontrada: ID " + obraPrincipalId));

        principal.incorporarMetadadosDe(duplicada);

        List<ItemCatalogo> itensDuplicados = itemCatalogoRepository.findByObraId(obraDuplicadaId);
        int itensTransferidos = 0;
        int itensIncorporados = 0;

        for (ItemCatalogo itemDuplicado : itensDuplicados) {
            Optional<ItemCatalogo> itemPrincipalExistente = itemCatalogoRepository
                    .findByUsuarioIdAndObraId(itemDuplicado.getUsuario().getId(), obraPrincipalId);

            if (itemPrincipalExistente.isPresent()) {
                itemPrincipalExistente.get().incorporarCiclosDe(itemDuplicado);
                itemCatalogoRepository.delete(itemDuplicado);
                itensIncorporados++;
            } else {
                itemDuplicado.transferirParaObra(principal);
                itemCatalogoRepository.save(itemDuplicado);
                itensTransferidos++;
            }
        }
        itemCatalogoRepository.flush();

        int comentariosTransferidos = comentarioRepository.transferirObra(obraDuplicadaId, obraPrincipalId);
        sugestaoEdicaoObraRepository.transferirObra(obraDuplicadaId, obraPrincipalId);
        atividadeFeedRepository.transferirObra(obraDuplicadaId, obraPrincipalId);

        obraRepository.delete(duplicada);
        Obra obraSalva = obraRepository.save(principal);

        String detalhes = "Mesclada duplicada [%d: '%s'] na principal [%d: '%s']. Itens transferidos: %d, incorporados: %d, comentários: %d"
                .formatted(obraDuplicadaId, duplicada.getTituloPrincipal(), obraPrincipalId, obraSalva.getTituloPrincipal(),
                        itensTransferidos, itensIncorporados, comentariosTransferidos);

        registrarAuditoria(admin, "MESCLAR_OBRAS", "Obra", String.valueOf(obraPrincipalId), detalhes);

        return new MesclarObrasResponse(
                obraSalva.getId(),
                obraSalva.getTituloPrincipal(),
                obraDuplicadaId,
                itensTransferidos,
                itensIncorporados,
                comentariosTransferidos,
                "Obras mescladas com sucesso sem perda de dados."
        );
    }

    public ObraResponse oficializarObra(Long obraId, Long adminId) {
        Usuario admin = obterAdmin(adminId);
        Obra obra = obraRepository.findById(obraId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Obra não encontrada: ID " + obraId));

        obra.oficializarPorAdmin(admin);
        Obra obraSalva = obraRepository.save(obra);

        registrarAuditoria(admin, "OFICIALIZAR_OBRA", "Obra", String.valueOf(obraId),
                "Obra promovida a status OFICIAL: " + obraSalva.getTituloPrincipal());

        return ObraResponse.fromEntity(obraSalva);
    }

    public ObraResponse aplicarOverrideObra(Long obraId, OverrideObraRequest request, Long adminId) {
        Usuario admin = obterAdmin(adminId);
        Obra obra = obraRepository.findById(obraId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Obra não encontrada: ID " + obraId));

        obra.aplicarOverrideAdministrativo(
                admin,
                request.tituloPrincipal(),
                request.tituloOriginal(),
                request.capaUrl(),
                request.sinopse(),
                request.totalCapitulosLancados(),
                request.totalVolumes()
        );

        Obra obraSalva = obraRepository.save(obra);
        registrarAuditoria(admin, "OVERRIDE_OBRA", "Obra", String.valueOf(obraId),
                "Override aplicado e obra protegida contra sync automático");

        return ObraResponse.fromEntity(obraSalva);
    }

    public GeneroResponse criarGenero(CriarGeneroRequest request, Long adminId) {
        Usuario admin = obterAdmin(adminId);
        String nomeNormalizado = request.nome().trim();
        if (generoRepository.existsByNomeIgnoreCase(nomeNormalizado)) {
            throw new RegraDeNegocioException("Gênero com esse nome já cadastrado: " + nomeNormalizado);
        }

        Genero genero = new Genero(nomeNormalizado, request.descricao());
        Genero generoSalvo = generoRepository.save(genero);

        registrarAuditoria(admin, "CRIAR_GENERO", "Genero", String.valueOf(generoSalvo.getId()),
                "Gênero cadastrado: " + generoSalvo.getNome());

        return GeneroResponse.deEntidade(generoSalvo);
    }

    public MesclarTagsResponse mesclarTags(MesclarTagsRequest request, Long adminId) {
        Usuario admin = obterAdmin(adminId);

        Tag tagDestino = tagRepository.findById(request.tagDestinoId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Tag de destino não encontrada: ID " + request.tagDestinoId()));

        Set<Long> origensUnicas = new HashSet<>(request.tagOrigemIds());
        origensUnicas.remove(tagDestino.getId());

        int tagsMescladas = 0;
        int obrasAtualizadas = 0;

        for (Long origemId : origensUnicas) {
            Optional<Tag> tagOrigemOpt = tagRepository.findById(origemId);
            if (tagOrigemOpt.isEmpty()) {
                continue;
            }
            Tag tagOrigem = tagOrigemOpt.get();

            List<Obra> obrasComTag = obraRepository.findByTagsContains(tagOrigem);
            for (Obra obra : obrasComTag) {
                obra.removerTag(tagOrigem);
                obra.adicionarTag(tagDestino);
                obraRepository.save(obra);
                obrasAtualizadas++;
            }

            List<VotoTag> votos = votoTagRepository.findByTagId(tagOrigem.getId());
            for (VotoTag voto : votos) {
                Optional<VotoTag> votoExistente = votoTagRepository
                        .findByUsuarioIdAndTagIdAndObraId(voto.getUsuario().getId(), tagDestino.getId(), voto.getObra().getId());
                if (votoExistente.isPresent()) {
                    votoTagRepository.delete(voto);
                }
            }
            votoTagRepository.flush();
            votoTagRepository.deletarPorTagId(tagOrigem.getId());

            tagRepository.delete(tagOrigem);
            tagsMescladas++;
        }

        String detalhes = "Mescladas %d tags na tag canônica [%d: '%s']. Obras afetadas: %d"
                .formatted(tagsMescladas, tagDestino.getId(), tagDestino.getNome(), obrasAtualizadas);

        registrarAuditoria(admin, "MESCLAR_TAGS", "Tag", String.valueOf(tagDestino.getId()), detalhes);

        return new MesclarTagsResponse(
                tagDestino.getId(),
                tagDestino.getNome(),
                tagsMescladas,
                obrasAtualizadas,
                "Tags mescladas com sucesso na tag oficial."
        );
    }

    // ==========================================
    // 4. INTEGRAÇÕES EXTERNAS DINÂMICAS
    // ==========================================

    @Transactional(readOnly = true)
    public List<ProvedorIntegracaoResponse> listarProvedoresIntegracao(Long adminId) {
        validarAdmin(obterAdmin(adminId));
        return provedorIntegracaoRepository.findAll().stream()
                .map(ProvedorIntegracaoResponse::deEntidade)
                .toList();
    }

    public ProvedorIntegracaoResponse salvarProvedorIntegracao(SalvarProvedorIntegracaoRequest request, Long adminId) {
        Usuario admin = obterAdmin(adminId);
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
        registrarAuditoria(admin, "CONFIGURAR_PROVEDOR_INTEGRACAO", "ProvedorIntegracao", String.valueOf(salvo.getId()),
                "Configuração do provedor " + salvo.getCodigo() + " atualizada");

        return ProvedorIntegracaoResponse.deEntidade(salvo);
    }

    public ProvedorIntegracaoResponse alternarStatusProvedor(Long id, boolean ativo, Long adminId) {
        Usuario admin = obterAdmin(adminId);
        ProvedorIntegracao provedor = provedorIntegracaoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Provedor não encontrado: ID " + id));

        provedor.alternarStatus(ativo);
        ProvedorIntegracao salvo = provedorIntegracaoRepository.save(provedor);

        registrarAuditoria(admin, "STATUS_PROVEDOR_INTEGRACAO", "ProvedorIntegracao", String.valueOf(id),
                "Status do provedor " + salvo.getCodigo() + " alterado para ativo=" + ativo);

        return ProvedorIntegracaoResponse.deEntidade(salvo);
    }

    public IntegracaoMetadadosObra consultarObraExterna(String codigoProvedor, String idExterno, Long adminId) {
        validarAdmin(obterAdmin(adminId));

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

    // ==========================================
    // 5. FEATURE FLAGS GLOBAIS
    // ==========================================

    @Transactional(readOnly = true)
    public List<FeatureFlagResponse> listarFeatureFlags(Long adminId) {
        validarAdmin(obterAdmin(adminId));
        return featureFlagRepository.findAll().stream()
                .map(FeatureFlagResponse::deEntidade)
                .toList();
    }

    public FeatureFlagResponse alternarFeatureFlag(String chave, AlterarFeatureFlagRequest request, Long adminId) {
        Usuario admin = obterAdmin(adminId);
        FeatureFlag flag = featureFlagRepository.findByChaveIgnoreCase(chave)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Feature flag não encontrada: " + chave));

        flag.alternarStatus(request.ativado());
        FeatureFlag salva = featureFlagRepository.save(flag);

        registrarAuditoria(admin, "ALTERAR_FEATURE_FLAG", "FeatureFlag", chave,
                "Feature flag " + chave + " alterada para ativado=" + request.ativado());

        return FeatureFlagResponse.deEntidade(salva);
    }

    @Transactional(readOnly = true)
    public boolean isFeatureFlagAtiva(String chave) {
        return featureFlagRepository.findByChaveIgnoreCase(chave)
                .map(FeatureFlag::isAtivado)
                .orElse(false);
    }

    // ==========================================
    // 6. BROADCAST DE SISTEMA
    // ==========================================

    public BroadcastResponse criarBroadcast(CriarBroadcastRequest request, Long adminId) {
        Usuario admin = obterAdmin(adminId);
        BroadcastSistema broadcast = new BroadcastSistema(
                request.titulo(),
                request.mensagem(),
                request.nivel() != null ? request.nivel() : NivelBroadcast.INFO,
                request.dataExpiracao(),
                admin
        );

        BroadcastSistema salvo = broadcastSistemaRepository.save(broadcast);
        registrarAuditoria(admin, "CRIAR_BROADCAST", "BroadcastSistema", String.valueOf(salvo.getId()),
                "Broadcast criado: " + salvo.getTitulo());

        return BroadcastResponse.deEntidade(salvo);
    }

    @Transactional(readOnly = true)
    public Page<BroadcastResponse> listarBroadcasts(Pageable pageable, Long adminId) {
        validarAdmin(obterAdmin(adminId));
        return broadcastSistemaRepository.findAllByOrderByDataCriacaoDesc(pageable)
                .map(BroadcastResponse::deEntidade);
    }

    @Transactional(readOnly = true)
    public List<BroadcastResponse> listarBroadcastsAtivos() {
        return broadcastSistemaRepository.findByAtivoTrueOrderByDataCriacaoDesc().stream()
                .filter(BroadcastSistema::isVigente)
                .map(BroadcastResponse::deEntidade)
                .toList();
    }

    public void desativarBroadcast(Long id, Long adminId) {
        Usuario admin = obterAdmin(adminId);
        BroadcastSistema broadcast = broadcastSistemaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Broadcast não encontrado: ID " + id));

        broadcast.desativar();
        broadcastSistemaRepository.save(broadcast);

        registrarAuditoria(admin, "DESATIVAR_BROADCAST", "BroadcastSistema", String.valueOf(id),
                "Broadcast desativado: " + broadcast.getTitulo());
    }

    // ==========================================
    // 7. FILAS DE MODERAÇÃO (SUGESTÕES & DENÚNCIAS)
    // ==========================================

    @Transactional(readOnly = true)
    public Page<SugestaoEdicaoResponse> listarSugestoesPendentes(Pageable pageable, Long adminId) {
        validarAdmin(obterAdmin(adminId));
        return sugestaoEdicaoObraRepository.findByStatus(StatusSugestao.PENDENTE, pageable)
                .map(SugestaoEdicaoResponse::deEntidade);
    }

    public SugestaoEdicaoResponse avaliarSugestao(Long sugestaoId, AvaliarSugestaoRequest request, Long adminId) {
        Usuario admin = obterAdmin(adminId);
        SugestaoEdicaoObra sugestao = sugestaoEdicaoObraRepository.findById(sugestaoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Sugestão de edição não encontrada: ID " + sugestaoId));

        if (Boolean.TRUE.equals(request.aprovado())) {
            sugestao.aprovar(admin, request.parecer());
        } else {
            sugestao.rejeitar(admin, request.parecer());
        }

        SugestaoEdicaoObra salva = sugestaoEdicaoObraRepository.save(sugestao);
        registrarAuditoria(admin, "AVALIAR_SUGESTAO", "SugestaoEdicaoObra", String.valueOf(salva.getId()),
                "Avaliação da sugestão: status=" + salva.getStatus() + ", parecer: " + request.parecer());

        return SugestaoEdicaoResponse.deEntidade(salva);
    }

    @Transactional(readOnly = true)
    public Page<DenunciaComentarioResponse> listarDenunciasComentariosPendentes(Pageable pageable, Long adminId) {
        validarAdmin(obterAdmin(adminId));
        return denunciaComentarioRepository.findByStatus(StatusDenuncia.PENDENTE, pageable)
                .map(DenunciaComentarioResponse::deEntidade);
    }

    public DenunciaComentarioResponse resolverDenunciaComentario(Long denunciaId, boolean aceitar, String parecer, Long adminId) {
        Usuario admin = obterAdmin(adminId);
        DenunciaComentario denuncia = denunciaComentarioRepository.findById(denunciaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Denúncia de comentário não encontrada: ID " + denunciaId));

        StatusDenuncia novoStatus = aceitar ? StatusDenuncia.PROCEDENTE : StatusDenuncia.IMPROCEDENTE;
        denuncia.resolverDenuncia(novoStatus, parecer);

        DenunciaComentario salva = denunciaComentarioRepository.save(denuncia);
        registrarAuditoria(admin, "RESOLVER_DENUNCIA_COMENTARIO", "DenunciaComentario", String.valueOf(salva.getId()),
                "Denúncia resolvida como status=" + novoStatus + ", parecer: " + parecer);

        return DenunciaComentarioResponse.deEntidade(salva);
    }

    @Transactional(readOnly = true)
    public Page<DenunciaObraResponse> listarDenunciasObrasPendentes(Pageable pageable, Long adminId) {
        validarAdmin(obterAdmin(adminId));
        return denunciaObraRepository.findByStatus(StatusDenuncia.PENDENTE, pageable)
                .map(DenunciaObraResponse::deEntidade);
    }

    public DenunciaObraResponse resolverDenunciaObra(Long denunciaId, boolean aceitar, String parecer, Long adminId) {
        Usuario admin = obterAdmin(adminId);
        DenunciaObra denuncia = denunciaObraRepository.findById(denunciaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Denúncia de obra não encontrada: ID " + denunciaId));

        StatusDenuncia novoStatus = aceitar ? StatusDenuncia.PROCEDENTE : StatusDenuncia.IMPROCEDENTE;
        denuncia.resolverDenuncia(novoStatus, parecer);

        DenunciaObra salva = denunciaObraRepository.save(denuncia);
        registrarAuditoria(admin, "RESOLVER_DENUNCIA_OBRA", "DenunciaObra", String.valueOf(salva.getId()),
                "Denúncia resolvida como status=" + novoStatus + ", parecer: " + parecer);

        return DenunciaObraResponse.deEntidade(salva);
    }

    // ==========================================
    // 8. AUDITORIA NO BANCO
    // ==========================================

    @Transactional(readOnly = true)
    public Page<RegistroAuditoriaResponse> listarAuditoria(Pageable pageable, Long adminId) {
        validarAdmin(obterAdmin(adminId));
        return registroAuditoriaRepository.findAllByOrderByDataHoraDesc(pageable)
                .map(RegistroAuditoriaResponse::deEntidade);
    }

    private void registrarAuditoria(Usuario admin, String acao, String entidadeAfetada, String entidadeId, String detalhes) {
        RegistroAuditoria registro = new RegistroAuditoria(
                admin.getId(),
                admin.getApelidoOuEmail(),
                acao,
                entidadeAfetada,
                entidadeId,
                detalhes,
                "127.0.0.1"
        );
        registroAuditoriaRepository.save(registro);

        log.warn("AUDITORIA_SEGURANCA: Admin [id={}, apelido={}] executou [{}] sobre [{} id={}]. Detalhes: {}",
                admin.getId(), admin.getApelidoOuEmail(), acao, entidadeAfetada, entidadeId, detalhes);
    }

    private Usuario obterAdmin(Long adminId) {
        if (adminId == null) {
            throw new RegraDeNegocioException("ID do administrador não informado");
        }
        Usuario admin = usuarioRepository.findById(adminId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Administrador não encontrado: ID " + adminId));
        validarAdmin(admin);
        return admin;
    }

    private void validarAdmin(Usuario admin) {
        if (admin == null || !admin.isAdmin()) {
            throw new RegraDeNegocioException("Acesso negado: operação restrita a administradores");
        }
    }
}
