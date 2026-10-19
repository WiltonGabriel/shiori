package dev.shiori.domain.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "tb_registro_auditoria")
public class RegistroAuditoria extends EntidadeBase {

    @Column(nullable = false)
    private Long adminId;

    @Column(nullable = false, length = 100)
    private String adminApelido;

    @Column(nullable = false, length = 80)
    private String acao;

    @Column(nullable = false, length = 80)
    private String entidadeAfetada;

    @Column(length = 100)
    private String entidadeId;

    @Column(columnDefinition = "TEXT")
    private String detalhes;

    @Column(length = 60)
    private String ipOrigem;

    @Column(nullable = false)
    private LocalDateTime dataHora;

    protected RegistroAuditoria() {
    }

    public RegistroAuditoria(
            Long adminId,
            String adminApelido,
            String acao,
            String entidadeAfetada,
            String entidadeId,
            String detalhes,
            String ipOrigem) {
        this.adminId = adminId;
        this.adminApelido = adminApelido != null ? adminApelido : "sys_overseer";
        this.acao = acao;
        this.entidadeAfetada = entidadeAfetada;
        this.entidadeId = entidadeId;
        this.detalhes = detalhes;
        this.ipOrigem = ipOrigem;
        this.dataHora = LocalDateTime.now();
    }

    public Long getAdminId() {
        return adminId;
    }

    public String getAdminApelido() {
        return adminApelido;
    }

    public String getAcao() {
        return acao;
    }

    public String getEntidadeAfetada() {
        return entidadeAfetada;
    }

    public String getEntidadeId() {
        return entidadeId;
    }

    public String getDetalhes() {
        return detalhes;
    }

    public String getIpOrigem() {
        return ipOrigem;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }
}
