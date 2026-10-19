package dev.shiori.domain.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import dev.shiori.domain.enums.AvisoConteudo;
import dev.shiori.domain.enums.MotivoDenuncia;
import dev.shiori.domain.enums.StatusCatalogo;
import dev.shiori.domain.enums.StatusPublicacao;
import dev.shiori.domain.enums.TipoObra;
import dev.shiori.domain.enums.TipoVoto;
import dev.shiori.domain.exception.RegraDeNegocioException;
import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Entity
@Table(name = "tb_obra")
public class Obra extends EntidadeBase {

    private static final Logger log = LoggerFactory.getLogger(Obra.class);

    @Column(nullable = false, length = 150)
    private String tituloPrincipal;

    @Column(length = 150)
    private String tituloOriginal;

    @Column(length = 500)
    private String capaUrl;

    @Column(columnDefinition = "TEXT")
    private String sinopse;

    @Column
    private Integer totalCapitulosLancados;

    @Column(name = "total_volumes")
    private Integer totalVolumes;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_publicacao", nullable = false)
    private StatusPublicacao statusPublicacao = StatusPublicacao.EM_LANCAMENTO;

    @Column(name = "anilist_id", length = 50)
    private String anilistId;

    @Column(name = "mal_id", length = 50)
    private String malId;

    @Column(name = "mangadex_id", length = 100)
    private String mangadexId;

    @Column(name = "kitsu_id", length = 100)
    private String kitsuId;

    @Column(name = "isbn", length = 50)
    private String isbn;

    @Column(name = "origem_fonte", length = 50)
    private String origemFonte = "SHIORI_INTERNO";

    @Column(name = "ultima_sincronizacao_externa")
    private LocalDateTime ultimaSincronizacaoExterna;

    @Column
    private Double notaGeral;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoObra tipoObra = TipoObra.MANGA;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusCatalogo statusCatalogo = StatusCatalogo.PRIVADO;

    @Column
    private Long criadoPorUsuarioId;

