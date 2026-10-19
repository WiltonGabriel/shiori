package dev.shiori.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import dev.shiori.domain.enums.StatusLeitura;
import dev.shiori.domain.enums.TipoAtividadeFeed;
import dev.shiori.domain.enums.TipoObra;
import dev.shiori.domain.enums.Visibilidade;
import dev.shiori.domain.exception.RegraDeNegocioException;

class AtividadeFeedTest {

    private Usuario usuario;
    private Obra obra;
    private ItemCatalogo item;

    @BeforeEach
    void setUp() {
        usuario = new Usuario("autor@shiori.dev", "senha", "leitor_ativo");
        obra = new Obra("Berserk", "Beruseruku", null, "Guts em sua jornada...", 375, TipoObra.MANGA);
        item = new ItemCatalogo(usuario, obra, Visibilidade.PUBLICO, StatusLeitura.LENDO);
    }

    @Test
    @DisplayName("Deve registrar início de leitura no feed")
    void deveRegistrarInicioLeitura() {
        AtividadeFeed atividade = AtividadeFeed.registrarInicioLeitura(usuario, obra, item);

        assertThat(atividade.getUsuario()).isEqualTo(usuario);
        assertThat(atividade.getTipoAtividade()).isEqualTo(TipoAtividadeFeed.INICIOU_LEITURA);
        assertThat(atividade.getObra()).isEqualTo(obra);
        assertThat(atividade.getItemCatalogo()).isEqualTo(item);
        assertThat(atividade.getDescricao()).contains("começou a ler Berserk");
        assertThat(atividade.getDataCriacao()).isNotNull();
    }

    @Test
    @DisplayName("Deve registrar progresso de leitura no feed")
    void deveRegistrarProgressoLeitura() {
        AtividadeFeed atividade = AtividadeFeed.registrarProgresso(usuario, obra, item, 87.0);

        assertThat(atividade.getTipoAtividade()).isEqualTo(TipoAtividadeFeed.ATUALIZOU_PROGRESSO);
        assertThat(atividade.getDescricao()).contains("alcançou o capítulo 87 de Berserk");
    }

    @Test
    @DisplayName("Deve registrar conclusão de leitura no feed")
    void deveRegistrarConclusaoLeitura() {
        AtividadeFeed atividade = AtividadeFeed.registrarConclusao(usuario, obra, item);

        assertThat(atividade.getTipoAtividade()).isEqualTo(TipoAtividadeFeed.CONCLUIU_LEITURA);
        assertThat(atividade.getDescricao()).contains("concluiu a leitura de Berserk!");
    }

    @Test
    @DisplayName("Deve registrar avaliação no feed")
    void deveRegistrarAvaliacaoObra() {
        AtividadeFeed atividade = AtividadeFeed.registrarAvaliacao(usuario, obra, item, 10.0);

        assertThat(atividade.getTipoAtividade()).isEqualTo(TipoAtividadeFeed.AVALIOU_OBRA);
        assertThat(atividade.getDescricao()).contains("avaliou Berserk com nota 10.0/10");
    }

    @Test
    @DisplayName("Deve registrar adição à estante no feed")
    void deveRegistrarAdicaoEstante() {
        AtividadeFeed atividade = AtividadeFeed.registrarAdicaoEstante(usuario, obra, item);

        assertThat(atividade.getTipoAtividade()).isEqualTo(TipoAtividadeFeed.ADICIONOU_ESTANTE);
        assertThat(atividade.getDescricao()).contains("adicionou Berserk à sua estante");
    }

    @Test
    @DisplayName("Validação de campos obrigatórios ao instanciar atividade")
    void deveValidarCamposObrigatorios() {
        assertThatThrownBy(() -> new AtividadeFeed(null, TipoAtividadeFeed.INICIOU_LEITURA, obra, item, "Desc"))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("usuário autor da atividade é obrigatório");

        assertThatThrownBy(() -> new AtividadeFeed(usuario, null, obra, item, "Desc"))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("tipo de atividade é obrigatório");

        assertThatThrownBy(() -> new AtividadeFeed(usuario, TipoAtividadeFeed.INICIOU_LEITURA, obra, item, "  "))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("descrição da atividade é obrigatória");
    }
}
