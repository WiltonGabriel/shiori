package dev.shiori.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import dev.shiori.domain.enums.StatusCatalogo;
import dev.shiori.domain.enums.StatusLeitura;
import dev.shiori.domain.enums.StatusPareamento;
import dev.shiori.domain.enums.TipoObra;
import dev.shiori.domain.enums.Visibilidade;
import dev.shiori.domain.model.ItemCatalogo;
import dev.shiori.domain.model.NoFederado;
import dev.shiori.domain.model.Obra;
import dev.shiori.domain.model.PareamentoInstancia;
import dev.shiori.domain.model.Usuario;
import dev.shiori.dto.catalogo.AdicionarItemCatalogoRequest;
import dev.shiori.dto.catalogo.EstanteExportDTO;
import dev.shiori.dto.catalogo.EstanteImportRequest;
import dev.shiori.dto.catalogo.ItemCatalogoResponse;
import dev.shiori.dto.federacao.FederacaoLoteObrasDTO;
import dev.shiori.dto.federacao.ObraFederadaItemDTO;
import dev.shiori.dto.hub.DecisaoPareamentoRequest;
import dev.shiori.dto.hub.PareamentoResponse;
import dev.shiori.dto.hub.SolicitacaoPareamentoRequest;
import dev.shiori.dto.hub.SubmissaoObraAnonimaRequest;
import dev.shiori.repository.AtividadeFeedRepository;
import dev.shiori.repository.ItemCatalogoRepository;
import dev.shiori.repository.NoFederadoRepository;
import dev.shiori.repository.ObraRepository;
import dev.shiori.repository.PareamentoInstanciaRepository;
import dev.shiori.repository.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes Unitários: Soberania da Estante, Cache Defensivo, Pareamento e Federação")
class SoberaniaEstanteEFederacaoTest {

    @Mock
    private ItemCatalogoRepository itemCatalogoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private ObraRepository obraRepository;

    @Mock
    private AtividadeFeedRepository atividadeFeedRepository;

    @Mock
    private PareamentoInstanciaRepository pareamentoRepository;

    @Mock
    private NoFederadoRepository noFederadoRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private ItemCatalogoService itemCatalogoService;
    private ObraService obraService;
    private PareamentoService pareamentoService;
    private FederacaoService federacaoService;

    private Usuario usuario;
    private Obra obraGlobal;

    @BeforeEach
    void setUp() {
        itemCatalogoService = new ItemCatalogoService(itemCatalogoRepository, usuarioRepository, obraRepository, eventPublisher);
        obraService = new ObraService(obraRepository, usuarioRepository, itemCatalogoRepository, atividadeFeedRepository);
        pareamentoService = new PareamentoService(pareamentoRepository, obraRepository);
        federacaoService = new FederacaoService(noFederadoRepository, obraRepository);

        usuario = new Usuario("leitor@shiori.dev", "hash_senha", "leitor_soberano");
        ReflectionTestUtils.setField(usuario, "id", 1L);

        obraGlobal = new Obra("Berserk", "ベルセルク", "https://img.shiori.dev/berserk.jpg", "Sinopse épica", 375, TipoObra.MANGA);
        ReflectionTestUtils.setField(obraGlobal, "id", 100L);
    }

