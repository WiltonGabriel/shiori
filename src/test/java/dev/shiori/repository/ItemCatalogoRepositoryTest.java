package dev.shiori.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

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
import dev.shiori.domain.model.ItemCatalogo;
import dev.shiori.domain.model.Obra;
import dev.shiori.domain.model.Usuario;

@SpringBootTest
@Transactional
class ItemCatalogoRepositoryTest {

    @Autowired
    private ItemCatalogoRepository itemCatalogoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ObraRepository obraRepository;

    private Usuario usuario;
    private Obra obra;

    @BeforeEach
    void setup() {
        usuario = usuarioRepository.save(new Usuario("leitor.item@shiori.dev", "senha123"));
        obra = obraRepository.save(new Obra("Vinland Saga", null, null, "Vikings", 200, TipoObra.MANGA));
    }

    @Test
    @DisplayName("Deve salvar ItemCatalogo e recuperar com EntityGraph e ciclos")
    void deveSalvarERecuperarComEntityGraph() {
        ItemCatalogo item = new ItemCatalogo(usuario, obra, Visibilidade.PUBLICO, StatusLeitura.LENDO);
        item.registrarCicloLeitura(15.0, 9.5, "Início excelente");
        ItemCatalogo salvo = itemCatalogoRepository.save(item);

        Optional<ItemCatalogo> encontrado = itemCatalogoRepository.findComDetalhesById(salvo.getId());

        assertThat(encontrado).isPresent();
        assertThat(encontrado.get().getObra().getTituloPrincipal()).isEqualTo("Vinland Saga");
        assertThat(encontrado.get().getHistoricosLeitura()).hasSize(1);
        assertThat(encontrado.get().getUltimaAtualizacao()).isNotNull();
    }

    @Test
    @DisplayName("Deve paginar itens do catálogo por usuário e status de leitura")
    void devePaginarPorUsuarioEStatus() {
        ItemCatalogo item1 = new ItemCatalogo(usuario, obra, Visibilidade.PUBLICO, StatusLeitura.LENDO);
        itemCatalogoRepository.save(item1);

        Page<ItemCatalogo> resultado = itemCatalogoRepository.findByUsuarioIdAndStatusLeitura(
                usuario.getId(),
                StatusLeitura.LENDO,
                PageRequest.of(0, 10));

        assertThat(resultado.getContent()).hasSize(1);
        assertThat(itemCatalogoRepository.countByUsuarioId(usuario.getId())).isEqualTo(1);
        assertThat(itemCatalogoRepository.existsByUsuarioIdAndObraId(usuario.getId(), obra.getId())).isTrue();
    }
}
