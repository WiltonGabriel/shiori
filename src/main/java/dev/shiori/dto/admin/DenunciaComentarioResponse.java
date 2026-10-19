package dev.shiori.dto.admin;

import java.time.LocalDateTime;

import dev.shiori.domain.enums.MotivoDenuncia;
import dev.shiori.domain.enums.StatusDenuncia;
import dev.shiori.domain.model.DenunciaComentario;

public record DenunciaComentarioResponse(
        Long id,
        Long comentarioId,
        String comentarioTexto,
        Long comentarioAutorId,
        String comentarioAutorApelido,
        Long denuncianteId,
        String denuncianteApelido,
        MotivoDenuncia motivo,
        String detalhes,
        StatusDenuncia status,
        LocalDateTime dataCriacao,
        LocalDateTime dataResolucao,
        String parecerModeracao
) {
    public static DenunciaComentarioResponse deEntidade(DenunciaComentario d) {
        return new DenunciaComentarioResponse(
                d.getId(),
                d.getComentario() != null ? d.getComentario().getId() : null,
                d.getComentario() != null ? d.getComentario().getTexto() : null,
                d.getComentario() != null && d.getComentario().getAutor() != null ? d.getComentario().getAutor().getId() : null,
                d.getComentario() != null && d.getComentario().getAutor() != null ? d.getComentario().getAutor().getApelidoOuEmail() : "Usuário Excluído",
                d.getUsuarioDenunciante() != null ? d.getUsuarioDenunciante().getId() : null,
                d.getUsuarioDenunciante() != null ? d.getUsuarioDenunciante().getApelidoOuEmail() : null,
                d.getMotivo(),
                d.getDetalhes(),
                d.getStatus(),
                d.getDataCriacao(),
                d.getDataResolucao(),
                d.getParecerModeracao()
        );
    }
}
