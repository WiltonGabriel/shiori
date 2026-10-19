package dev.shiori.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

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

import dev.shiori.domain.enums.TipoAtividadeFeed;
import dev.shiori.domain.enums.TipoObra;
import dev.shiori.domain.model.AtividadeFeed;
import dev.shiori.domain.model.ConexaoUsuario;
import dev.shiori.domain.model.Obra;
import dev.shiori.domain.model.Usuario;
import dev.shiori.dto.feed.AtividadeFeedResponse;
import dev.shiori.repository.AtividadeFeedRepository;
import dev.shiori.repository.ConexaoUsuarioRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes Unitários: FeedService")
class FeedServiceTest {

    @Mock
    private AtividadeFeedRepository atividadeFeedRepository;

    @Mock
    private ConexaoUsuarioRepository conexaoUsuarioRepository;

    @InjectMocks
    private FeedService feedService;

    private Usuario usuarioLogado;
    private Usuario amigo;
    private Obra obra;

    @BeforeEach
    void setUp() {
        usuarioLogado = new Usuario("eu@shiori.dev", "senha", "eumesmo");
        ReflectionTestUtils.setField(usuarioLogado, "id", 1L);

        amigo = new Usuario("amigo@shiori.dev", "senha", "meuamigo");
        ReflectionTestUtils.setField(amigo, "id", 2L);

        obra = new Obra("One Piece", "Wan Pīsu", "https://capas.shiori.dev/op.jpg", "Luffy quer ser rei dos piratas", 1100, TipoObra.MANGA);
        ReflectionTestUtils.setField(obra, "id", 200L);
    }

    @Test
    @DisplayName("Deve retornar timeline com atividades próprias e de amigos com conexão aceita")
    void deveRetornarTimelineComAtividadesDeAmigosEProprias() {
        ConexaoUsuario conexao = new ConexaoUsuario(usuarioLogado, amigo);
        conexao.aceitar();

        when(conexaoUsuarioRepository.findConexoesAceitas(1L)).thenReturn(List.of(conexao));

        AtividadeFeed atividadeAmigo = new AtividadeFeed(
                amigo,
                TipoAtividadeFeed.CONCLUIU_LEITURA,
                obra,
                null,
                "meuamigo concluiu a leitura de One Piece!"
        );
        ReflectionTestUtils.setField(atividadeAmigo, "id", 99L);

        Pageable pageable = PageRequest.of(0, 10);
        Page<AtividadeFeed> pagina = new PageImpl<>(List.of(atividadeAmigo), pageable, 1);

        when(atividadeFeedRepository.findByUsuarioIdInOrderByDataCriacaoDesc(List.of(1L, 2L), pageable))
                .thenReturn(pagina);

        Page<AtividadeFeedResponse> resultado = feedService.obterFeed(1L, pageable);

        assertThat(resultado.getContent()).hasSize(1);
        AtividadeFeedResponse item = resultado.getContent().get(0);
        assertThat(item.id()).isEqualTo(99L);
        assertThat(item.usuarioId()).isEqualTo(2L);
        assertThat(item.usuarioApelido()).isEqualTo("meuamigo");
        assertThat(item.tipoAtividade()).isEqualTo(TipoAtividadeFeed.CONCLUIU_LEITURA);
        assertThat(item.obraTitulo()).isEqualTo("One Piece");
    }

    @Test
    @DisplayName("Deve retornar atividades públicas de um usuário específico")
    void deveRetornarAtividadesDeUsuarioEspecifico() {
        AtividadeFeed atividade = new AtividadeFeed(
                amigo,
                TipoAtividadeFeed.ADICIONOU_ESTANTE,
                obra,
                null,
                "meuamigo adicionou One Piece à sua estante"
        );
        ReflectionTestUtils.setField(atividade, "id", 88L);

        Pageable pageable = PageRequest.of(0, 10);
        Page<AtividadeFeed> pagina = new PageImpl<>(List.of(atividade), pageable, 1);

        when(atividadeFeedRepository.findByUsuarioIdOrderByDataCriacaoDesc(2L, pageable))
                .thenReturn(pagina);

        Page<AtividadeFeedResponse> resultado = feedService.obterAtividadesDeUsuario(2L, pageable);

        assertThat(resultado.getContent()).hasSize(1);
        assertThat(resultado.getContent().get(0).id()).isEqualTo(88L);
        verify(atividadeFeedRepository).findByUsuarioIdOrderByDataCriacaoDesc(2L, pageable);
    }
}