    @ManyToMany
    @JoinTable(
            name = "tb_obra_genero",
            joinColumns = @JoinColumn(name = "obra_id"),
            inverseJoinColumns = @JoinColumn(name = "genero_id")
    )
    private Set<Genero> generos = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "tb_obra_tag",
            joinColumns = @JoinColumn(name = "obra_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags = new HashSet<>();

    @OneToMany(mappedBy = "obra", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VotoTag> votosTag = new ArrayList<>();

    @OneToMany(mappedBy = "obra", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DenunciaObra> denuncias = new ArrayList<>();

    @OneToMany(mappedBy = "obra", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SugestaoEdicaoObra> sugestoesEdicao = new ArrayList<>();

    @ElementCollection(targetClass = AvisoConteudo.class)
    @CollectionTable(
            name = "tb_obra_avisos_conteudo",
            joinColumns = @JoinColumn(name = "obra_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "aviso_conteudo", nullable = false)
    private Set<AvisoConteudo> avisosConteudo = new HashSet<>();

    @Column(nullable = false)
    private boolean protegidoContraSync = false;

    protected Obra() {
    }

    public Obra(
            String tituloPrincipal,
            String tituloOriginal,
            String capaUrl,
            String sinopse,
            Integer totalCapitulosLancados,
            TipoObra tipoObra) {
        if (tituloPrincipal == null || tituloPrincipal.isBlank()) {
            throw new RegraDeNegocioException("O título principal é obrigatório");
        }
        if (totalCapitulosLancados != null && totalCapitulosLancados < 0) {
            throw new RegraDeNegocioException("O total de capítulos não pode ser negativo");
        }

        this.tituloPrincipal = tituloPrincipal.trim();
        this.tituloOriginal = tituloOriginal != null ? tituloOriginal.trim() : null;
        this.capaUrl = capaUrl;
        this.sinopse = sinopse;
        this.totalCapitulosLancados = totalCapitulosLancados;
        this.tipoObra = tipoObra != null ? tipoObra : TipoObra.MANGA;
        this.statusCatalogo = StatusCatalogo.PRIVADO;
    }

    public static Obra criarDeApiExterna(
            String tituloPrincipal,
            String tituloOriginal,
            String capaUrl,
            String sinopse,
            Integer totalCapitulosLancados,
            TipoObra tipoObra) {
        Obra obra = new Obra(tituloPrincipal, tituloOriginal, capaUrl, sinopse, totalCapitulosLancados, tipoObra);
        obra.statusCatalogo = StatusCatalogo.OFICIAL;
        return obra;
    }

    public static Obra criarDeFederacao(
            String tituloPrincipal,
            String tituloOriginal,
            String capaUrl,
            String sinopse,
            Integer totalCapitulosLancados,
            TipoObra tipoObra) {
        Obra obra = new Obra(tituloPrincipal, tituloOriginal, capaUrl, sinopse, totalCapitulosLancados, tipoObra);
        obra.statusCatalogo = StatusCatalogo.OFICIAL;
        return obra;
    }

    public void submeterAoCatalogoGlobal(Long usuarioId) {
        if (usuarioId == null) {
            throw new RegraDeNegocioException("O identificador do usuário autor é obrigatório para submeter ao catálogo global");
        }
        if (this.statusCatalogo == StatusCatalogo.OFICIAL) {
            throw new RegraDeNegocioException("Esta obra já é oficial do catálogo");
        }
        this.criadoPorUsuarioId = usuarioId;
        this.statusCatalogo = StatusCatalogo.COMUNITARIO;
    }

    public void submeterAnonimamenteAoCatalogoGlobal() {
        if (this.statusCatalogo == StatusCatalogo.OFICIAL) {
            throw new RegraDeNegocioException("Esta obra já é oficial do catálogo");
        }
        this.criadoPorUsuarioId = null;
        this.statusCatalogo = StatusCatalogo.COMUNITARIO;
    }

    public void promoverParaOficial() {
        if (this.statusCatalogo != StatusCatalogo.COMUNITARIO) {
            throw new RegraDeNegocioException("Apenas obras comunitárias submetidas podem ser promovidas a oficial por administradores");
        }
        this.statusCatalogo = StatusCatalogo.OFICIAL;
    }

    public void tornarPrivada() {
        this.statusCatalogo = StatusCatalogo.PRIVADO;
    }

    public boolean isVisivelNaHome() {
        return this.statusCatalogo == StatusCatalogo.OFICIAL || this.statusCatalogo == StatusCatalogo.COMUNITARIO;
    }

    public void atualizarDadosFrios(
            String tituloPrincipal,
            String tituloOriginal,
            String capaUrl,
            String sinopse,
            Integer totalCapitulosLancados,
            TipoObra tipoObra) {
        if (tituloPrincipal == null || tituloPrincipal.isBlank()) {
            throw new RegraDeNegocioException("O título principal é obrigatório");
        }
        if (totalCapitulosLancados != null && totalCapitulosLancados < 0) {
            throw new RegraDeNegocioException("O total de capítulos não pode ser negativo");
        }

        this.tituloPrincipal = tituloPrincipal.trim();
        this.tituloOriginal = tituloOriginal != null ? tituloOriginal.trim() : null;
        this.capaUrl = capaUrl;
        this.sinopse = sinopse;
        this.totalCapitulosLancados = totalCapitulosLancados;
        if (tipoObra != null) {
            this.tipoObra = tipoObra;
        }
    }

    public void atualizarPorUsuario(
            Usuario operador,
            String tituloPrincipal,
            String tituloOriginal,
            String capaUrl,
            String sinopse,
            Integer totalCapitulosLancados,
            TipoObra tipoObra) {
        if (operador == null) {
            throw new RegraDeNegocioException("O usuário operador é obrigatório para atualização");
        }
        if (this.statusCatalogo == StatusCatalogo.OFICIAL && !operador.isAdmin()) {
            throw new RegraDeNegocioException("Obras com status oficial são imutáveis para usuários comuns. Envie uma sugestão de edição.");
        }
        if (this.statusCatalogo != StatusCatalogo.OFICIAL && !operador.isAdmin()) {
            if (this.criadoPorUsuarioId != null && operador.getId() != null && !this.criadoPorUsuarioId.equals(operador.getId())) {
                throw new RegraDeNegocioException("Apenas o autor ou um administrador pode alterar esta obra");
            }
        }
        if (operador.isAdmin()) {
            log.info("AUDITORIA_SEGURANCA: Administrador [id={}, identificador={}] alterou diretamente a obra [id={}, titulo={}]",
                    operador.getId(), operador.getApelidoOuEmail(), getId(), this.tituloPrincipal);
        }
        atualizarDadosFrios(tituloPrincipal, tituloOriginal, capaUrl, sinopse, totalCapitulosLancados, tipoObra);
    }

    public void anonimizarCriador() {
        this.criadoPorUsuarioId = null;
    }

    public void oficializarPorAdmin(Usuario admin) {
        if (admin == null || !admin.isAdmin()) {
            throw new RegraDeNegocioException("Apenas administradores podem oficializar uma obra no catálogo");
        }
        this.statusCatalogo = StatusCatalogo.OFICIAL;
        log.warn("AUDITORIA_SEGURANCA: Administrador [id={}, apelido={}] oficializou a obra [id={}, titulo={}]",
                admin.getId(), admin.getApelidoOuEmail(), getId(), this.tituloPrincipal);
    }

    public void incorporarMetadadosDe(Obra duplicada) {
        if (duplicada == null || duplicada.equals(this)) {
            return;
        }
        for (Genero g : duplicada.getGeneros()) {
            this.generos.add(g);
        }
        for (Tag t : duplicada.getTags()) {
            this.tags.add(t);
        }
        for (AvisoConteudo ac : duplicada.getAvisosConteudo()) {
            this.avisosConteudo.add(ac);
        }
        if (this.anilistId == null && duplicada.getAnilistId() != null) {
            this.anilistId = duplicada.getAnilistId();
        }
        if (this.malId == null && duplicada.getMalId() != null) {
            this.malId = duplicada.getMalId();
        }
        if (this.mangadexId == null && duplicada.getMangadexId() != null) {
            this.mangadexId = duplicada.getMangadexId();
        }
        if (this.totalCapitulosLancados == null && duplicada.getTotalCapitulosLancados() != null) {
            this.totalCapitulosLancados = duplicada.getTotalCapitulosLancados();
        }
        if (this.totalVolumes == null && duplicada.getTotalVolumes() != null) {
            this.totalVolumes = duplicada.getTotalVolumes();
        }
    }

    public void aplicarOverrideAdministrativo(
            Usuario admin,
            String novoTituloPrincipal,
            String novoTituloOriginal,
            String novaCapaUrl,
            String novaSinopse,
            Integer novoTotalCapitulos,
            Integer novoTotalVolumes) {
        if (admin == null || !admin.isAdmin()) {
            throw new RegraDeNegocioException("Apenas administradores podem aplicar override em obras oficiais");
        }
        if (novoTituloPrincipal != null && !novoTituloPrincipal.isBlank()) {
            this.tituloPrincipal = novoTituloPrincipal.trim();
        }
        if (novoTituloOriginal != null) {
            this.tituloOriginal = novoTituloOriginal.trim();
        }
        if (novaCapaUrl != null) {
            this.capaUrl = novaCapaUrl.trim();
        }
        if (novaSinopse != null) {
            this.sinopse = novaSinopse.trim();
        }
        if (novoTotalCapitulos != null) {
            this.totalCapitulosLancados = novoTotalCapitulos;
        }
        if (novoTotalVolumes != null) {
            this.totalVolumes = novoTotalVolumes;
        }
        this.protegidoContraSync = true;
        this.statusCatalogo = StatusCatalogo.OFICIAL;

        log.warn("AUDITORIA_SEGURANCA: Administrador [id={}, apelido={}] aplicou OVERRIDE na obra [id={}, titulo='{}']. Obra agora blindada contra sync automático.",
                admin.getId(), admin.getApelidoOuEmail(), getId(), this.tituloPrincipal);
    }

    public boolean podeSerSincronizada() {
        return !this.protegidoContraSync;
    }

    public boolean isProtegidoContraSync() {
        return protegidoContraSync;
    }

    public void aplicarSugestaoAprovada(SugestaoEdicaoObra sugestao) {
        if (sugestao == null) {
            throw new RegraDeNegocioException("A sugestão de edição não pode ser nula");
        }
        if (sugestao.getTituloPrincipalSugerido() != null && !sugestao.getTituloPrincipalSugerido().isBlank()) {
            this.tituloPrincipal = sugestao.getTituloPrincipalSugerido().trim();
        }
        if (sugestao.getTituloOriginalSugerido() != null && !sugestao.getTituloOriginalSugerido().isBlank()) {
            this.tituloOriginal = sugestao.getTituloOriginalSugerido().trim();
        }
        if (sugestao.getCapaUrlSugerida() != null && !sugestao.getCapaUrlSugerida().isBlank()) {
            this.capaUrl = sugestao.getCapaUrlSugerida().trim();
        }
        if (sugestao.getSinopseSugerida() != null && !sugestao.getSinopseSugerida().isBlank()) {
            this.sinopse = sugestao.getSinopseSugerida().trim();
        }
        if (sugestao.getTotalCapitulosSugerido() != null) {
            if (sugestao.getTotalCapitulosSugerido() < 0) {
                throw new RegraDeNegocioException("O total de capítulos não pode ser negativo");
            }
            this.totalCapitulosLancados = sugestao.getTotalCapitulosSugerido();
        }
        if (sugestao.getTipoObraSugerido() != null) {
            this.tipoObra = sugestao.getTipoObraSugerido();
        }
    }

    public SugestaoEdicaoObra sugerirEdicao(
            Usuario usuarioSugeridor,
            String tituloPrincipal,
            String tituloOriginal,
            String capaUrl,
            String sinopse,
            Integer totalCapitulos,
            TipoObra tipoObra,
            String justificativa) {
        SugestaoEdicaoObra sugestao = new SugestaoEdicaoObra(
                this,
                usuarioSugeridor,
                tituloPrincipal,
                tituloOriginal,
                capaUrl,
                sinopse,
                totalCapitulos,
                tipoObra,
                justificativa);
        this.sugestoesEdicao.add(sugestao);
        return sugestao;
    }

    public void atualizarNotaGeral(Double notaGeral) {
        this.notaGeral = notaGeral;
    }

    public void adicionarGenero(Genero genero) {
        if (genero == null) {
            throw new RegraDeNegocioException("O gênero não pode ser nulo");
        }
        this.generos.add(genero);
    }

    public void removerGenero(Genero genero) {
        if (genero != null) {
            this.generos.remove(genero);
        }
    }

    public void adicionarTag(Tag tag) {
        if (tag == null) {
            throw new RegraDeNegocioException("A tag não pode ser nula");
        }
        this.tags.add(tag);
    }

    public void removerTag(Tag tag) {
        if (tag != null) {
            this.tags.remove(tag);
            this.votosTag.removeIf(v -> v.getTag().equals(tag));
        }
    }

    public void computarVotoTag(Tag tag, Usuario usuario, TipoVoto tipoVoto) {
        if (tag == null) {
            throw new RegraDeNegocioException("A tag é obrigatória");
        }
        if (usuario == null) {
            throw new RegraDeNegocioException("O usuário é obrigatório");
        }
        if (tipoVoto == null) {
            throw new RegraDeNegocioException("O tipo de voto é obrigatório");
        }
        if (!this.tags.contains(tag)) {
            throw new RegraDeNegocioException("A tag informada não está associada a esta obra");
        }

        for (VotoTag voto : this.votosTag) {
            if (voto.getTag().equals(tag) && voto.getUsuario().equals(usuario)) {
                voto.alterarVoto(tipoVoto);
                return;
            }
        }

        this.votosTag.add(new VotoTag(usuario, tag, this, tipoVoto));
    }

    public int calcularScoreTag(Tag tag) {
        if (tag == null) {
            return 0;
        }
        int score = 0;
        for (VotoTag voto : this.votosTag) {
            if (voto.getTag().equals(tag)) {
                score += voto.getTipoVoto().getValor();
            }
        }
        return score;
    }

    public boolean isTagVisivel(Tag tag) {
        return calcularScoreTag(tag) >= 0;
    }

    public Set<Tag> getTagsVisiveis() {
        Set<Tag> visiveis = new HashSet<>();
        for (Tag tag : this.tags) {
            if (isTagVisivel(tag)) {
                visiveis.add(tag);
            }
        }
        return Collections.unmodifiableSet(visiveis);
    }

    public DenunciaObra registrarDenuncia(Usuario denunciante, MotivoDenuncia motivo, String detalhes) {
        DenunciaObra denuncia = new DenunciaObra(this, denunciante, motivo, detalhes);
        this.denuncias.add(denuncia);
        return denuncia;
    }

    public Set<Genero> getGeneros() {
        return Collections.unmodifiableSet(generos);
    }

    public Set<Tag> getTags() {
        return Collections.unmodifiableSet(tags);
    }

    public List<VotoTag> getVotosTag() {
        return Collections.unmodifiableList(votosTag);
    }

    public List<DenunciaObra> getDenuncias() {
        return Collections.unmodifiableList(denuncias);
    }

    public List<SugestaoEdicaoObra> getSugestoesEdicao() {
        return Collections.unmodifiableList(sugestoesEdicao);
    }

    public String getTituloPrincipal() {
        return tituloPrincipal;
    }

    public String getTituloOriginal() {
        return tituloOriginal;
    }

    public String getCapaUrl() {
        return capaUrl;
    }

    public String getSinopse() {
        return sinopse;
    }

    public Integer getTotalCapitulosLancados() {
        return totalCapitulosLancados;
    }

    public Integer getTotalVolumes() {
        return totalVolumes;
    }

    public void atualizarTotalVolumes(Integer totalVolumes) {
        if (totalVolumes != null && totalVolumes < 0) {
            throw new RegraDeNegocioException("O total de volumes não pode ser negativo");
        }
        this.totalVolumes = totalVolumes;
    }

    public StatusPublicacao getStatusPublicacao() {
        return statusPublicacao;
    }

    public void atualizarStatusPublicacao(StatusPublicacao novoStatus) {
        if (novoStatus != null) {
            this.statusPublicacao = novoStatus;
        }
    }

    public String getAnilistId() {
        return anilistId;
    }

    public void definirAnilistId(String anilistId) {
        this.anilistId = (anilistId != null && !anilistId.isBlank()) ? anilistId.trim() : null;
    }

    public String getMalId() {
        return malId;
    }

    public void definirMalId(String malId) {
        this.malId = (malId != null && !malId.isBlank()) ? malId.trim() : null;
    }

    public String getMangadexId() {
        return mangadexId;
    }

    public void definirMangadexId(String mangadexId) {
        this.mangadexId = (mangadexId != null && !mangadexId.isBlank()) ? mangadexId.trim() : null;
    }

    public Double getNotaGeral() {
        return notaGeral;
    }

    public TipoObra getTipoObra() {
        return tipoObra;
    }

    public StatusCatalogo getStatusCatalogo() {
        return statusCatalogo;
    }

    public Long getCriadoPorUsuarioId() {
        return criadoPorUsuarioId;
    }

    public Set<AvisoConteudo> getAvisosConteudo() {
        return Collections.unmodifiableSet(this.avisosConteudo);
    }

    public void adicionarAvisoConteudo(AvisoConteudo aviso) {
        if (aviso != null) {
            this.avisosConteudo.add(aviso);
        }
    }

    public void removerAvisoConteudo(AvisoConteudo aviso) {
        if (aviso != null) {
            this.avisosConteudo.remove(aviso);
        }
    }

    public void definirAvisosConteudo(Set<AvisoConteudo> novosAvisos) {
        this.avisosConteudo.clear();
        if (novosAvisos != null) {
            for (AvisoConteudo aviso : novosAvisos) {
                if (aviso != null) {
                    this.avisosConteudo.add(aviso);
                }
            }
        }
    }

    public String getKitsuId() {
        return kitsuId;
    }

    public String getIsbn() {
        return isbn;
    }

    public String getOrigemFonte() {
        return origemFonte != null ? origemFonte : "SHIORI_INTERNO";
    }

    public LocalDateTime getUltimaSincronizacaoExterna() {
        return ultimaSincronizacaoExterna;
    }

    public void definirIdentificadoresExternos(String mangadexId, String anilistId, String kitsuId, String isbn, String origemFonte) {
        if (mangadexId != null && !mangadexId.isBlank()) this.mangadexId = mangadexId.trim();
        if (anilistId != null && !anilistId.isBlank()) this.anilistId = anilistId.trim();
        if (kitsuId != null && !kitsuId.isBlank()) this.kitsuId = kitsuId.trim();
        if (isbn != null && !isbn.isBlank()) this.isbn = isbn.trim();
        if (origemFonte != null && !origemFonte.isBlank()) this.origemFonte = origemFonte.trim().toUpperCase();
        this.ultimaSincronizacaoExterna = LocalDateTime.now();
    }

    public void atualizarSincronizacaoExterna(Integer novosCapitulos, Integer novosVolumes) {
        if (!this.protegidoContraSync) {
            if (novosCapitulos != null && novosCapitulos > 0) {
                if (this.totalCapitulosLancados == null || novosCapitulos > this.totalCapitulosLancados) {
                    this.totalCapitulosLancados = novosCapitulos;
                }
            }
            if (novosVolumes != null && novosVolumes > 0) {
                if (this.totalVolumes == null || novosVolumes > this.totalVolumes) {
                    this.totalVolumes = novosVolumes;
                }
            }
        }
        this.ultimaSincronizacaoExterna = LocalDateTime.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Obra obra = (Obra) o;
        if (getId() != null && obra.getId() != null) {
            return getId().equals(obra.getId());
        }
        return Objects.equals(tituloPrincipal, obra.tituloPrincipal) &&
                Objects.equals(tipoObra, obra.tipoObra);
    }

    @Override
    public int hashCode() {
        if (getId() != null) {
            return getId().hashCode();
        }
        return Objects.hash(tituloPrincipal, tipoObra);
    }
}