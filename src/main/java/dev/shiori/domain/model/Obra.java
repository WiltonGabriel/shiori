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
    private List<HistoricoLeitura> historicosLeitura = new ArrayList<>();

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

    public void registrarCicloLeitura(Double capituloAtingido, Double nota, String anotacoes) {

        if (capituloAtingido == null || capituloAtingido <= this.progressoAtual) {
            throw new RegraDeNegocioException("O capítulo atingido deve ser maior que o progresso atual e não nulo");
        }

        if (nota != null && (nota > 10 || nota < 0)) {
            throw new RegraDeNegocioException("A nota deve estar entre 0 e 10");
        }

        if (anotacoes != null && (!anotacoes.isBlank() && nota == null)) {
            throw new RegraDeNegocioException("Para registrar uma anotação no ciclo, é obrigatório atribuir uma nota.");
        }

        if (this.historicosLeitura.isEmpty()) {
            this.historicosLeitura
                    .add(new HistoricoLeitura(this, this.progressoAtual, capituloAtingido, nota, anotacoes));
        } else {
            HistoricoLeitura ultimoCiclo = this.historicosLeitura.get(this.historicosLeitura.size() - 1);
            if (nota == null && ultimoCiclo.isCicloEmAberto()) {
                ultimoCiclo.estenderCicloAte(capituloAtingido);
            } else {
                this.historicosLeitura
                        .add(new HistoricoLeitura(this, this.progressoAtual, capituloAtingido, nota, anotacoes));
            }
        }
        this.progressoAtual = capituloAtingido;

    }

    public Double calcularMediaAvaliacaoCiclos() {
        double somaNotasVezesTamanho = 0.0;
        double somaTamanhosAvaliados = 0.0;

        for (HistoricoLeitura h : this.historicosLeitura) {
            if (h.getAvaliacaoCiclo() != null) {
                somaNotasVezesTamanho += h.getAvaliacaoCiclo() * h.calcularTamanhoCiclo();
                somaTamanhosAvaliados += h.calcularTamanhoCiclo();
            }
        }
        if (somaTamanhosAvaliados == 0.0) {
            return null;
        }

        return somaNotasVezesTamanho / somaTamanhosAvaliados;
    }

}