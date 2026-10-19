package dev.shiori.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import dev.shiori.domain.enums.TipoObra;
import dev.shiori.domain.exception.RegraDeNegocioException;
import dev.shiori.domain.model.Obra;
import dev.shiori.domain.model.Usuario;
import dev.shiori.dto.ResultadoBuscaObrasResponse;
import dev.shiori.dto.catalogo.AdicionarItemCatalogoRequest;
import dev.shiori.repository.ItemCatalogoRepository;
import dev.shiori.repository.ObraRepository;
import dev.shiori.repository.UsuarioRepository;

class FormatosPreferidosEBuscaAssistidaTest {

    private ObraRepository obraRepository;
    private UsuarioRepository usuarioRepository;
    private ItemCatalogoRepository itemCatalogoRepository;
    private ApplicationEventPublisher eventPublisher;

    private ObraService obraService;
    private ItemCatalogoService itemCatalogoService;

    @BeforeEach
    void setUp() {
        obraRepository = mock(ObraRepository.class);
        usuarioRepository = mock(UsuarioRepository.class);
        itemCatalogoRepository = mock(ItemCatalogoRepository.class);
        eventPublisher = mock(ApplicationEventPublisher.class);

        obraService = new ObraService(obraRepository, usuarioRepository);
        itemCatalogoService = new ItemCatalogoService(itemCatalogoRepository, usuarioRepository, obraRepository, eventPublisher);
    }

    @Test
    @DisplayName("Usuário deve validar que pelo menos um formato de leitura é obrigatório no onboarding")
    void deveExigirPeloMenosUmFormatoNoOnboarding() {
        Usuario usuario = new Usuario("leitor@shiori.dev", "senha123");

        assertThatThrownBy(() -> usuario.concluirOnboarding("leitor", "America/Sao_Paulo", null, null, Set.of()))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("Selecione ao menos um formato de leitura");

        assertThatThrownBy(() -> usuario.concluirOnboarding("leitor", "America/Sao_Paulo", null, null, null))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("Selecione ao menos um formato de leitura");
    }

    @Test
    @DisplayName("Usuário deve conseguir definir e atualizar seus formatos preferidos")
    void devePermitirDefinirFormatosPreferidos() {
        Usuario usuario = new Usuario("leitor@shiori.dev", "senha123");
        usuario.concluirOnboarding("leitor", "America/Sao_Paulo", null, null, Set.of(TipoObra.LIVRO, TipoObra.COMIC));

        assertThat(usuario.getFormatosPreferidos()).containsExactlyInAnyOrder(TipoObra.LIVRO, TipoObra.COMIC);

        usuario.atualizarFormatosPreferidos(Set.of(TipoObra.MANGA, TipoObra.LIGHT_NOVEL, TipoObra.WEB_NOVEL));
        assertThat(usuario.getFormatosPreferidos()).containsExactlyInAnyOrder(TipoObra.MANGA, TipoObra.LIGHT_NOVEL, TipoObra.WEB_NOVEL);
    }

    @Test
    @DisplayName("Não deve permitir adicionar à estante uma obra cujo formato esteja desativado nas preferências")
    void naoDevePermitirAdicionarObraDeFormatoDesativado() {
        Usuario usuario = new Usuario("leitor@shiori.dev", "senha123");
        usuario.concluirOnboarding("leitor", "America/Sao_Paulo", null, null, Set.of(TipoObra.LIVRO));

        Obra manga = new Obra("Berserk", "Beruseruku", null, "Guts...", 364, TipoObra.MANGA);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(obraRepository.findById(10L)).thenReturn(Optional.of(manga));
        when(itemCatalogoRepository.existsByUsuarioIdAndObraId(1L, 10L)).thenReturn(false);

        AdicionarItemCatalogoRequest request = new AdicionarItemCatalogoRequest(10L, null, null, null);

        assertThatThrownBy(() -> itemCatalogoService.adicionarObraEstante(1L, request))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("desativado em suas preferências de leitura");
    }

    @Test
    @DisplayName("Busca assistida deve retornar obras normalmente se houver correspondência em formatos permitidos")
    void buscaAssistidaDeveRetornarObrasEmFormatosPermitidos() {
        Usuario usuario = new Usuario("leitor@shiori.dev", "senha123");
        usuario.concluirOnboarding("leitor", "America/Sao_Paulo", null, null, Set.of(TipoObra.MANGA, TipoObra.LIGHT_NOVEL));

        Obra manga = new Obra("Solo Leveling", null, null, "Sinopse", 179, TipoObra.MANGA);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(obraRepository.pesquisarPorTermo("Solo")).thenReturn(List.of(manga));

        ResultadoBuscaObrasResponse resultado = obraService.buscarObrasComAssistencia("Solo", 1L);

        assertThat(resultado.obras()).hasSize(1);
        assertThat(resultado.correspondenciasEmOutrosFormatos()).isFalse();
        assertThat(resultado.formatosDetectadosNaoPermitidos()).isEmpty();
    }

    @Test
    @DisplayName("Busca assistida deve ocultar cards e emitir aviso contextual quando o termo só existir em formatos desativados")
    void buscaAssistidaDeveOcultarCardsEEmitirAvisoContextual() {
        Usuario usuario = new Usuario("leitor@shiori.dev", "senha123");
        // Usuário configurou APENAS livros tradicionais
        usuario.concluirOnboarding("leitor", "America/Sao_Paulo", null, null, Set.of(TipoObra.LIVRO));

        // Obra encontrada é um Mangá e uma Light Novel
        Obra manga = new Obra("Omniscient Reader", null, null, "Sinopse", 200, TipoObra.MANGA);
        Obra novel = new Obra("Omniscient Reader (Novel)", null, null, "Sinopse", 551, TipoObra.LIGHT_NOVEL);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(obraRepository.pesquisarPorTermo("Omniscient")).thenReturn(List.of(manga, novel));

        ResultadoBuscaObrasResponse resultado = obraService.buscarObrasComAssistencia("Omniscient", 1L);

        // NÃO exibe cards nem capas!
        assertThat(resultado.obras()).isEmpty();
        assertThat(resultado.correspondenciasEmOutrosFormatos()).isTrue();
        assertThat(resultado.formatosDetectadosNaoPermitidos()).contains(TipoObra.MANGA, TipoObra.LIGHT_NOVEL);
        assertThat(resultado.mensagemContextual()).contains("Omniscient");
        assertThat(resultado.mensagemContextual()).contains("preferências de leitura");
    }
}
