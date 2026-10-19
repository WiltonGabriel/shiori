package dev.shiori.dto.catalogo;

import dev.shiori.domain.enums.StatusLeitura;
import dev.shiori.domain.enums.Visibilidade;
import jakarta.validation.constraints.NotNull;

public record AdicionarItemCatalogoRequest(
        @NotNull(message = "O ID da obra é obrigatório")
        Long obraId,

        Visibilidade visibilidade,

        StatusLeitura statusLeitura,

        String urlLeitura
) {
    public Visibilidade visibilidadeOuPadrao() {
        return visibilidade != null ? visibilidade : Visibilidade.RESTRITO;
    }

    public StatusLeitura statusLeituraOuPadrao() {
        return statusLeitura != null ? statusLeitura : StatusLeitura.LENDO;
    }
}
