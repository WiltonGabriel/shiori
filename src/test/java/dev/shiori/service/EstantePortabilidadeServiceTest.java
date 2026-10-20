package dev.shiori.service;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mockito;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.shiori.domain.enums.StatusLeitura;
import dev.shiori.domain.enums.TipoObra;
import dev.shiori.domain.enums.Visibilidade;
import dev.shiori.domain.exception.RecursoNaoEncontradoException;
import dev.shiori.domain.model.ItemCatalogo;
import dev.shiori.domain.model.Obra;
import dev.shiori.domain.model.Usuario;
import dev.shiori.dto.catalogo.EstanteExportDTO;
import dev.shiori.dto.catalogo.EstanteImportRequest;
import dev.shiori.repository.ItemCatalogoRepository;
import dev.shiori.repository.ObraRepository;
import dev.shiori.repository.UsuarioRepository;

@DisplayName("Testes Unitários: EstantePortabilidadeService (SRP)")
class EstantePortabilidadeServiceTest {

    private ItemCatalogoRepository itemCatalogoRepository;
    private UsuarioRepository usuarioRepository;
    private ObraRepository obraRepository;
    private EstantePortabilidadeService portabilidadeService;

    private Usuario usuarioPadrao;

    @BeforeEach
    void setUp() {
        itemCatalogoRepository = mock(ItemCatalogoRepository.class);
        usuarioRepository = mock(UsuarioRepository.class);
        obraRepository = mock(ObraRepository.class);

        portabilidadeService = new EstantePortabilidadeService(
                itemCatalogoRepository,
                usuarioRepository,
                obraRepository
        );

        usuarioPadrao = new Usuario("leitor@shiori.dev", "hash_senha", "Leitor Shiori");
        try {
            var idField = Usuario.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(usuarioPadrao, 1L);
        } catch (Exception ignored) {
        }
    }

    @Test
    @DisplayName("Deve exportar a estante pessoal com snapshot completo e ciclos de leitura")
    void deveExportarEstanteComSucesso() {
        // Arrange
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioPadrao));

        Obra obra = new Obra("Berserk", "ベルセルク", "https://img/berserk.jpg", "Sinopse", 375, TipoObra.MANGA);
        ItemCatalogo item1 = new ItemCatalogo(
                usuarioPadrao,
                obra,
                "Berserk (Snapshot Local)",
                "https://img/berserk.jpg",
                TipoObra.MANGA,
                375,
                41,
                0.0,
                0.0,
                Visibilidade.PRIVADO,
                StatusLeitura.LENDO,
                "https://reader.secret.local/berserk",
                true
        );
        item1.registrarCicloLeitura(100.0, 9.5, "Excelente leitura");

        ItemCatalogo item2 = new ItemCatalogo(
                usuarioPadrao,
                "Obra Manual Local",
                null,
                TipoObra.LIVRO,
                null,
                null,
                15.0,
                1.0,
                Visibilidade.PRIVADO,
                StatusLeitura.LENDO,
                null
        );

        when(itemCatalogoRepository.findByUsuarioId(1L)).thenReturn(List.of(item1, item2));

        // Act
        EstanteExportDTO export = portabilidadeService.exportarEstante(1L);

        // Assert
        assertThat(export).isNotNull();
        assertThat(export.versao()).isEqualTo("1.0");
        assertThat(export.usuarioApelido()).isEqualToIgnoringCase("Leitor Shiori");
        assertThat(export.totalItens()).isEqualTo(2);

        var itemExport1 = export.itens().get(0);
        assertThat(itemExport1.titulo()).isEqualTo("Berserk (Snapshot Local)");
        assertThat(itemExport1.progressoAtual()).isEqualTo(100.0);
        assertThat(itemExport1.historicos()).hasSize(1);
        assertThat(itemExport1.historicos().get(0).anotacoes()).isEqualTo("Excelente leitura");

        var itemExport2 = export.itens().get(1);
        assertThat(itemExport2.titulo()).isEqualTo("Obra Manual Local");
        assertThat(itemExport2.tipoObra()).isEqualTo(TipoObra.LIVRO);
        assertThat(itemExport2.obraGlobalId()).isNull();
    }

    @Test
    @DisplayName("Deve lançar RecursoNaoEncontradoException ao exportar estante de usuário inexistente")
    void deveFalharExportarUsuarioInexistente() {
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> portabilidadeService.exportarEstante(99L))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessageContaining("Usuário não encontrado");
    }

    @Test
    @DisplayName("Deve importar estante com sucesso reconstruindo itens e ciclos de leitura")
    void deveImportarEstanteComSucesso() {
        // Arrange
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioPadrao));

        var cicloDto = new EstanteExportDTO.HistoricoCicloExportDTO(
                0.0, 50.0, 10.0, "Ciclo inicial concluído", java.time.LocalDate.now()
        );

        var itemDto = new EstanteExportDTO.EstanteItemExportDTO(
                "Vagabond",
                "https://img/vagabond.jpg",
                TipoObra.MANGA,
                50.0,
                5,
                327.0,
                37,
                StatusLeitura.LENDO,
                Visibilidade.RESTRITO,
                10.0,
                "https://secret.shiori.local/vagabond",
                true,
                null,
                List.of(cicloDto)
        );

        var request = new EstanteImportRequest(List.of(itemDto), false);

        // Act
        int importados = portabilidadeService.importarEstante(1L, request);

        // Assert
        assertThat(importados).isEqualTo(1);
        verify(itemCatalogoRepository, times(1)).save(any(ItemCatalogo.class));
    }

    @Test
    @DisplayName("Deve ignorar itens com título vazio na importação")
    void deveIgnorarItensInvalidosNaImportacao() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioPadrao));

        var itemInvalido = new EstanteExportDTO.EstanteItemExportDTO(
                "   ",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );

        var request = new EstanteImportRequest(List.of(itemInvalido), false);

        int importados = portabilidadeService.importarEstante(1L, request);

        assertThat(importados).isEqualTo(0);
        verify(itemCatalogoRepository, Mockito.never()).save(any(ItemCatalogo.class));
    }
}
