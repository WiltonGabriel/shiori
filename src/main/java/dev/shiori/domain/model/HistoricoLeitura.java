package dev.shiori.domain.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

import dev.shiori.domain.exception.RegraDeNegocioException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "tb_historico_leitura")
public class HistoricoLeitura extends EntidadeBase {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_catalogo_id", nullable = false)
    private ItemCatalogo itemCatalogo;

    @Column(nullable = false)
    private Double capituloInicial;

    @Column(nullable = false)
    private Double capituloFinal;

    @Column
    private Double avaliacaoCiclo;

    @Column(columnDefinition = "TEXT")
    private String anotacoes;

    @Column(nullable = false)
    private LocalDate dataLeitura;

    @Column(name = "ultima_atualizacao", nullable = false)
    private LocalDateTime ultimaAtualizacao;

    protected HistoricoLeitura() {
    }

    public HistoricoLeitura(
            ItemCatalogo itemCatalogo,
            Double capituloInicial,
            Double capituloFinal,
            Double avaliacaoCiclo,
            String anotacoes) {
        if (itemCatalogo == null) {
            throw new RegraDeNegocioException("O item do catálogo é obrigatório para registrar o histórico");
        }
        if (capituloInicial == null) {
            throw new RegraDeNegocioException("O capítulo inicial é obrigatório");
        }
        if (capituloFinal == null) {
            throw new RegraDeNegocioException("O capítulo final é obrigatório");
        }

        if (capituloInicial < 0.0) {
            throw new RegraDeNegocioException("O capítulo inicial não pode ser negativo");
        }

        if (capituloFinal <= capituloInicial) {
            throw new RegraDeNegocioException("O capítulo final deve ser maior que o inicial");
        }

        if (avaliacaoCiclo != null && (avaliacaoCiclo < 0.0 || avaliacaoCiclo > 10.0)) {
            throw new RegraDeNegocioException("A avaliação do ciclo deve estar entre 0 e 10");
        }

        this.itemCatalogo = itemCatalogo;
        this.capituloInicial = capituloInicial;
        this.capituloFinal = capituloFinal;
        this.avaliacaoCiclo = avaliacaoCiclo;
        this.anotacoes = anotacoes;
        this.dataLeitura = LocalDate.now();
        this.ultimaAtualizacao = LocalDateTime.now();
    }

    public HistoricoLeitura(
            ItemCatalogo itemCatalogo,
            Double capituloInicial,
            Double capituloFinal,
            Double avaliacaoCiclo,
            String anotacoes,
            LocalDate dataLeitura) {
        this(itemCatalogo, capituloInicial, capituloFinal, avaliacaoCiclo, anotacoes);
        if (dataLeitura != null) {
            this.dataLeitura = dataLeitura;
        }
    }

    public Double calcularTamanhoCiclo() {
        return this.capituloFinal - this.capituloInicial;
    }

    public boolean isCapituloUnico() {
        return this.calcularTamanhoCiclo().equals(1.0);
    }

    public boolean isCicloEmAberto() {
        return this.avaliacaoCiclo == null && (this.anotacoes == null || this.anotacoes.isBlank());
    }

    public void estenderCicloAte(Double novoCapituloFinal) {
        if (novoCapituloFinal == null || novoCapituloFinal <= this.capituloFinal) {
            throw new RegraDeNegocioException("O novo capítulo final deve ser maior que o atual");
        }
        this.capituloFinal = novoCapituloFinal;
        this.dataLeitura = LocalDate.now();
        this.ultimaAtualizacao = LocalDateTime.now();
    }

    public void atualizarAvaliacaoEAnotacoes(Double avaliacaoCiclo, String anotacoes) {
        if (avaliacaoCiclo != null && (avaliacaoCiclo < 0.0 || avaliacaoCiclo > 10.0)) {
            throw new RegraDeNegocioException("A avaliação do ciclo deve estar entre 0 e 10");
        }
        this.avaliacaoCiclo = avaliacaoCiclo;
        this.anotacoes = anotacoes;
        this.ultimaAtualizacao = LocalDateTime.now();
    }

    public void transferirParaItem(ItemCatalogo novoItem) {
        if (novoItem == null) {
            throw new RegraDeNegocioException("O novo item de catálogo é obrigatório");
        }
        this.itemCatalogo = novoItem;
        this.ultimaAtualizacao = LocalDateTime.now();
    }

    public ItemCatalogo getItemCatalogo() {
        return itemCatalogo;
    }

    public Double getCapituloInicial() {
        return capituloInicial;
    }

    public Double getCapituloFinal() {
        return capituloFinal;
    }

    public Double getAvaliacaoCiclo() {
        return avaliacaoCiclo;
    }

    public String getAnotacoes() {
        return anotacoes;
    }

    public LocalDate getDataLeitura() {
        return dataLeitura;
    }

    public LocalDateTime getUltimaAtualizacao() {
        return ultimaAtualizacao;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HistoricoLeitura that = (HistoricoLeitura) o;
        if (getId() != null && that.getId() != null) {
            return getId().equals(that.getId());
        }
        return Objects.equals(itemCatalogo, that.itemCatalogo) &&
                Objects.equals(capituloInicial, that.capituloInicial) &&
                Objects.equals(capituloFinal, that.capituloFinal) &&
                Objects.equals(dataLeitura, that.dataLeitura);
    }

    @Override
    public int hashCode() {
        if (getId() != null) {
            return getId().hashCode();
        }
        return Objects.hash(itemCatalogo, capituloInicial, capituloFinal, dataLeitura);
    }
}