    @Test
    @DisplayName("Objetivo 1: Adicionar obra à estante cria snapshot desacoplado e imune a remoção")
    void deveCriarSnapshotProprioAoAdicionarObra() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(obraRepository.findById(100L)).thenReturn(Optional.of(obraGlobal));
        when(itemCatalogoRepository.existsByUsuarioIdAndObraId(1L, 100L)).thenReturn(false);
        when(itemCatalogoRepository.save(any(ItemCatalogo.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AdicionarItemCatalogoRequest request = new AdicionarItemCatalogoRequest(
                100L,
                "Berserk (Minha Edição)",
                "https://img.custom/berserk.jpg",
                TipoObra.MANGA,
                10.0,
                1.0,
                375,
                41,
                Visibilidade.PUBLICO,
                StatusLeitura.LENDO,
                "https://leitura-privada.onion/berserk",
                true
        );

        ItemCatalogoResponse response = itemCatalogoService.adicionarObraEstante(1L, request);

        assertThat(response).isNotNull();
        assertThat(response.titulo()).isEqualTo("Berserk (Minha Edição)");
        assertThat(response.capaUrl()).isEqualTo("https://img.custom/berserk.jpg");
        assertThat(response.totalCapitulos()).isEqualTo(375);
        assertThat(response.totalVolumes()).isEqualTo(41);
        assertThat(response.sincronizarMetadados()).isTrue();
    }

    @Test
    @DisplayName("Objetivo 1 & 6: Exclusão de Obra Global desvincula estante sem cascade delete")
    void deveDesvincularEstanteAoExcluirObraGlobal() {
        when(obraRepository.existsById(100L)).thenReturn(true);

        obraService.excluirObra(100L);

        verify(itemCatalogoRepository).desvincularObra(100L);
        verify(atividadeFeedRepository).desvincularObra(100L);
        verify(obraRepository).deleteById(100L);
    }

    @Test
    @DisplayName("Objetivo 1 & 2: Desvincular e Vincular obra manualmente pelo leitor")
    void devePermitirDesvincularEVincularObra() {
        ItemCatalogo item = new ItemCatalogo(
                usuario, obraGlobal, "Berserk", obraGlobal.getCapaUrl(), TipoObra.MANGA,
                375, 41, 50.0, 5.0, Visibilidade.RESTRITO, StatusLeitura.LENDO, "https://link.privado", true
        );
        ReflectionTestUtils.setField(item, "id", 10L);

        when(itemCatalogoRepository.findById(10L)).thenReturn(Optional.of(item));
        when(itemCatalogoRepository.save(any(ItemCatalogo.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Desvincular
        ItemCatalogoResponse desvinculado = itemCatalogoService.desvincularObra(1L, 10L);
        assertThat(desvinculado.obraId()).isNull();
        assertThat(desvinculado.desvinculado()).isTrue();
        assertThat(desvinculado.titulo()).isEqualTo("Berserk");

        // Vincular novamente
        when(obraRepository.findById(100L)).thenReturn(Optional.of(obraGlobal));
        ItemCatalogoResponse revinculado = itemCatalogoService.vincularObra(1L, 10L, 100L, true);
        assertThat(revinculado.obraId()).isEqualTo(100L);
        assertThat(revinculado.desvinculado()).isFalse();
    }

    @Test
    @DisplayName("Objetivo 1 & 2: Notificação sutil de novos capítulos e atualização da meta")
    void deveDetectarNovosCapitulosEAtualizarMeta() {
        ItemCatalogo item = new ItemCatalogo(
                usuario, obraGlobal, "Berserk", obraGlobal.getCapaUrl(), TipoObra.MANGA,
                360, 41, 50.0, 5.0, Visibilidade.RESTRITO, StatusLeitura.LENDO, null, true
        );
        ReflectionTestUtils.setField(item, "id", 10L);

        // Obra global tem 375 caps, item tem 360 no snapshot
        assertThat(item.temNovosCapitulosDisponiveis()).isTrue();

        when(itemCatalogoRepository.findById(10L)).thenReturn(Optional.of(item));
        when(itemCatalogoRepository.save(any(ItemCatalogo.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ItemCatalogoResponse atualizado = itemCatalogoService.atualizarMetaNovosCapitulos(1L, 10L);
        assertThat(atualizado.totalCapitulos()).isEqualTo(375);
        assertThat(atualizado.temNovosCapitulos()).isFalse();
    }

    @Test
    @DisplayName("Objetivo 1: Exportação e importação da estante pessoal com portabilidade completa")
    void deveExportarEImportarEstanteCompleta() {
        ItemCatalogo item = new ItemCatalogo(
                usuario, obraGlobal, "Berserk", obraGlobal.getCapaUrl(), TipoObra.MANGA,
                375, 41, 0.0, 0.0, Visibilidade.PRIVADO, StatusLeitura.ABDICOU, "https://link.privado", true
        );
        item.avaliar(9.5);
        item.registrarCicloLeitura(100.0, 9.5, "Anotação profunda");

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(itemCatalogoRepository.findByUsuarioId(1L)).thenReturn(List.of(item));

        EstanteExportDTO exportacao = itemCatalogoService.exportarEstante(1L);

        assertThat(exportacao.totalItens()).isEqualTo(1);
        assertThat(exportacao.itens().get(0).titulo()).isEqualTo("Berserk");
        assertThat(exportacao.itens().get(0).statusLeitura()).isEqualTo(StatusLeitura.ABDICOU);
        assertThat(exportacao.itens().get(0).historicos()).hasSize(1);
        assertThat(exportacao.itens().get(0).historicos().get(0).anotacoes()).isEqualTo("Anotação profunda");

        // Importação
        EstanteImportRequest importRequest = new EstanteImportRequest(exportacao.itens(), false);
        when(obraRepository.findById(100L)).thenReturn(Optional.of(obraGlobal));

        int importados = itemCatalogoService.importarEstante(1L, importRequest);
        assertThat(importados).isEqualTo(1);
        verify(itemCatalogoRepository).save(any(ItemCatalogo.class));
    }

    @Test
    @DisplayName("Objetivo 3: Handshake de pareamento cliente <-> hub e moderação de instâncias")
    void deveGerenciarPareamentoEHandshake() {
        when(pareamentoRepository.findByInstanceUuid("inst-uuid-123")).thenReturn(Optional.empty());
        when(pareamentoRepository.save(any(PareamentoInstancia.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SolicitacaoPareamentoRequest solicitacao = new SolicitacaoPareamentoRequest("inst-uuid-123", "Cliente Web Alpha");
        PareamentoResponse response = pareamentoService.solicitarPareamento(solicitacao);

        assertThat(response.status()).isEqualTo(StatusPareamento.PENDENTE);
        assertThat(response.instanceUuid()).isEqualTo("inst-uuid-123");

        // Decisão do Admin: Autorizar
        PareamentoInstancia instancia = new PareamentoInstancia("inst-uuid-123", "Cliente Web Alpha");
        ReflectionTestUtils.setField(instancia, "id", 1L);
        when(pareamentoRepository.findById(1L)).thenReturn(Optional.of(instancia));

        DecisaoPareamentoRequest decisao = new DecisaoPareamentoRequest(StatusPareamento.AUTORIZADO, "Aprovado", null);
        PareamentoResponse autorizada = pareamentoService.decidirPareamento(1L, decisao);

        assertThat(autorizada.status()).isEqualTo(StatusPareamento.AUTORIZADO);
        assertThat(autorizada.tokenAcesso()).startsWith("shiori_inst_");
    }

    @Test
    @DisplayName("Objetivo 3: Submissão anônima de obra literária via instância pareada")
    void devePermitirSubmissaoAnonimaDeObra() {
        PareamentoInstancia instancia = new PareamentoInstancia("inst-uuid-123", "Cliente Web Alpha");
        instancia.autorizar();

        when(pareamentoRepository.findByInstanceUuid("inst-uuid-123")).thenReturn(Optional.of(instancia));
        when(obraRepository.save(any(Obra.class))).thenAnswer(invocation -> {
            Obra o = invocation.getArgument(0);
            ReflectionTestUtils.setField(o, "id", 999L);
            return o;
        });

        SubmissaoObraAnonimaRequest request = new SubmissaoObraAnonimaRequest(
                "inst-uuid-123",
                "Cliente Web Alpha",
                "Obra Desconhecida",
                "Unknown Work",
                "https://img.shiori/unknown.jpg",
                "Sinopse misteriosa",
                12,
                1,
                TipoObra.LIGHT_NOVEL
        );

        var obraCriada = pareamentoService.submeterObraAnonima(request);

        assertThat(obraCriada.id()).isEqualTo(999L);
        assertThat(obraCriada.tituloPrincipal()).isEqualTo("Obra Desconhecida");
        assertThat(obraCriada.criadoPorUsuarioId()).isNull(); // 100% anônimo!
    }

    @Test
    @DisplayName("Objetivo 5: Federação entre Hubs (Mesh) com controle de hops e deduplicação")
    void deveProcessarLoteFederadoComControleDeHops() {
        NoFederado noVizinho = new NoFederado("Hub Sudeste", "https://hub-sp.shiori.dev", "secret-123");
        when(noFederadoRepository.findBySegredoCompartilhado("secret-123")).thenReturn(Optional.of(noVizinho));
        when(obraRepository.findByMangadexId("manga-123")).thenReturn(Optional.empty());
        when(obraRepository.findByTituloPrincipalIgnoreCase("One Piece")).thenReturn(Optional.empty());

        ObraFederadaItemDTO obraItem = new ObraFederadaItemDTO(
                "One Piece", "ワンピース", "https://img/op.jpg", "Piratas",
                1120, 108, TipoObra.MANGA, "manga-123", null, null, null, "FEDERACAO"
        );

        FederacaoLoteObrasDTO lote = new FederacaoLoteObrasDTO("Hub Sudeste", 2, List.of(obraItem));

        int processadas = federacaoService.receberLoteFederado("secret-123", lote);

        assertThat(processadas).isEqualTo(1);
        verify(obraRepository).save(any(Obra.class));
        assertThat(noVizinho.getTotalObrasRecebidas()).isEqualTo(1);
    }
}
