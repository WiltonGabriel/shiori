package dev.shiori.domain.model;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import jakarta.persistence.*;

import dev.shiori.domain.enums.StatusLeitura;
import dev.shiori.domain.enums.StatusObra;
import dev.shiori.domain.exception.RecursoNaoEncontradoException;
import dev.shiori.domain.exception.RegraDeNegocioException;

@Entity
@Table(name = "tb_obra")
public class Obra extends EntidadeBase {

    @Column(nullable = false, length = 255)
    private String tituloPrincipal;
    @Column(length = 512)
    private String tituloOriginal;
    @Column(length = 512)
    private String capaUrl;
    @Column(columnDefinition = "TEXT", length = 2048)
    private String sinopse;

    private Double notaGeral;

    @Enumerated(EnumType.STRING)
    private StatusObra statusObra;
    @Enumerated(EnumType.STRING)
    private StatusLeitura statusLeitura;

    private Double progressoAtual = 0.0;

    private Double avaliacaoPessoal;
    private Double mediaAvaliacaoCiclos;

    @OneToMany(mappedBy = "obra", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<HistoricoLeitura> historicoLeitura = new ArrayList<>();

    protected Obra() {
    }

    public Obra(
            String tituloPrincipal,
            String tituloOriginal,
            String capaUrl,
            String sinopse,
            Double notaGeral,
            StatusObra statusObra,
            StatusLeitura statusLeitura,
            Double avaliacaoPessoal) {

        if (tituloPrincipal == null || tituloPrincipal.isBlank()) {
            throw new RegraDeNegocioException("O título principal é obrigatório");
        }

        if (statusLeitura == null) {
            statusLeitura = StatusLeitura.LENDO;
        }

        if (avaliacaoPessoal != null && (avaliacaoPessoal > 10 || avaliacaoPessoal < 0)) {
            throw new RegraDeNegocioException("A avaliação pessoal deve estar entre 0 e 10");
        }

        this.tituloPrincipal = tituloPrincipal;
        this.tituloOriginal = tituloOriginal;
        this.capaUrl = capaUrl;
        this.sinopse = sinopse;
        this.notaGeral = notaGeral;
        this.statusObra = statusObra;
        this.statusLeitura = statusLeitura;
        this.avaliacaoPessoal = avaliacaoPessoal;
    }

}
