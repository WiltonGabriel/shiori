package dev.shiori.domain.enums;

public enum StatusPareamento {
    PENDENTE("Pendente de Aprovação"),
    AUTORIZADO("Autorizado"),
    SUSPENSO("Suspenso Temporariamente"),
    BANIDO("Banido / Revogado");

    private final String descricao;

    StatusPareamento(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
