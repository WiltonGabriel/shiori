package dev.shiori.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import dev.shiori.domain.enums.StatusLeitura;
import dev.shiori.domain.enums.TipoAtividadeFeed;
import dev.shiori.domain.enums.TipoObra;
import dev.shiori.domain.enums.Visibilidade;
import dev.shiori.domain.event.AtividadeLeituraEvent;
import dev.shiori.domain.exception.RegraDeNegocioException;
import dev.shiori.domain.model.ItemCatalogo;
import dev.shiori.domain.model.Obra;
import dev.shiori.domain.model.Usuario;
import dev.shiori.dto.catalogo.AdicionarItemCatalogoRequest;
import dev.shiori.dto.catalogo.AtualizarVisibilidadeRequest;
import dev.shiori.dto.catalogo.ItemCatalogoResponse;
import dev.shiori.dto.catalogo.RegistrarProgressoRequest;
import dev.shiori.repository.ItemCatalogoRepository;
import dev.shiori.repository.ObraRepository;
import dev.shiori.repository.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes Unitários: ItemCatalogoService")
class ItemCatalogoServiceTest {

    @Mock
    private ItemCatalogoRepository itemCatalogoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private ObraRepository obraRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private ItemCatalogoService itemCatalogoService;

    private Usuario usuario;
    private Obra obra;

    @BeforeEach
    void setUp() {
        usuario = new Usuario("leitor@shiori.dev", "senhaHash", "leitor_shiori");
        ReflectionTestUtils.setField(usuario, "id", 1L);

        obra = new Obra("Berserk", "Beruseruku", "https://capas.shiori.dev/berserk.jpg", "Guts em sua jornada", 364, TipoObra.MANGA);
        ReflectionTestUtils.setField(obra, "id", 100L);
    }

