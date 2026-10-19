package dev.shiori.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import dev.shiori.domain.enums.NivelBroadcast;
import dev.shiori.domain.enums.Role;
import dev.shiori.domain.enums.StatusDenuncia;
import dev.shiori.domain.enums.StatusLeitura;
import dev.shiori.domain.enums.StatusSugestao;
import dev.shiori.domain.enums.TipoObra;
import dev.shiori.domain.enums.Visibilidade;
import dev.shiori.domain.exception.RegraDeNegocioException;
import dev.shiori.domain.model.BroadcastSistema;
import dev.shiori.domain.model.FeatureFlag;
import dev.shiori.domain.model.ItemCatalogo;
import dev.shiori.domain.model.Obra;
import dev.shiori.domain.model.ProvedorIntegracao;
import dev.shiori.domain.model.RegistroAuditoria;
import dev.shiori.domain.model.Tag;
import dev.shiori.domain.model.Usuario;
import dev.shiori.dto.ObraResponse;
import dev.shiori.dto.admin.AdminDashboardResponse;
import dev.shiori.dto.admin.AdminUsuarioResponse;
import dev.shiori.dto.admin.AlterarFeatureFlagRequest;
import dev.shiori.dto.admin.AlterarRoleUsuarioRequest;
import dev.shiori.dto.admin.AlterarStatusUsuarioRequest;
import dev.shiori.dto.admin.AplicarStrikeRequest;
import dev.shiori.dto.admin.BroadcastResponse;
import dev.shiori.dto.admin.CriarBroadcastRequest;
import dev.shiori.dto.admin.FeatureFlagResponse;
import dev.shiori.dto.admin.MesclarObrasRequest;
import dev.shiori.dto.admin.MesclarObrasResponse;
import dev.shiori.dto.admin.MesclarTagsRequest;
import dev.shiori.dto.admin.MesclarTagsResponse;
import dev.shiori.dto.admin.OverrideObraRequest;
import dev.shiori.dto.admin.ProvedorIntegracaoResponse;
import dev.shiori.dto.admin.SalvarProvedorIntegracaoRequest;
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

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes Unitários: AdminService")
class AdminServiceTest {

    @Mock
    private ObraRepository obraRepository;

    @Mock
    private ItemCatalogoRepository itemCatalogoRepository;

    @Mock
    private ComentarioRepository comentarioRepository;

    @Mock
    private SugestaoEdicaoObraRepository sugestaoEdicaoObraRepository;

    @Mock
    private AtividadeFeedRepository atividadeFeedRepository;

    @Mock
    private GeneroRepository generoRepository;

    @Mock
    private DenunciaComentarioRepository denunciaComentarioRepository;

    @Mock
    private DenunciaObraRepository denunciaObraRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private TagRepository tagRepository;

    @Mock
    private VotoTagRepository votoTagRepository;

    @Mock
    private ProvedorIntegracaoRepository provedorIntegracaoRepository;

    @Mock
    private FeatureFlagRepository featureFlagRepository;

    @Mock
    private BroadcastSistemaRepository broadcastSistemaRepository;

    @Mock
    private RegistroAuditoriaRepository registroAuditoriaRepository;

    @Mock
    private ProvedorIntegracaoFactory provedorIntegracaoFactory;

    @InjectMocks
    private AdminService adminService;

    private Usuario admin;
    private Usuario usuarioComum;
    private Obra obraPrincipal;
    private Obra obraDuplicada;

    @BeforeEach
    void setUp() {
        admin = new Usuario("admin@shiori.dev", "hash_senha", "sys_overseer");
        ReflectionTestUtils.setField(admin, "id", 1L);
        admin.promoverPara(Role.ROLE_ADMIN);

        usuarioComum = new Usuario("leitor@shiori.dev", "hash_senha", "leitor_assiduo");
        ReflectionTestUtils.setField(usuarioComum, "id", 2L);

        obraPrincipal = new Obra("Berserk Oficial", "Berserk", "https://capa1.jpg", "Sinopse 1", 364, TipoObra.MANGA);
        ReflectionTestUtils.setField(obraPrincipal, "id", 10L);

        obraDuplicada = new Obra("Berserk (Duplicata)", "Berserk", "https://capa2.jpg", "Sinopse 2", 364, TipoObra.MANGA);
        ReflectionTestUtils.setField(obraDuplicada, "id", 20L);
    }

