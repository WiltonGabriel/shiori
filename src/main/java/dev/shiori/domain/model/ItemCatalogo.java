package dev.shiori.domain.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import dev.shiori.domain.enums.StatusLeitura;
import dev.shiori.domain.enums.Visibilidade;
import dev.shiori.domain.exception.RegraDeNegocioException;
import dev.shiori.infra.crypto.SensitiveDataCryptoConverter;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
        name = "tb_item_catalogo",
        uniqueConstraints = @UniqueConstraint(name = "uk_item_usuario_obra", columnNames = {"usuario_id", "obra_id"})
)
public class ItemCatalogo extends EntidadeBase {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "obra_id", nullable = false)
    private Obra obra;

    @Column(nullable = false)
    private Double progressoAtual = 0.0;

    @Column(name = "volume_atual", nullable = false)
    private Double volumeAtual = 0.0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusLeitura statusLeitura = StatusLeitura.LENDO;

    @Column
    private Double avaliacaoPessoal;

    @Convert(converter = SensitiveDataCryptoConverter.class)
    @Column(length = 500)
    private String urlLeitura;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Visibilidade visibilidade = Visibilidade.RESTRITO;

    @Column(nullable = false)
    private LocalDateTime dataAdicao;

    @Column(name = "ultima_atualizacao", nullable = false)
    private LocalDateTime ultimaAtualizacao;

    @OneToMany(mappedBy = "itemCatalogo", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<HistoricoLeitura> historicosLeitura = new ArrayList<>();

    protected ItemCatalogo() {
    }

    public ItemCatalogo(Usuario usuario, Obra obra, Visibilidade visibilidade, StatusLeitura statusLeitura) {
        if (usuario == null) {
            throw new RegraDeNegocioException("O usuário proprietário do item é obrigatório");
        }
        if (obra == null) {
            throw new RegraDeNegocioException("A obra vinculada ao item é obrigatória");
        }

        this.usuario = usuario;
        this.obra = obra;
        this.visibilidade = visibilidade != null ? visibilidade : Visibilidade.RESTRITO;
        this.statusLeitura = statusLeitura != null ? statusLeitura : StatusLeitura.LENDO;
        this.progressoAtual = 0.0;
        this.dataAdicao = LocalDateTime.now();
        this.ultimaAtualizacao = LocalDateTime.now();
    }

    public ItemCatalogo(Usuario usuario, Obra obra, Visibilidade visibilidade, StatusLeitura statusLeitura, String urlLeitura) {
        this(usuario, obra, visibilidade, statusLeitura);
        this.urlLeitura = (urlLeitura != null && !urlLeitura.isBlank()) ? urlLeitura.trim() : null;
    }

    public String obterUrlLeituraSensivel(Usuario solicitante) {
        if (solicitante != null && this.usuario != null && this.usuario.equals(solicitante)) {
            return this.urlLeitura;
        }
        return null;
    }

    public void atualizarProgresso(Double novoProgresso) {
        if (novoProgresso == null || novoProgresso < 0.0) {
            throw new RegraDeNegocioException("O progresso de leitura não pode ser nulo ou negativo");
        }
        if (this.obra.getTotalCapitulosLancados() != null && novoProgresso > this.obra.getTotalCapitulosLancados()) {
            throw new RegraDeNegocioException("O progresso não pode exceder o total de capítulos lançados da obra");
        }

        this.progressoAtual = novoProgresso;
        this.ultimaAtualizacao = LocalDateTime.now();
    }

    public void atualizarVolumeAtual(Double novoVolume) {
        if (novoVolume == null || novoVolume < 0.0) {
            throw new RegraDeNegocioException("O volume atual não pode ser nulo ou negativo");
        }
        if (this.obra.getTotalVolumes() != null && novoVolume > this.obra.getTotalVolumes()) {
            throw new RegraDeNegocioException("O volume atual não pode exceder o total de volumes da obra");
        }

        this.volumeAtual = novoVolume;
        this.ultimaAtualizacao = LocalDateTime.now();
    }

    public void atualizarProgresso(Double novoProgresso, Double novoVolume) {
        atualizarProgresso(novoProgresso);
        if (novoVolume != null) {
            atualizarVolumeAtual(novoVolume);
        }
    }

    public void alterarStatusLeitura(StatusLeitura novoStatus) {
        if (novoStatus == null) {
            throw new RegraDeNegocioException("O status de leitura não pode ser nulo");
        }
        this.statusLeitura = novoStatus;
        this.ultimaAtualizacao = LocalDateTime.now();
    }

    public void avaliar(Double nota) {
        if (nota != null && (nota < 0.0 || nota > 10.0)) {
            throw new RegraDeNegocioException("A avaliação pessoal deve estar entre 0 e 10");
        }
        this.avaliacaoPessoal = nota;
        this.ultimaAtualizacao = LocalDateTime.now();
    }

    public void alterarVisibilidade(Visibilidade novaVisibilidade) {
        if (novaVisibilidade == null) {
            throw new RegraDeNegocioException("A visibilidade não pode ser nula");
        }
        this.visibilidade = novaVisibilidade;
        this.ultimaAtualizacao = LocalDateTime.now();
    }

    public void atualizarUrlLeitura(String novaUrl) {
        this.urlLeitura = (novaUrl != null && !novaUrl.isBlank()) ? novaUrl.trim() : null;
        this.ultimaAtualizacao = LocalDateTime.now();
    }

    public HistoricoLeitura registrarCicloLeitura(Double capituloAtingido, Double notaCiclo, String anotacoes) {
        if (capituloAtingido == null) {
            throw new RegraDeNegocioException("O capítulo atingido é obrigatório para registrar o ciclo");
        }

        Double capituloInicial = this.progressoAtual;
        HistoricoLeitura ciclo = new HistoricoLeitura(this, capituloInicial, capituloAtingido, notaCiclo, anotacoes);
        this.historicosLeitura.add(ciclo);
        this.progressoAtual = capituloAtingido;
        this.ultimaAtualizacao = LocalDateTime.now();
        return ciclo;
    }

    public boolean isVisivelPara(Usuario visitante, boolean saoConexoes) {
        if (visitante != null && this.usuario != null && this.usuario.equals(visitante)) {
            return true;
        }
        if (this.visibilidade == Visibilidade.PUBLICO) {
            return true;
        }
        if (this.visibilidade == Visibilidade.PRIVADO) {
            return false;
        }
        return this.visibilidade == Visibilidade.RESTRITO && saoConexoes;
    }

    public Double getMediaAvaliacaoCiclos() {
        if (this.historicosLeitura.isEmpty()) {
            return null;
        }

        double soma = 0.0;
        int contagem = 0;

        for (HistoricoLeitura ciclo : this.historicosLeitura) {
            if (ciclo.getAvaliacaoCiclo() != null) {
                soma += ciclo.getAvaliacaoCiclo();
                contagem++;
            }
        }

        if (contagem == 0) {
            return null;
        }

        return Math.round((soma / contagem) * 100.0) / 100.0;
    }

    public List<HistoricoLeitura> getHistoricosLeitura() {
        return Collections.unmodifiableList(historicosLeitura);
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public Obra getObra() {
        return obra;
    }

    public Double getProgressoAtual() {
        return progressoAtual;
    }

    public Double getVolumeAtual() {
        return volumeAtual;
    }

    public StatusLeitura getStatusLeitura() {
        return statusLeitura;
    }

    public Double getAvaliacaoPessoal() {
        return avaliacaoPessoal;
    }

    public String getUrlLeitura() {
        return urlLeitura;
    }

    public Visibilidade getVisibilidade() {
        return visibilidade;
    }

    public LocalDateTime getDataAdicao() {
        return dataAdicao;
    }

    public LocalDateTime getUltimaAtualizacao() {
        return ultimaAtualizacao;
    }

    public LocalDateTime getDataAtualizacao() {
        return ultimaAtualizacao;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ItemCatalogo that = (ItemCatalogo) o;
        if (getId() != null && that.getId() != null) {
            return getId().equals(that.getId());
        }
        return Objects.equals(usuario, that.usuario) && Objects.equals(obra, that.obra);
    }

    @Override
    public int hashCode() {
        if (getId() != null) {
            return getId().hashCode();
        }
        return Objects.hash(usuario, obra);
    }
}