    @Test
    @DisplayName("Deve adicionar obra à estante com sucesso e publicar evento assíncrono")
    void deveAdicionarObraEstanteComSucesso() {
        AdicionarItemCatalogoRequest request = new AdicionarItemCatalogoRequest(
                100L,
                Visibilidade.PUBLICO,
                StatusLeitura.LENDO,
                "https://meusite.com/ler/berserk"
        );

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(obraRepository.findById(100L)).thenReturn(Optional.of(obra));
        when(itemCatalogoRepository.existsByUsuarioIdAndObraId(1L, 100L)).thenReturn(false);

        when(itemCatalogoRepository.save(any(ItemCatalogo.class))).thenAnswer(invocation -> {
            ItemCatalogo item = invocation.getArgument(0);
            ReflectionTestUtils.setField(item, "id", 50L);
            return item;
        });

        ItemCatalogoResponse response = itemCatalogoService.adicionarObraEstante(1L, request);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(50L);
        assertThat(response.obraId()).isEqualTo(100L);
        assertThat(response.obraTitulo()).isEqualTo("Berserk");
        assertThat(response.visibilidade()).isEqualTo(Visibilidade.PUBLICO);
        assertThat(response.statusLeitura()).isEqualTo(StatusLeitura.LENDO);
        assertThat(response.urlLeitura()).isEqualTo("https://meusite.com/ler/berserk");

        ArgumentCaptor<AtividadeLeituraEvent> eventCaptor = ArgumentCaptor.forClass(AtividadeLeituraEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        AtividadeLeituraEvent published = eventCaptor.getValue();
        assertThat(published.usuarioId()).isEqualTo(1L);
        assertThat(published.obraId()).isEqualTo(100L);
        assertThat(published.tipoAtividade()).isEqualTo(TipoAtividadeFeed.ADICIONOU_ESTANTE);
    }

    @Test
    @DisplayName("Não deve permitir duplicar obra na mesma estante")
    void naoDevePermitirDuplicarObraNaEstante() {
        AdicionarItemCatalogoRequest request = new AdicionarItemCatalogoRequest(
                100L,
                Visibilidade.PUBLICO,
                StatusLeitura.LENDO,
                null
        );

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(obraRepository.findById(100L)).thenReturn(Optional.of(obra));
        when(itemCatalogoRepository.existsByUsuarioIdAndObraId(1L, 100L)).thenReturn(true);

        assertThatThrownBy(() -> itemCatalogoService.adicionarObraEstante(1L, request))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("Esta obra já está adicionada à sua estante");
    }

    @Test
    @DisplayName("Deve registrar progresso avançando capítulos (Ciclo Elástico) e emitir evento")
    void deveRegistrarProgressoAvancoCapitulos() {
        ItemCatalogo item = new ItemCatalogo(usuario, obra, Visibilidade.PUBLICO, StatusLeitura.LENDO);
        ReflectionTestUtils.setField(item, "id", 50L);

        RegistrarProgressoRequest request = new RegistrarProgressoRequest(
                25.0,
                3.0,
                9.5,
                "Excelente arco da Era de Ouro",
                StatusLeitura.LENDO
        );

        when(itemCatalogoRepository.findComDetalhesById(50L)).thenReturn(Optional.of(item));
        when(itemCatalogoRepository.save(any(ItemCatalogo.class))).thenReturn(item);

        ItemCatalogoResponse response = itemCatalogoService.registrarProgresso(1L, 50L, request);

        assertThat(response).isNotNull();
        assertThat(item.getProgressoAtual()).isEqualTo(25.0);
        assertThat(item.getVolumeAtual()).isEqualTo(3.0);
        assertThat(item.getAvaliacaoPessoal()).isEqualTo(9.5);
        assertThat(item.getHistoricosLeitura()).hasSize(1);
        assertThat(item.getHistoricosLeitura().get(0).getCapituloInicial()).isEqualTo(0.0);
        assertThat(item.getHistoricosLeitura().get(0).getCapituloFinal()).isEqualTo(25.0);

        ArgumentCaptor<AtividadeLeituraEvent> eventCaptor = ArgumentCaptor.forClass(AtividadeLeituraEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        AtividadeLeituraEvent published = eventCaptor.getValue();
        assertThat(published.tipoAtividade()).isEqualTo(TipoAtividadeFeed.ATUALIZOU_PROGRESSO);
    }

    @Test
    @DisplayName("Deve emitir evento de conclusão de leitura quando status for TERMINOU")
    void deveEmitirEventoConclusaoLeituraQuandoTerminou() {
        ItemCatalogo item = new ItemCatalogo(usuario, obra, Visibilidade.PUBLICO, StatusLeitura.LENDO);
        ReflectionTestUtils.setField(item, "id", 50L);

        RegistrarProgressoRequest request = new RegistrarProgressoRequest(
                364.0,
                41.0,
                10.0,
                "Obra-prima eterna",
                StatusLeitura.TERMINOU
        );

        when(itemCatalogoRepository.findComDetalhesById(50L)).thenReturn(Optional.of(item));
        when(itemCatalogoRepository.save(any(ItemCatalogo.class))).thenReturn(item);

        itemCatalogoService.registrarProgresso(1L, 50L, request);

        ArgumentCaptor<AtividadeLeituraEvent> eventCaptor = ArgumentCaptor.forClass(AtividadeLeituraEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        AtividadeLeituraEvent published = eventCaptor.getValue();
        assertThat(published.tipoAtividade()).isEqualTo(TipoAtividadeFeed.CONCLUIU_LEITURA);
    }

    @Test
    @DisplayName("Deve emitir evento de nota máxima quando avaliar com 10.0 sem terminar")
    void deveEmitirEventoNotaMaximaQuandoAvaliarCom10() {
        ItemCatalogo item = new ItemCatalogo(usuario, obra, Visibilidade.PUBLICO, StatusLeitura.LENDO);
        ReflectionTestUtils.setField(item, "id", 50L);

        RegistrarProgressoRequest request = new RegistrarProgressoRequest(
                100.0,
                12.0,
                10.0,
                "Capítulo lendário",
                StatusLeitura.LENDO
        );

        when(itemCatalogoRepository.findComDetalhesById(50L)).thenReturn(Optional.of(item));
        when(itemCatalogoRepository.save(any(ItemCatalogo.class))).thenReturn(item);

        itemCatalogoService.registrarProgresso(1L, 50L, request);

        ArgumentCaptor<AtividadeLeituraEvent> eventCaptor = ArgumentCaptor.forClass(AtividadeLeituraEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        AtividadeLeituraEvent published = eventCaptor.getValue();
        assertThat(published.tipoAtividade()).isEqualTo(TipoAtividadeFeed.AVALIOU_OBRA);
    }

    @Test
    @DisplayName("Deve atualizar anotação e nota no mesmo capítulo sem quebrar o ciclo")
    void deveAtualizarAnotacaoNoMesmoCapituloSemQuebrarCiclo() {
        ItemCatalogo item = new ItemCatalogo(usuario, obra, Visibilidade.PUBLICO, StatusLeitura.LENDO);
        ReflectionTestUtils.setField(item, "id", 50L);
        item.registrarCicloLeitura(20.0, 8.0, "Nota antiga");

        RegistrarProgressoRequest request = new RegistrarProgressoRequest(
                20.0,
                null,
                9.0,
                "Nota revisada",
                StatusLeitura.LENDO
        );

        when(itemCatalogoRepository.findComDetalhesById(50L)).thenReturn(Optional.of(item));
        when(itemCatalogoRepository.save(any(ItemCatalogo.class))).thenReturn(item);

        itemCatalogoService.registrarProgresso(1L, 50L, request);

        assertThat(item.getHistoricosLeitura()).hasSize(1);
        assertThat(item.getHistoricosLeitura().get(0).getAvaliacaoCiclo()).isEqualTo(9.0);
        assertThat(item.getHistoricosLeitura().get(0).getAnotacoes()).isEqualTo("Nota revisada");
    }

    @Test
    @DisplayName("Não deve permitir registrar progresso em item de outro usuário")
    void naoDevePermitirRegistrarProgressoEmItemDeOutroUsuario() {
        Usuario outroUsuario = new Usuario("outro@shiori.dev", "senha", "outro");
        ReflectionTestUtils.setField(outroUsuario, "id", 2L);

        ItemCatalogo item = new ItemCatalogo(outroUsuario, obra, Visibilidade.PUBLICO, StatusLeitura.LENDO);
        ReflectionTestUtils.setField(item, "id", 50L);

        RegistrarProgressoRequest request = new RegistrarProgressoRequest(10.0, null, null, null, null);

        when(itemCatalogoRepository.findComDetalhesById(50L)).thenReturn(Optional.of(item));

        assertThatThrownBy(() -> itemCatalogoService.registrarProgresso(1L, 50L, request))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("Você não tem permissão para manipular este item da estante");
    }

    @Test
    @DisplayName("Deve alterar visibilidade com sucesso")
    void deveAlterarVisibilidadeComSucesso() {
        ItemCatalogo item = new ItemCatalogo(usuario, obra, Visibilidade.PUBLICO, StatusLeitura.LENDO);
        ReflectionTestUtils.setField(item, "id", 50L);

        when(itemCatalogoRepository.findById(50L)).thenReturn(Optional.of(item));
        when(itemCatalogoRepository.save(any(ItemCatalogo.class))).thenReturn(item);

        AtualizarVisibilidadeRequest request = new AtualizarVisibilidadeRequest(Visibilidade.PRIVADO);
        ItemCatalogoResponse response = itemCatalogoService.alterarVisibilidade(1L, 50L, request);

        assertThat(response.visibilidade()).isEqualTo(Visibilidade.PRIVADO);
        assertThat(item.getVisibilidade()).isEqualTo(Visibilidade.PRIVADO);
    }

    @Test
    @DisplayName("Deve listar minha estante com paginação")
    void deveListarMinhaEstanteComPaginacao() {
        ItemCatalogo item = new ItemCatalogo(usuario, obra, Visibilidade.PUBLICO, StatusLeitura.LENDO);
        ReflectionTestUtils.setField(item, "id", 50L);

        Pageable pageable = PageRequest.of(0, 10);
        Page<ItemCatalogo> pagina = new PageImpl<>(java.util.List.of(item), pageable, 1);

        when(itemCatalogoRepository.findByUsuarioId(1L, pageable)).thenReturn(pagina);

        Page<ItemCatalogoResponse> resultado = itemCatalogoService.listarMinhaEstante(1L, pageable);

        assertThat(resultado.getContent()).hasSize(1);
        assertThat(resultado.getContent().get(0).obraTitulo()).isEqualTo("Berserk");
    }

    @Test
    @DisplayName("Deve remover item da estante com sucesso")
    void deveRemoverItemDaEstante() {
        ItemCatalogo item = new ItemCatalogo(usuario, obra, Visibilidade.PUBLICO, StatusLeitura.LENDO);
        ReflectionTestUtils.setField(item, "id", 50L);

        when(itemCatalogoRepository.findById(50L)).thenReturn(Optional.of(item));

        itemCatalogoService.removerItem(1L, 50L);

        verify(itemCatalogoRepository).delete(item);
    }
}
