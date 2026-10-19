package dev.shiori.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import dev.shiori.domain.enums.StatusLeitura;
import dev.shiori.domain.enums.TipoObra;
import dev.shiori.domain.enums.Visibilidade;
import dev.shiori.domain.exception.RegraDeNegocioException;

class ItemCatalogoTest {

    private Usuario proprietario;
    private Usuario amigoConectado;
    private Usuario estranho;
    private Obra obra;

    @BeforeEach
    void setUp() {
        proprietario = new Usuario("dono@shiori.dev", "senha", "dono");
        amigoConectado = new Usuario("amigo@shiori.dev", "senha", "amigo");
        estranho = new Usuario("estranho@shiori.dev", "senha", "estranho");

        obra = new Obra(
                "Chainsaw Man",
                "Chensō Man",
                "https://capas.shiori.dev/csm.jpg",
                "Denji é um jovem...",
                150,
                TipoObra.MANGA
        );
    }

    @Test
    @DisplayName("ItemCatalogo deve nascer com progresso zero e status LENDO por padrão")
    void itemCatalogoDeveNascerComValoresPadrao() {
        ItemCatalogo item = new ItemCatalogo(proprietario, obra, Visibilidade.RESTRITO, null);

        assertThat(item.getUsuario()).isEqualTo(proprietario);
        assertThat(item.getObra()).isEqualTo(obra);
        assertThat(item.getProgressoAtual()).isEqualTo(0.0);
        assertThat(item.getStatusLeitura()).isEqualTo(StatusLeitura.LENDO);
        assertThat(item.getVisibilidade()).isEqualTo(Visibilidade.RESTRITO);
        assertThat(item.getDataAdicao()).isNotNull();
    }

    @Test
    @DisplayName("Atualização de progresso não pode exceder o total de capítulos lançados")
    void atualizacaoDeProgressoDeveRespeitarTotalCapitulos() {
        ItemCatalogo item = new ItemCatalogo(proprietario, obra, Visibilidade.PUBLICO, StatusLeitura.LENDO);

        item.atualizarProgresso(50.0);
        assertThat(item.getProgressoAtual()).isEqualTo(50.0);

        assertThatThrownBy(() -> item.atualizarProgresso(200.0))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("não pode exceder o total de capítulos");

        assertThatThrownBy(() -> item.atualizarProgresso(-1.0))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("não pode ser nulo ou negativo");
    }

    @Test
    @DisplayName("Deve registrar ciclos de leitura e atualizar progresso automaticamente")
    void deveRegistrarCiclosDeLeitura() {
        ItemCatalogo item = new ItemCatalogo(proprietario, obra, Visibilidade.PUBLICO, StatusLeitura.LENDO);

        HistoricoLeitura ciclo1 = item.registrarCicloLeitura(20.0, 9.0, "Excelente introdução!");
        assertThat(item.getProgressoAtual()).isEqualTo(20.0);
        assertThat(ciclo1.getItemCatalogo()).isEqualTo(item);
        assertThat(ciclo1.getCapituloInicial()).isEqualTo(0.0);
        assertThat(ciclo1.getCapituloFinal()).isEqualTo(20.0);

        HistoricoLeitura ciclo2 = item.registrarCicloLeitura(45.0, 10.0, "Arco espetacular");
        assertThat(item.getProgressoAtual()).isEqualTo(45.0);
        assertThat(ciclo2.getCapituloInicial()).isEqualTo(20.0);
        assertThat(ciclo2.getCapituloFinal()).isEqualTo(45.0);

        assertThat(item.getHistoricosLeitura()).hasSize(2);
        assertThat(item.getMediaAvaliacaoCiclos()).isEqualTo(9.5);
    }

    @Test
    @DisplayName("Transição de status de leitura (LENDO, ENGAVETADO, TERMINOU)")
    void deveAlterarStatusLeitura() {
        ItemCatalogo item = new ItemCatalogo(proprietario, obra, Visibilidade.PUBLICO, StatusLeitura.LENDO);

        item.alterarStatusLeitura(StatusLeitura.ENGAVETADO);
        assertThat(item.getStatusLeitura()).isEqualTo(StatusLeitura.ENGAVETADO);

        item.alterarStatusLeitura(StatusLeitura.TERMINOU);
        assertThat(item.getStatusLeitura()).isEqualTo(StatusLeitura.TERMINOU);
    }

