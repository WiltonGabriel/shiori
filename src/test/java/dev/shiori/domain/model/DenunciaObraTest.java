package dev.shiori.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import dev.shiori.domain.enums.MotivoDenuncia;
import dev.shiori.domain.enums.StatusDenuncia;
import dev.shiori.domain.enums.TipoObra;
import dev.shiori.domain.exception.RegraDeNegocioException;

class DenunciaObraTest {

    private Obra obra;
    private Usuario denunciante;

    @BeforeEach
    void setUp() {
        obra = new Obra(
                "Obra Suspeita",
                "Fake Title",
                null,
                "Texto copiado sem autorização",
                10,
                TipoObra.MANGA
        );
        denunciante = new Usuario("vigilante@shiori.dev", "senha", "vigilante");
    }

    @Test
    @DisplayName("Deve registrar denúncia com status inicial PENDENTE e data de criação")
    void deveRegistrarDenunciaComSucesso() {
        DenunciaObra denuncia = obra.registrarDenuncia(
                denunciante,
                MotivoDenuncia.DADOS_FALSOS_OU_DUPLICADOS,
                "O título original e o autor informados não coincidem com o registro oficial."
        );

        assertThat(denuncia.getObra()).isEqualTo(obra);
        assertThat(denuncia.getUsuarioDenunciante()).isEqualTo(denunciante);
        assertThat(denuncia.getMotivo()).isEqualTo(MotivoDenuncia.DADOS_FALSOS_OU_DUPLICADOS);
        assertThat(denuncia.getDetalhes()).contains("O título original");
        assertThat(denuncia.getStatus()).isEqualTo(StatusDenuncia.PENDENTE);
        assertThat(denuncia.getDataCriacao()).isNotNull();
        assertThat(denuncia.getDataResolucao()).isNull();
        assertThat(obra.getDenuncias()).contains(denuncia);
    }

    @Test
    @DisplayName("Não deve permitir registrar denúncia sem denunciante ou motivo")
    void deveValidarCamposObrigatoriosDaDenuncia() {
        assertThatThrownBy(() -> obra.registrarDenuncia(null, MotivoDenuncia.SPAM, "Detalhes"))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("usuário denunciante é obrigatório");

        assertThatThrownBy(() -> obra.registrarDenuncia(denunciante, null, "Detalhes"))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("motivo da denúncia é obrigatório");
    }

    @Test
    @DisplayName("Deve resolver denúncia como PROCEDENTE ou IMPROCEDENTE com parecer")
    void deveResolverDenunciaComSucesso() {
        DenunciaObra denuncia = obra.registrarDenuncia(
                denunciante,
                MotivoDenuncia.CONTEUDO_IMPROPRIO,
                "Capa com conteúdo impróprio para menores."
        );

        denuncia.resolverDenuncia(StatusDenuncia.PROCEDENTE, "Obra removida do catálogo após verificação da equipe.");

        assertThat(denuncia.getStatus()).isEqualTo(StatusDenuncia.PROCEDENTE);
        assertThat(denuncia.getParecerModeracao()).isEqualTo("Obra removida do catálogo após verificação da equipe.");
        assertThat(denuncia.getDataResolucao()).isNotNull();
    }

    @Test
    @DisplayName("Não deve permitir resolver denúncia mantendo status PENDENTE ou nulo")
    void naoDevePermitirResolverDenunciaComStatusInvalido() {
        DenunciaObra denuncia = obra.registrarDenuncia(
                denunciante,
                MotivoDenuncia.DADOS_FALSOS_OU_DUPLICADOS,
                "Já existe outra obra cadastrada com este ISBN."
        );

        assertThatThrownBy(() -> denuncia.resolverDenuncia(StatusDenuncia.PENDENTE, "Ainda analisando"))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("status de resolução deve ser PROCEDENTE ou IMPROCEDENTE");

        assertThatThrownBy(() -> denuncia.resolverDenuncia(null, null))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("status de resolução deve ser PROCEDENTE ou IMPROCEDENTE");
    }
}
