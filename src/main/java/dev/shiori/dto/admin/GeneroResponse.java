package dev.shiori.dto.admin;

import dev.shiori.domain.model.Genero;

public record GeneroResponse(
        Long id,
        String nome,
        String slug,
        String descricao
) {
    public static GeneroResponse deEntidade(Genero genero) {
        return new GeneroResponse(
                genero.getId(),
                genero.getNome(),
                genero.getSlug(),
                genero.getDescricao()
        );
    }
}