    @Test
    @DisplayName("Visibilidade PRIVADO deve ser visível EXCLUSIVAMENTE para o proprietário")
    void visibilidadePrivadaDeveSerApenasParaODono() {
        ItemCatalogo itemPrivado = new ItemCatalogo(proprietario, obra, Visibilidade.PRIVADO, StatusLeitura.LENDO);

        assertThat(itemPrivado.isVisivelPara(proprietario, false)).isTrue();
        assertThat(itemPrivado.isVisivelPara(amigoConectado, true)).isFalse();
        assertThat(itemPrivado.isVisivelPara(estranho, false)).isFalse();
        assertThat(itemPrivado.isVisivelPara(null, false)).isFalse();
    }

    @Test
    @DisplayName("Visibilidade RESTRITO deve ser visível para o dono e para conexões aceitas")
    void visibilidadeRestritaDeveSerParaDonoEConexoes() {
        ItemCatalogo itemRestrito = new ItemCatalogo(proprietario, obra, Visibilidade.RESTRITO, StatusLeitura.LENDO);

        assertThat(itemRestrito.isVisivelPara(proprietario, false)).isTrue();
        assertThat(itemRestrito.isVisivelPara(amigoConectado, true)).isTrue();
        assertThat(itemRestrito.isVisivelPara(estranho, false)).isFalse();
    }

    @Test
    @DisplayName("Visibilidade PUBLICO deve ser visível para todos")
    void visibilidadePublicaDeveSerParaTodos() {
        ItemCatalogo itemPublico = new ItemCatalogo(proprietario, obra, Visibilidade.PUBLICO, StatusLeitura.LENDO);

        assertThat(itemPublico.isVisivelPara(proprietario, false)).isTrue();
        assertThat(itemPublico.isVisivelPara(amigoConectado, true)).isTrue();
        assertThat(itemPublico.isVisivelPara(estranho, false)).isTrue();
        assertThat(itemPublico.isVisivelPara(null, false)).isTrue();
    }

    @Test
    @DisplayName("Link de leitura (urlLeitura) deve ser estritamente privado, visível apenas para o proprietário")
    void urlLeituraDeveSerEstritamentePrivadaParaODono() {
        String linkSecreto = "https://meusiteprivado.com/leitura/capitulo-12";
        ItemCatalogo itemPublico = new ItemCatalogo(
                proprietario,
                obra,
                Visibilidade.PUBLICO,
                StatusLeitura.LENDO,
                linkSecreto
        );

        // O dono consegue recuperar a URL sensível para WebView
        assertThat(itemPublico.obterUrlLeituraSensivel(proprietario)).isEqualTo(linkSecreto);

        // Mesmo sendo um item de visibilidade PUBLICO, visitantes e conexões NÃO têm acesso à urlLeitura
        assertThat(itemPublico.obterUrlLeituraSensivel(amigoConectado)).isNull();
        assertThat(itemPublico.obterUrlLeituraSensivel(estranho)).isNull();
        assertThat(itemPublico.obterUrlLeituraSensivel(null)).isNull();
    }

    @Test
    @DisplayName("Deve atualizar volume atual respeitando regras e total de volumes da obra")
    void deveAtualizarVolumeAtualComSucesso() {
        obra.atualizarTotalVolumes(12);
        ItemCatalogo item = new ItemCatalogo(proprietario, obra, Visibilidade.PUBLICO, StatusLeitura.LENDO);

        item.atualizarVolumeAtual(3.0);
        assertThat(item.getVolumeAtual()).isEqualTo(3.0);

        item.atualizarProgresso(45.0, 4.0);
        assertThat(item.getProgressoAtual()).isEqualTo(45.0);
        assertThat(item.getVolumeAtual()).isEqualTo(4.0);

        assertThatThrownBy(() -> item.atualizarVolumeAtual(15.0))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("não pode exceder o total de volumes");

        assertThatThrownBy(() -> item.atualizarVolumeAtual(-1.0))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("não pode ser nulo ou negativo");
    }
}
