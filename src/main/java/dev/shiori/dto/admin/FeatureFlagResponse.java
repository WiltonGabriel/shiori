package dev.shiori.dto.admin;

import java.time.LocalDateTime;

import dev.shiori.domain.model.FeatureFlag;

public record FeatureFlagResponse(
        Long id,
        String chave,
        String nome,
        String descricao,
        boolean ativado,
        LocalDateTime dataAtualizacao
) {
    public static FeatureFlagResponse deEntidade(FeatureFlag f) {
        return new FeatureFlagResponse(
                f.getId(),
                f.getChave(),
                f.getNome(),
                f.getDescricao(),
                f.isAtivado(),
                f.getDataAtualizacao()
        );
    }
}