    @Test
    @DisplayName("Deve retornar métricas consolidadas no dashboard admin")
    void deveRetornarDashboardComMetricasConsolidadas() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(usuarioRepository.count()).thenReturn(50L);
        when(usuarioRepository.countByAtivoTrue()).thenReturn(48L);
        when(obraRepository.count()).thenReturn(120L);
        when(itemCatalogoRepository.count()).thenReturn(350L);
        when(sugestaoEdicaoObraRepository.countByStatus(StatusSugestao.PENDENTE)).thenReturn(4L);
        when(denunciaComentarioRepository.countByStatus(StatusDenuncia.PENDENTE)).thenReturn(2L);
        when(denunciaObraRepository.countByStatus(StatusDenuncia.PENDENTE)).thenReturn(1L);

        AdminDashboardResponse dashboard = adminService.obterDashboard(1L);

        assertThat(dashboard).isNotNull();
        assertThat(dashboard.totalUsuarios()).isEqualTo(50L);
        assertThat(dashboard.totalUsuariosAtivos()).isEqualTo(48L);
        assertThat(dashboard.totalObras()).isEqualTo(120L);
        assertThat(dashboard.totalItensEstante()).isEqualTo(350L);
        assertThat(dashboard.totalSugestoesPendentes()).isEqualTo(4L);
        assertThat(dashboard.totalDenunciasComentariosPendentes()).isEqualTo(2L);
        assertThat(dashboard.totalDenunciasObrasPendentes()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Deve listar usuários de forma paginada")
    void deveListarUsuariosPaginado() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(admin));
        Pageable pageable = PageRequest.of(0, 10);
        when(usuarioRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(admin, usuarioComum)));

        Page<AdminUsuarioResponse> page = adminService.listarUsuarios(pageable, 1L);

        assertThat(page).hasSize(2);
        assertThat(page.getContent().get(0).apelido()).isEqualTo("sys_overseer");
    }

    @Test
    @DisplayName("Deve alterar status de usuário (suspensão/reativação)")
    void deveAlterarStatusUsuario() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(usuarioComum));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(i -> i.getArgument(0));

        AlterarStatusUsuarioRequest request = new AlterarStatusUsuarioRequest(false, "Suspensão temporária preventiva");
        AdminUsuarioResponse response = adminService.alterarStatusUsuario(2L, request, 1L);

        assertThat(response.ativo()).isFalse();
        assertThat(usuarioComum.isAtivo()).isFalse();
        verify(registroAuditoriaRepository).save(any(RegistroAuditoria.class));
    }

    @Test
    @DisplayName("Deve promover usuário alterando sua Role")
    void devePromoverUsuarioAlterandoRole() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(usuarioComum));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(i -> i.getArgument(0));

        AlterarRoleUsuarioRequest request = new AlterarRoleUsuarioRequest(Role.ROLE_CURADOR);
        AdminUsuarioResponse response = adminService.alterarRoleUsuario(2L, request, 1L);

        assertThat(response.role()).isEqualTo(Role.ROLE_CURADOR);
        assertThat(usuarioComum.getRole()).isEqualTo(Role.ROLE_CURADOR);
        assertThat(usuarioComum.isCurador()).isTrue();
        verify(registroAuditoriaRepository).save(any(RegistroAuditoria.class));
    }

    @Test
    @DisplayName("Deve aplicar strike ao usuário e banir automaticamente no terceiro strike")
    void deveAplicarStrikeEBanirNoTerceiro() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(usuarioComum));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(i -> i.getArgument(0));

        AplicarStrikeRequest request = new AplicarStrikeRequest("Violação de regras");
        adminService.aplicarStrikeUsuario(2L, request, 1L);
        assertThat(usuarioComum.getStrikes()).isEqualTo(1);
        assertThat(usuarioComum.isAtivo()).isTrue();

        adminService.aplicarStrikeUsuario(2L, request, 1L);
        assertThat(usuarioComum.getStrikes()).isEqualTo(2);
        assertThat(usuarioComum.isAtivo()).isTrue();

        adminService.aplicarStrikeUsuario(2L, request, 1L);
        assertThat(usuarioComum.getStrikes()).isEqualTo(3);
        assertThat(usuarioComum.isAtivo()).isFalse(); // Banido automaticamente!
    }

    @Test
    @DisplayName("Deve aplicar override administrativo na obra e protegê-la contra sync futuro")
    void deveAplicarOverrideAdministrativo() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(obraRepository.findById(10L)).thenReturn(Optional.of(obraPrincipal));
        when(obraRepository.save(any(Obra.class))).thenAnswer(i -> i.getArgument(0));

        OverrideObraRequest request = new OverrideObraRequest(
                "Berserk (Edição Definitiva Dark)",
                "Berserk Original",
                "https://nova-capa.jpg",
                "Sinopse corrigida manualmente pelo curador",
                370,
                41
        );

        ObraResponse response = adminService.aplicarOverrideObra(10L, request, 1L);

        assertThat(response.tituloPrincipal()).isEqualTo("Berserk (Edição Definitiva Dark)");
        assertThat(obraPrincipal.isProtegidoContraSync()).isTrue();
        assertThat(obraPrincipal.podeSerSincronizada()).isFalse();
        verify(registroAuditoriaRepository).save(any(RegistroAuditoria.class));
    }

    @Test
    @DisplayName("Deve mesclar tags consolidando obras e votos na tag de destino")
    void deveMesclarTagsComSucesso() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(admin));

        Tag tagOrigem1 = new Tag("cyber-punk", 1L);
        ReflectionTestUtils.setField(tagOrigem1, "id", 101L);

        Tag tagDestino = new Tag("cyberpunk", 1L);
        ReflectionTestUtils.setField(tagDestino, "id", 200L);

        when(tagRepository.findById(200L)).thenReturn(Optional.of(tagDestino));
        when(tagRepository.findById(101L)).thenReturn(Optional.of(tagOrigem1));
        obraPrincipal.adicionarTag(tagOrigem1);
        when(obraRepository.findByTagsContains(tagOrigem1)).thenReturn(List.of(obraPrincipal));

        MesclarTagsRequest request = new MesclarTagsRequest(List.of(101L), 200L);
        MesclarTagsResponse response = adminService.mesclarTags(request, 1L);

        assertThat(response.tagsMescladas()).isEqualTo(1);
        assertThat(response.tagDestinoId()).isEqualTo(200L);
        verify(tagRepository).delete(tagOrigem1);
        verify(registroAuditoriaRepository).save(any(RegistroAuditoria.class));
    }

    @Test
    @DisplayName("Deve salvar e atualizar provedor de integração dinâmica")
    void deveSalvarProvedorIntegracao() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(provedorIntegracaoRepository.findByCodigoIgnoreCase("ANILIST")).thenReturn(Optional.empty());
        when(provedorIntegracaoRepository.save(any(ProvedorIntegracao.class))).thenAnswer(i -> {
            ProvedorIntegracao p = i.getArgument(0);
            ReflectionTestUtils.setField(p, "id", 1L);
            return p;
        });

        SalvarProvedorIntegracaoRequest request = new SalvarProvedorIntegracaoRequest(
                "ANILIST", "AniList API v2", "https://graphql.anilist.co", "chave_secreta_teste", 5000L
        );

        ProvedorIntegracaoResponse response = adminService.salvarProvedorIntegracao(request, 1L);

        assertThat(response).isNotNull();
        assertThat(response.codigo()).isEqualTo("ANILIST");
        assertThat(response.limiteDiario()).isEqualTo(5000L);
        verify(registroAuditoriaRepository).save(any(RegistroAuditoria.class));
    }

    @Test
    @DisplayName("Deve alternar feature flag em tempo real")
    void deveAlternarFeatureFlag() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(admin));
        FeatureFlag flag = new FeatureFlag("MODO_MANUTENCAO", "Modo Manutenção", "Bloqueia rotas", false);
        when(featureFlagRepository.findByChaveIgnoreCase("MODO_MANUTENCAO")).thenReturn(Optional.of(flag));
        when(featureFlagRepository.save(any(FeatureFlag.class))).thenAnswer(i -> i.getArgument(0));

        AlterarFeatureFlagRequest request = new AlterarFeatureFlagRequest(true);
        FeatureFlagResponse response = adminService.alternarFeatureFlag("MODO_MANUTENCAO", request, 1L);

        assertThat(response.ativado()).isTrue();
        assertThat(flag.isAtivado()).isTrue();
        verify(registroAuditoriaRepository).save(any(RegistroAuditoria.class));
    }

    @Test
    @DisplayName("Deve criar broadcast de sistema com sucesso")
    void deveCriarBroadcastDeSistema() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(broadcastSistemaRepository.save(any(BroadcastSistema.class))).thenAnswer(i -> {
            BroadcastSistema b = i.getArgument(0);
            ReflectionTestUtils.setField(b, "id", 1L);
            return b;
        });

        CriarBroadcastRequest request = new CriarBroadcastRequest(
                "Manutenção Programada", "Servidor entrará em manutenção às 03:00", NivelBroadcast.AVISO, null
        );

        BroadcastResponse response = adminService.criarBroadcast(request, 1L);

        assertThat(response).isNotNull();
        assertThat(response.titulo()).isEqualTo("Manutenção Programada");
        assertThat(response.nivel()).isEqualTo(NivelBroadcast.AVISO);
        verify(registroAuditoriaRepository).save(any(RegistroAuditoria.class));
    }

    @Test
    @DisplayName("Deve mesclar obras com sucesso consolidando itens e histórico sem perda de dados")
    void deveMesclarObrasComSucesso() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(admin));

        ItemCatalogo itemDuplicado = new ItemCatalogo(usuarioComum, obraDuplicada, Visibilidade.PUBLICO, StatusLeitura.LENDO);
        ReflectionTestUtils.setField(itemDuplicado, "id", 100L);
        itemDuplicado.registrarCicloLeitura(15.0, 9.5, "Capítulo incrível");

        when(obraRepository.findById(20L)).thenReturn(Optional.of(obraDuplicada));
        when(obraRepository.findById(10L)).thenReturn(Optional.of(obraPrincipal));
        when(itemCatalogoRepository.findByObraId(20L)).thenReturn(List.of(itemDuplicado));
        when(itemCatalogoRepository.findByUsuarioIdAndObraId(2L, 10L)).thenReturn(Optional.empty());
        when(comentarioRepository.transferirObra(20L, 10L)).thenReturn(3);
        when(obraRepository.save(any(Obra.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MesclarObrasRequest request = new MesclarObrasRequest(20L, 10L);
        MesclarObrasResponse response = adminService.mesclarObras(request, 1L);

        assertThat(response).isNotNull();
        assertThat(response.obraPrincipalId()).isEqualTo(10L);
        assertThat(response.itensTransferidos()).isEqualTo(1);
        assertThat(response.itensIncorporados()).isEqualTo(0);
        assertThat(response.comentariosTransferidos()).isEqualTo(3);

        verify(itemCatalogoRepository).save(itemDuplicado);
        verify(obraRepository).delete(obraDuplicada);
        verify(comentarioRepository).transferirObra(20L, 10L);
        verify(sugestaoEdicaoObraRepository).transferirObra(20L, 10L);
        verify(atividadeFeedRepository).transferirObra(20L, 10L);
        verify(registroAuditoriaRepository).save(any(RegistroAuditoria.class));
    }

    @Test
    @DisplayName("Deve rejeitar banir outro administrador")
    void deveRejeitarBanirOutroAdmin() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(admin));

        Usuario outroAdmin = new Usuario("outro_admin@shiori.dev", "senha", "co_overseer");
        ReflectionTestUtils.setField(outroAdmin, "id", 4L);
        outroAdmin.promoverPara(Role.ROLE_ADMIN);

        when(usuarioRepository.findById(4L)).thenReturn(Optional.of(outroAdmin));

        assertThatThrownBy(() -> adminService.banirUsuario(4L, "Tentativa de golpe", 1L))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("Não é permitido banir outro administrador");
    }
}
