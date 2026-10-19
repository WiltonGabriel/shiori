package dev.shiori.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import dev.shiori.domain.enums.StatusLeitura;
import dev.shiori.domain.enums.TipoObra;
import dev.shiori.domain.enums.Visibilidade;
import dev.shiori.domain.model.AtividadeFeed;
import dev.shiori.domain.model.ItemCatalogo;
import dev.shiori.domain.model.Obra;
import dev.shiori.domain.model.Usuario;

@SpringBootTest
@Transactional
class AtividadeFeedRepositoryTest {

    @Autowired
    private AtividadeFeedRepository atividadeFeedRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ObraRepository obraRepository;

    @Autowired
    private ItemCatalogoRepository itemCatalogoRepository;

    private Usuario userA;
    private Usuario userB;
    private Obra obra;
    private ItemCatalogo item;

    @BeforeEach
    void setup() {
        userA = usuarioRepository.save(new Usuario("userA@shiori.dev", "senha123"));
        userB = usuarioRepository.save(new Usuario("userB@shiori.dev", "senha123"));
        obra = obraRepository.save(new Obra("Berserk", null, null, "Fantasia Sombria", 364, TipoObra.MANGA));
        item = itemCatalogoRepository.save(new ItemCatalogo(userA, obra, Visibilidade.PUBLICO, StatusLeitura.LENDO));
    }

    @Test
    @DisplayName("Deve salvar atividades e buscar feed por lista de conexões com paginação")
    void deveSalvarEBuscarFeedPorConexoes() {
        AtividadeFeed a1 = AtividadeFeed.registrarInicioLeitura(userA, obra, item);
        AtividadeFeed a2 = AtividadeFeed.registrarProgresso(userB, obra, null, 10.0);
        atividadeFeedRepository.save(a1);
        atividadeFeedRepository.save(a2);

        Page<AtividadeFeed> feed = atividadeFeedRepository.findByUsuarioIdInOrderByDataCriacaoDesc(
                List.of(userA.getId(), userB.getId()),
                PageRequest.of(0, 10));

        assertThat(feed.getContent()).hasSize(2);
        assertThat(feed.getContent().get(0).getUsuario()).isNotNull();
    }

    @Test
    @DisplayName("Deve buscar atividades do perfil do usuário em ordem cronológica reversa")
    void deveBuscarAtividadesDoUsuario() {
        AtividadeFeed a1 = AtividadeFeed.registrarInicioLeitura(userA, obra, item);
        a1.setDataCriacao(java.time.LocalDateTime.now().minusHours(1));
        AtividadeFeed a2 = AtividadeFeed.registrarConclusao(userA, obra, item);
        atividadeFeedRepository.save(a1);
        atividadeFeedRepository.save(a2);

        Page<AtividadeFeed> feed = atividadeFeedRepository.findByUsuarioIdOrderByDataCriacaoDesc(
                userA.getId(),
                PageRequest.of(0, 10));

        assertThat(feed.getContent()).hasSize(2);
        assertThat(feed.getContent().get(0).getDescricao()).contains("concluiu");
    }
}
