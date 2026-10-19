package dev.shiori.infra.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
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
import org.springframework.test.util.ReflectionTestUtils;

import dev.shiori.domain.enums.TipoAtividadeFeed;
import dev.shiori.domain.enums.TipoObra;
import dev.shiori.domain.event.AtividadeLeituraEvent;
import dev.shiori.domain.model.AtividadeFeed;
import dev.shiori.domain.model.Obra;
import dev.shiori.domain.model.Usuario;
import dev.shiori.repository.AtividadeFeedRepository;
import dev.shiori.repository.ItemCatalogoRepository;
import dev.shiori.repository.ObraRepository;
import dev.shiori.repository.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes Unitários: AtividadeFeedEventListener")
class AtividadeFeedEventListenerTest {

    @Mock
    private AtividadeFeedRepository atividadeFeedRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private ObraRepository obraRepository;

    @Mock
    private ItemCatalogoRepository itemCatalogoRepository;

    @InjectMocks
    private AtividadeFeedEventListener eventListener;

    private Usuario usuario;
    private Obra obra;

    @BeforeEach
    void setUp() {
        usuario = new Usuario("autor@shiori.dev", "senha", "autor");
        ReflectionTestUtils.setField(usuario, "id", 1L);

        obra = new Obra("Vagabond", "Vagabond", "https://capas.shiori.dev/vgb.jpg", "Miyamoto Musashi", 327, TipoObra.MANGA);
        ReflectionTestUtils.setField(obra, "id", 50L);
    }

    @Test
    @DisplayName("Deve processar evento assíncrono e persistir atividade no feed")
    void deveProcessarEventoEPersistirAtividade() {
        AtividadeLeituraEvent evento = new AtividadeLeituraEvent(
                1L,
                50L,
                null,
                TipoAtividadeFeed.CONCLUIU_LEITURA,
                "autor concluiu a leitura de Vagabond!"
        );

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(obraRepository.findById(50L)).thenReturn(Optional.of(obra));

        eventListener.processarEventoFeed(evento);

        ArgumentCaptor<AtividadeFeed> captor = ArgumentCaptor.forClass(AtividadeFeed.class);
        verify(atividadeFeedRepository).save(captor.capture());

        AtividadeFeed salva = captor.getValue();
        assertThat(salva.getUsuario()).isEqualTo(usuario);
        assertThat(salva.getObra()).isEqualTo(obra);
        assertThat(salva.getTipoAtividade()).isEqualTo(TipoAtividadeFeed.CONCLUIU_LEITURA);
        assertThat(salva.getDescricao()).isEqualTo("autor concluiu a leitura de Vagabond!");
    }

    @Test
    @DisplayName("Deve ignorar evento quando o usuário não for encontrado")
    void deveIgnorarEventoQuandoUsuarioNaoEncontrado() {
        AtividadeLeituraEvent evento = new AtividadeLeituraEvent(
                999L,
                50L,
                null,
                TipoAtividadeFeed.CONCLUIU_LEITURA,
                "Descrição"
        );

        when(usuarioRepository.findById(999L)).thenReturn(Optional.empty());

        eventListener.processarEventoFeed(evento);

        verify(atividadeFeedRepository, never()).save(any());
    }
}
