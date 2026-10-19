package dev.shiori.domain.model;

import java.time.LocalDateTime;

import dev.shiori.domain.enums.StatusSugestao;
import dev.shiori.domain.enums.TipoObra;
import dev.shiori.domain.exception.RegraDeNegocioException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "tb_sugestao_edicao_obra")
public class SugestaoEdicaoObra extends EntidadeBase {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "obra_id", nullable = false)
    private Obra obra;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuarioSugeridor;

    @Column(length = 150)
    private String tituloPrincipalSugerido;

    @Column(length = 150)
    private String tituloOriginalSugerido;

    @Column(length = 500)
    private String capaUrlSugerida;

    @Column(columnDefinition = "TEXT")
    private String sinopseSugerida;

    @Column
    private Integer totalCapitulosSugerido;

    @Enumerated(EnumType.STRING)
    @Column
    private TipoObra tipoObraSugerido;

    @Column(columnDefinition = "TEXT")
    private String justificativa;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StatusSugestao status = StatusSugestao.PENDENTE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_avaliador_id")
    private Usuario adminAvaliador;

    @Column(columnDefinition = "TEXT")
    private String parecerAdmin;

    @Column(nullable = false)
    private LocalDateTime dataCriacao;

    @Column
    private LocalDateTime dataAvaliacao;

    protected SugestaoEdicaoObra() {
    }

    public SugestaoEdicaoObra(
            Obra obra,
            Usuario usuarioSugeridor,
            String tituloPrincipalSugerido,
            String tituloOriginalSugerido,
            String capaUrlSugerida,
            String sinopseSugerida,
            Integer totalCapitulosSugerido,
            TipoObra tipoObraSugerido,
            String justificativa) {
        if (obra == null) {
            throw new RegraDeNegocioException("A obra referenciada é obrigatória para a sugestão");
        }
        if (usuarioSugeridor == null) {
            throw new RegraDeNegocioException("O usuário autor da sugestão é obrigatório");
        }

        boolean temAlgumCampo = (tituloPrincipalSugerido != null && !tituloPrincipalSugerido.isBlank())
                || (tituloOriginalSugerido != null && !tituloOriginalSugerido.isBlank())
                || (capaUrlSugerida != null && !capaUrlSugerida.isBlank())
                || (sinopseSugerida != null && !sinopseSugerida.isBlank())
                || (totalCapitulosSugerido != null)
                || (tipoObraSugerido != null);

        if (!temAlgumCampo) {
            throw new RegraDeNegocioException("Ao menos um campo deve ser sugerido para alteração");
        }
        if (totalCapitulosSugerido != null && totalCapitulosSugerido < 0) {
            throw new RegraDeNegocioException("O total de capítulos não pode ser negativo");
        }

        this.obra = obra;
        this.usuarioSugeridor = usuarioSugeridor;
        this.tituloPrincipalSugerido = tituloPrincipalSugerido != null ? tituloPrincipalSugerido.trim() : null;
        this.tituloOriginalSugerido = tituloOriginalSugerido != null ? tituloOriginalSugerido.trim() : null;
        this.capaUrlSugerida = capaUrlSugerida != null ? capaUrlSugerida.trim() : null;
        this.sinopseSugerida = sinopseSugerida != null ? sinopseSugerida.trim() : null;
        this.totalCapitulosSugerido = totalCapitulosSugerido;
        this.tipoObraSugerido = tipoObraSugerido;
        this.justificativa = justificativa != null ? justificativa.trim() : null;
        this.status = StatusSugestao.PENDENTE;
        this.dataCriacao = LocalDateTime.now();
    }

    public void aprovar(Usuario admin, String parecerAdmin) {
        validarAvaliadorAdmin(admin);
        if (this.status != StatusSugestao.PENDENTE) {
            throw new RegraDeNegocioException("Esta sugestão já foi avaliada anteriormente");
        }

        this.status = StatusSugestao.APROVADA;
        this.adminAvaliador = admin;
        this.parecerAdmin = parecerAdmin != null ? parecerAdmin.trim() : null;
        this.dataAvaliacao = LocalDateTime.now();

        this.obra.aplicarSugestaoAprovada(this);

        if (this.usuarioSugeridor != null) {
            this.usuarioSugeridor.adicionarPontosConfianca(10);
        }
    }

    public void rejeitar(Usuario admin, String parecerAdmin) {
        validarAvaliadorAdmin(admin);
        if (this.status != StatusSugestao.PENDENTE) {
            throw new RegraDeNegocioException("Esta sugestão já foi avaliada anteriormente");
        }

        this.status = StatusSugestao.REJEITADA;
        this.adminAvaliador = admin;
        this.parecerAdmin = parecerAdmin != null ? parecerAdmin.trim() : null;
        this.dataAvaliacao = LocalDateTime.now();
    }

    private void validarAvaliadorAdmin(Usuario admin) {
        if (admin == null || !admin.isAdmin()) {
            throw new RegraDeNegocioException("Apenas administradores podem avaliar sugestões de edição");
        }
    }

    public Obra getObra() {
        return obra;
    }

    public Usuario getUsuarioSugeridor() {
        return usuarioSugeridor;
    }

    public String getTituloPrincipalSugerido() {
        return tituloPrincipalSugerido;
    }

    public String getTituloOriginalSugerido() {
        return tituloOriginalSugerido;
    }

    public String getCapaUrlSugerida() {
        return capaUrlSugerida;
    }

    public String getSinopseSugerida() {
        return sinopseSugerida;
    }

    public Integer getTotalCapitulosSugerido() {
        return totalCapitulosSugerido;
    }

    public TipoObra getTipoObraSugerido() {
        return tipoObraSugerido;
    }

    public String getJustificativa() {
        return justificativa;
    }

    public StatusSugestao getStatus() {
        return status;
    }

    public Usuario getAdminAvaliador() {
        return adminAvaliador;
    }

    public String getParecerAdmin() {
        return parecerAdmin;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public LocalDateTime getDataAvaliacao() {
        return dataAvaliacao;
    }
}
