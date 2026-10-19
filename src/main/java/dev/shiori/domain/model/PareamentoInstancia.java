package dev.shiori.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

import dev.shiori.domain.enums.StatusPareamento;
import dev.shiori.domain.exception.RegraDeNegocioException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

@Entity
@Table(name = "tb_pareamento_instancia")
public class PareamentoInstancia extends EntidadeBase {

    @Column(name = "instance_uuid", nullable = false, unique = true, length = 64)
    private String instanceUuid;

    @Column(name = "apelido_publico", nullable = false, length = 60)
    private String apelidoPublico;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StatusPareamento status;

    @Column(name = "token_acesso", length = 128)
    private String tokenAcesso;

    @Column(name = "data_solicitacao", nullable = false)
    private LocalDateTime dataSolicitacao;

    @Column(name = "data_decisao")
    private LocalDateTime dataDecisao;

    @Column(name = "motivo_decisao", length = 255)
    private String motivoDecisao;

    @Column(name = "submissoes_validas", nullable = false)
    private int submissoesValidas;

    @Column(name = "tentativas_spam", nullable = false)
    private int tentativasSpam;

    @Column(name = "suspenso_ate")
    private LocalDateTime suspensoAte;

    protected PareamentoInstancia() {
    }

    public PareamentoInstancia(String instanceUuid, String apelidoPublico) {
        if (instanceUuid == null || instanceUuid.isBlank()) {
            throw new RegraDeNegocioException("O identificador único da instância (instanceUuid) é obrigatório");
        }
        if (apelidoPublico == null || apelidoPublico.isBlank()) {
            throw new RegraDeNegocioException("O apelido público da instância é obrigatório");
        }

        this.instanceUuid = instanceUuid.trim();
        this.apelidoPublico = apelidoPublico.trim();
        this.status = StatusPareamento.PENDENTE;
        this.dataSolicitacao = LocalDateTime.now();
        this.submissoesValidas = 0;
        this.tentativasSpam = 0;
    }

    public void autorizar() {
        this.status = StatusPareamento.AUTORIZADO;
        this.tokenAcesso = "shiori_inst_" + UUID.randomUUID().toString().replace("-", "");
        this.dataDecisao = LocalDateTime.now();
        this.motivoDecisao = "Instância autorizada pelo Administrador";
        this.suspensoAte = null;
    }

    public void suspender(int dias, String motivo) {
        this.status = StatusPareamento.SUSPENSO;
        this.dataDecisao = LocalDateTime.now();
        this.motivoDecisao = motivo != null ? motivo.trim() : "Suspensão temporária";
        this.suspensoAte = LocalDateTime.now().plusDays(Math.max(1, dias));
        this.tentativasSpam++;
    }

    public void banir(String motivo) {
        this.status = StatusPareamento.BANIDO;
        this.dataDecisao = LocalDateTime.now();
        this.motivoDecisao = motivo != null ? motivo.trim() : "Instância revogada / banida";
        this.tokenAcesso = null;
    }

    public void incrementarSubmissaoValida() {
        this.submissoesValidas++;
    }

    public void registrarTentativaSpam() {
        this.tentativasSpam++;
    }

    public boolean isAtiva() {
        if (this.status == StatusPareamento.AUTORIZADO) {
            return true;
        }
        if (this.status == StatusPareamento.SUSPENSO && this.suspensoAte != null && LocalDateTime.now().isAfter(this.suspensoAte)) {
            // Fim do cooldown da suspensão
            this.status = StatusPareamento.AUTORIZADO;
            this.suspensoAte = null;
            return true;
        }
        return false;
    }

    public String getInstanceUuid() {
        return instanceUuid;
    }

    public String getApelidoPublico() {
        return apelidoPublico;
    }

    public StatusPareamento getStatus() {
        return status;
    }

    public String getTokenAcesso() {
        return tokenAcesso;
    }

    public LocalDateTime getDataSolicitacao() {
        return dataSolicitacao;
    }

    public LocalDateTime getDataDecisao() {
        return dataDecisao;
    }

    public String getMotivoDecisao() {
        return motivoDecisao;
    }

    public int getSubmissoesValidas() {
        return submissoesValidas;
    }

    public int getTentativasSpam() {
        return tentativasSpam;
    }

    public LocalDateTime getSuspensoAte() {
        return suspensoAte;
    }
}
