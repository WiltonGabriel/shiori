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

@Entity
@Table(
        name = "tb_item_catalogo"
)
public class ItemCatalogo extends EntidadeBase {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "obra_id", nullable = true)
    private Obra obra;

    @Column(length = 200)
    private String titulo;

    @Column(name = "capa_url", length = 500)
    private String capaUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_obra")
    private dev.shiori.domain.enums.TipoObra tipoObra = dev.shiori.domain.enums.TipoObra.MANGA;

    @Column(name = "total_capitulos")
    private Integer totalCapitulos;

    @Column(name = "total_volumes")
    private Integer totalVolumes;

    @Column(name = "sincronizar_metadados", nullable = false)
    private boolean sincronizarMetadados = true;

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
        this.titulo = obra.getTituloPrincipal();
        this.capaUrl = obra.getCapaUrl();
        this.tipoObra = obra.getTipoObra() != null ? obra.getTipoObra() : dev.shiori.domain.enums.TipoObra.MANGA;
        this.totalCapitulos = obra.getTotalCapitulosLancados();
        this.totalVolumes = obra.getTotalVolumes();
        this.sincronizarMetadados = true;
        this.visibilidade = visibilidade != null ? visibilidade : Visibilidade.RESTRITO;
        this.statusLeitura = statusLeitura != null ? statusLeitura : StatusLeitura.LENDO;
        this.progressoAtual = 0.0;
        this.volumeAtual = 0.0;
        this.dataAdicao = LocalDateTime.now();
        this.ultimaAtualizacao = LocalDateTime.now();
    }

    public ItemCatalogo(Usuario usuario, Obra obra, Visibilidade visibilidade, StatusLeitura statusLeitura, String urlLeitura) {
        this(usuario, obra, visibilidade, statusLeitura);
        this.urlLeitura = (urlLeitura != null && !urlLeitura.isBlank()) ? urlLeitura.trim() : null;
    }

    public ItemCatalogo(
            Usuario usuario,
            Obra obra,
            String tituloPersonalizado,
            String capaPersonalizada,
            dev.shiori.domain.enums.TipoObra tipoObra,
            Integer totalCapitulos,
            Integer totalVolumes,
            Double capituloInicial,
            Double volumeInicial,
            Visibilidade visibilidade,
            StatusLeitura statusLeitura,
            String urlLeitura,
            Boolean sincronizarMetadados) {
        if (usuario == null) {
            throw new RegraDeNegocioException("O usuário proprietário do item é obrigatório");
        }
        this.usuario = usuario;
        this.obra = obra;

        String tituloFinal = (tituloPersonalizado != null && !tituloPersonalizado.isBlank())
                ? tituloPersonalizado.trim()
                : (obra != null ? obra.getTituloPrincipal() : null);
        if (tituloFinal == null || tituloFinal.isBlank()) {
            throw new RegraDeNegocioException("O título da obra na estante é obrigatório");
        }
        this.titulo = tituloFinal;

        this.capaUrl = (capaPersonalizada != null && !capaPersonalizada.isBlank())
                ? capaPersonalizada.trim()
                : (obra != null ? obra.getCapaUrl() : null);

        this.tipoObra = tipoObra != null ? tipoObra
                : (obra != null && obra.getTipoObra() != null ? obra.getTipoObra() : dev.shiori.domain.enums.TipoObra.MANGA);

        this.totalCapitulos = totalCapitulos != null ? totalCapitulos
                : (obra != null ? obra.getTotalCapitulosLancados() : null);

        this.totalVolumes = totalVolumes != null ? totalVolumes
                : (obra != null ? obra.getTotalVolumes() : null);

        this.sincronizarMetadados = sincronizarMetadados != null ? sincronizarMetadados : (obra != null);
        this.visibilidade = visibilidade != null ? visibilidade : Visibilidade.RESTRITO;
        this.statusLeitura = statusLeitura != null ? statusLeitura : StatusLeitura.LENDO;
        this.urlLeitura = (urlLeitura != null && !urlLeitura.isBlank()) ? urlLeitura.trim() : null;
        this.progressoAtual = (capituloInicial != null && capituloInicial >= 0.0) ? capituloInicial : 0.0;
        this.volumeAtual = (volumeInicial != null && volumeInicial >= 0.0) ? volumeInicial : 0.0;
        this.dataAdicao = LocalDateTime.now();
        this.ultimaAtualizacao = LocalDateTime.now();
    }

    public ItemCatalogo(
            Usuario usuario,
            String tituloPersonalizado,
            String capaPersonalizada,
            dev.shiori.domain.enums.TipoObra tipoObra,
            Integer totalCapitulos,
            Integer totalVolumes,
            Double capituloInicial,
            Double volumeInicial,
            Visibilidade visibilidade,
            StatusLeitura statusLeitura,
            String urlLeitura) {
        this(usuario, null, tituloPersonalizado, capaPersonalizada, tipoObra, totalCapitulos, totalVolumes, capituloInicial, volumeInicial, visibilidade, statusLeitura, urlLeitura, false);
    }

    public String obterUrlLeituraSensivel(Usuario solicitante) {
        if (solicitante != null && this.usuario != null && this.usuario.equals(solicitante)) {
            return this.urlLeitura;
        }
        return null;
    }

    public void desvincularObraGlobal() {
        this.obra = null;
        this.sincronizarMetadados = false;
        this.ultimaAtualizacao = LocalDateTime.now();
    }

    public void vincularObraGlobal(Obra novaObra, boolean manterSincronizado) {
        if (novaObra == null) {
            throw new RegraDeNegocioException("A obra global a ser vinculada é obrigatória");
        }
        this.obra = novaObra;
        this.sincronizarMetadados = manterSincronizado;
        if (manterSincronizado) {
            if (this.titulo == null || this.titulo.isBlank()) {
                this.titulo = novaObra.getTituloPrincipal();
            }
            if (this.capaUrl == null || this.capaUrl.isBlank()) {
                this.capaUrl = novaObra.getCapaUrl();
            }
            if (novaObra.getTipoObra() != null) {
                this.tipoObra = novaObra.getTipoObra();
            }
            if (novaObra.getTotalCapitulosLancados() != null) {
                this.totalCapitulos = novaObra.getTotalCapitulosLancados();
            }
            if (novaObra.getTotalVolumes() != null) {
                this.totalVolumes = novaObra.getTotalVolumes();
            }
        }
        this.ultimaAtualizacao = LocalDateTime.now();
    }

    public boolean temNovosCapitulosDisponiveis() {
        if (this.obra != null && this.obra.getTotalCapitulosLancados() != null) {
            int capitulosSnapshot = this.totalCapitulos != null ? this.totalCapitulos : 0;
            return this.obra.getTotalCapitulosLancados() > capitulosSnapshot;
        }
        return false;
    }

    public void atualizarMetaCapitulosGlobal() {
        if (this.obra != null && this.obra.getTotalCapitulosLancados() != null) {
            this.totalCapitulos = this.obra.getTotalCapitulosLancados();
            this.ultimaAtualizacao = LocalDateTime.now();
        }
    }

    public void atualizarSnapshot(String novoTitulo, String novaCapaUrl, dev.shiori.domain.enums.TipoObra novoTipo, Integer novosCapitulos, Integer novosVolumes) {
        if (novoTitulo != null && !novoTitulo.isBlank()) {
            this.titulo = novoTitulo.trim();
        }
        if (novaCapaUrl != null) {
            this.capaUrl = novaCapaUrl.trim();
        }
        if (novoTipo != null) {
            this.tipoObra = novoTipo;
        }
        if (novosCapitulos != null) {
            this.totalCapitulos = novosCapitulos;
        }
        if (novosVolumes != null) {
            this.totalVolumes = novosVolumes;
        }
        this.ultimaAtualizacao = LocalDateTime.now();
    }

    public void atualizarProgresso(Double novoProgresso) {
        if (novoProgresso == null || novoProgresso < 0.0) {
            throw new RegraDeNegocioException("O progresso de leitura não pode ser nulo ou negativo");
        }
        Integer maxCaps = this.totalCapitulos != null ? this.totalCapitulos
                : (this.obra != null ? this.obra.getTotalCapitulosLancados() : null);
        if (maxCaps != null && maxCaps > 0 && novoProgresso > maxCaps) {
            throw new RegraDeNegocioException("O progresso não pode exceder o total de capítulos lançados da obra");
        }

        this.progressoAtual = novoProgresso;
        this.ultimaAtualizacao = LocalDateTime.now();
    }

    public void atualizarVolumeAtual(Double novoVolume) {
        if (novoVolume == null || novoVolume < 0.0) {
            throw new RegraDeNegocioException("O volume atual não pode ser nulo ou negativo");
        }
        Integer maxVols = this.totalVolumes != null ? this.totalVolumes
                : (this.obra != null ? this.obra.getTotalVolumes() : null);
        if (maxVols != null && maxVols > 0 && novoVolume > maxVols) {
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

    public void transferirParaObra(Obra novaObra) {
        if (novaObra == null) {
            throw new RegraDeNegocioException("A nova obra é obrigatória para transferência");
        }
        this.obra = novaObra;
        if (this.sincronizarMetadados) {
            if (this.titulo == null || this.titulo.isBlank()) {
                this.titulo = novaObra.getTituloPrincipal();
            }
            if (this.capaUrl == null || this.capaUrl.isBlank()) {
                this.capaUrl = novaObra.getCapaUrl();
            }
            if (novaObra.getTipoObra() != null) {
                this.tipoObra = novaObra.getTipoObra();
            }
            if (novaObra.getTotalCapitulosLancados() != null) {
                this.totalCapitulos = novaObra.getTotalCapitulosLancados();
            }
        }
        this.ultimaAtualizacao = LocalDateTime.now();
    }

    public void incorporarCiclosDe(ItemCatalogo outroItem) {
        if (outroItem == null || outroItem.equals(this)) {
            return;
        }
        for (HistoricoLeitura ciclo : new ArrayList<>(outroItem.getHistoricosLeitura())) {
            ciclo.transferirParaItem(this);
            this.historicosLeitura.add(ciclo);
        }
        if (outroItem.getProgressoAtual() != null && (this.progressoAtual == null || outroItem.getProgressoAtual() > this.progressoAtual)) {
            this.progressoAtual = outroItem.getProgressoAtual();
        }
        if (outroItem.getVolumeAtual() != null && (this.volumeAtual == null || outroItem.getVolumeAtual() > this.volumeAtual)) {
            this.volumeAtual = outroItem.getVolumeAtual();
        }
        if (this.avaliacaoPessoal == null && outroItem.getAvaliacaoPessoal() != null) {
            this.avaliacaoPessoal = outroItem.getAvaliacaoPessoal();
        }
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

    public void restaurarCicloLeitura(Double capituloInicial, Double capituloFinal, Double notaCiclo, String anotacoes, java.time.LocalDate dataLeitura) {
        if (capituloInicial != null && capituloFinal != null && capituloFinal > capituloInicial) {
            HistoricoLeitura ciclo = new HistoricoLeitura(this, capituloInicial, capituloFinal, notaCiclo, anotacoes, dataLeitura);
            this.historicosLeitura.add(ciclo);
        }
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

    public String getTitulo() {
        return (this.titulo != null && !this.titulo.isBlank())
                ? this.titulo
                : (this.obra != null ? this.obra.getTituloPrincipal() : "Obra Sem Título");
    }

    public String getCapaUrl() {
        return (this.capaUrl != null && !this.capaUrl.isBlank())
                ? this.capaUrl
                : (this.obra != null ? this.obra.getCapaUrl() : null);
    }

    public dev.shiori.domain.enums.TipoObra getTipoObra() {
        return this.tipoObra != null ? this.tipoObra
                : (this.obra != null && this.obra.getTipoObra() != null ? this.obra.getTipoObra() : dev.shiori.domain.enums.TipoObra.MANGA);
    }

    public Integer getTotalCapitulos() {
        return this.totalCapitulos != null ? this.totalCapitulos
                : (this.obra != null ? this.obra.getTotalCapitulosLancados() : null);
    }

    public Integer getTotalVolumes() {
        return this.totalVolumes != null ? this.totalVolumes
                : (this.obra != null ? this.obra.getTotalVolumes() : null);
    }

    public boolean isSincronizarMetadados() {
        return sincronizarMetadados;
    }

    public void setSincronizarMetadados(boolean sincronizarMetadados) {
        this.sincronizarMetadados = sincronizarMetadados;
        this.ultimaAtualizacao = LocalDateTime.now();
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
        return Objects.equals(usuario, that.usuario) && Objects.equals(getTitulo(), that.getTitulo());
    }

    @Override
    public int hashCode() {
        if (getId() != null) {
            return getId().hashCode();
        }
        return Objects.hash(usuario, getTitulo());
    }
}
