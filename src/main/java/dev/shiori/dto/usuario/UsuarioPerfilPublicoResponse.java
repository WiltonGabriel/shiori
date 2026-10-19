package dev.shiori.dto.usuario;

import org.springframework.data.domain.Page;

import dev.shiori.domain.model.Usuario;
import dev.shiori.dto.catalogo.ItemCatalogoResponse;

public record UsuarioPerfilPublicoResponse(
        Long id,
        String apelido,
        String fotoUrl,
        String descricao,
        String fusoHorario,
        Integer pontosConfianca,
        boolean saoConexoes,
        Page<ItemCatalogoResponse> estante
) {
    public static UsuarioPerfilPublicoResponse of(
            Usuario usuario,
            boolean saoConexoes,
            Page<ItemCatalogoResponse> estante) {
        return new UsuarioPerfilPublicoResponse(
                usuario.getId(),
                usuario.getApelidoOuEmail(),
                usuario.getFotoUrl(),
                usuario.getDescricao(),
                usuario.getFusoHorario(),
                usuario.getPontosConfianca(),
                saoConexoes,
                estante
        );
    }
}
