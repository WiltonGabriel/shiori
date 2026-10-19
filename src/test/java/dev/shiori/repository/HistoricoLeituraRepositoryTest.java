package dev.shiori.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
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
import dev.shiori.domain.model.HistoricoLeitura;
import dev.shiori.domain.model.ItemCatalogo;
import dev.shiori.domain.model.Obra;
import dev.shiori.domain.model.Usuario;

@SpringBootTest
@Transactional
class HistoricoLeituraRepositoryTest {

    @Autowired
    private HistoricoLeituraRepository historicoLeituraRepository;

    @Autowired
    private ItemCatalogoRepository itemCatalogoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ObraRepository obraRepository;

    private Usuario usuario;
    private ItemCatalogo item;

    @BeforeEach
    void setup() {
        usuario = usuarioRepository.save(new Usuario("leitor.historico@shiori.dev", "senha123"));
        Obra obra = obraRepository.save(new Obra("Monster", null, null, "Suspense", 162, TipoObra.MANGA));
        item = itemCatalogoRepository.save(new ItemCatalogo(usuario, obra, Visibilidade.PUBLICO, StatusLeitura.LENDO));
    }

    @Test
    @DisplayName("Deve salvar histórico e recuperar por item de catálogo")
    void deveSalvarERecuperarPorItem() {
        HistoricoLeitura ciclo = new HistoricoLeitura(item, 0.0, 20.0, 10.0, "Sensacional");
        historicoLeituraRepository.save(ciclo);

        List<HistoricoLeitura> ciclos = historicoLeituraRepository.findByItemCatalogoId(item.getId());

        assertThat(ciclos).hasSize(1);
        assertThat(ciclos.get(0).getCapituloFinal()).isEqualTo(20.0);
        assertThat(ciclos.get(0).getUltimaAtualizacao()).isNotNull();
    }

    @Test
    @DisplayName("Deve recuperar históricos para o Shiori Wrapped pelo ano de leitura")
    void deveRecuperarParaWrappedPorAno() {
        HistoricoLeitura ciclo = new HistoricoLeitura(item, 0.0, 50.0, 9.0, "Leitura anual");
        historicoLeituraRepository.save(ciclo);

        int anoAtual = LocalDate.now().getYear();
        List<HistoricoLeitura> doAno = historicoLeituraRepository.findByUsuarioIdEAno(usuario.getId(), anoAtual);

        assertThat(doAno).hasSize(1);
        assertThat(doAno.get(0).getItemCatalogo().getUsuario().getId()).isEqualTo(usuario.getId());

        Page<HistoricoLeitura> paginados = historicoLeituraRepository.findByUsuarioId(usuario.getId(), PageRequest.of(0, 10));
        assertThat(paginados.getContent()).hasSize(1);
    }
}
