package dev.shiori.dto;

import java.time.LocalDateTime;

import dev.shiori.domain.enums.StatusSugestao;
import dev.shiori.domain.enums.TipoObra;
import dev.shiori.domain.model.SugestaoEdicaoObra;

public record SugestaoEdicaoResponse(
        Long id,
        Long obraId,
        String obraTitulo,
        Long usuarioSugeridorId,
        String usuarioSugeridorApelido,
        String tituloPrincipalSugerido,
        String tituloOriginalSugerido,
        String capaUrlSugerida,
        String sinopseSugerida,
        Integer totalCapitulosSugerido,
        TipoObra tipoObraSugerido,
        String justificativa,
        StatusSugestao status,
        Long adminAvaliadorId,
        String adminAvaliadorApelido,
        String parecerAdmin,
        LocalDateTime dataCriacao,
        LocalDateTime dataAvaliacao) {

    public static SugestaoEdicaoResponse fromEntity(SugestaoEdicaoObra sugestao) {
        return new SugestaoEdicaoResponse(
                sugestao.getId(),
                sugestao.getObra() != null ? sugestao.getObra().getId() : null,
                sugestao.getObra() != null ? sugestao.getObra().getTituloPrincipal() : null,
                sugestao.getUsuarioSugeridor() != null ? sugestao.getUsuarioSugeridor().getId() : null,
                sugestao.getUsuarioSugeridor() != null ? sugestao.getUsuarioSugeridor().getApelidoOuEmail() : null,
                sugestao.getTituloPrincipalSugerido(),
                sugestao.getTituloOriginalSugerido(),
                sugestao.getCapaUrlSugerida(),
                sugestao.getSinopseSugerida(),
                sugestao.getTotalCapitulosSugerido(),
                sugestao.getTipoObraSugerido(),
                sugestao.getJustificativa(),
                sugestao.getStatus(),
                sugestao.getAdminAvaliador() != null ? sugestao.getAdminAvaliador().getId() : null,
                sugestao.getAdminAvaliador() != null ? sugestao.getAdminAvaliador().getApelidoOuEmail() : null,
                sugestao.getParecerAdmin(),
                sugestao.getDataCriacao(),
                sugestao.getDataAvaliacao()
        );
    }
}
