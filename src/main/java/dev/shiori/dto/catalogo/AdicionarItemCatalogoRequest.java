package dev.shiori.dto.catalogo;

import dev.shiori.domain.enums.StatusLeitura;
import dev.shiori.domain.enums.TipoObra;
import dev.shiori.domain.enums.Visibilidade;

public record AdicionarItemCatalogoRequest(
        Long obraId,
        String tituloPersonalizado,
        String capaPersonalizada,
        TipoObra tipoObra,
        Double capituloInicial,
        Double volumeInicial,
        Integer totalCapitulosEsperados,
        Integer totalVolumesEsperados,
        Visibilidade visibilidade,
        StatusLeitura statusLeitura,
        String urlLeitura,
        Boolean sincronizarMetadados
) {
    public AdicionarItemCatalogoRequest(Long obraId, Visibilidade visibilidade, StatusLeitura statusLeitura, String urlLeitura) {
        this(obraId, null, null, null, null, null, null, null, visibilidade, statusLeitura, urlLeitura, true);
    }

    public Visibilidade visibilidadeOuPadrao() {
        return visibilidade != null ? visibilidade : Visibilidade.RESTRITO;
    }

    public StatusLeitura statusLeituraOuPadrao() {
        return statusLeitura != null ? statusLeitura : StatusLeitura.LENDO;
    }
}

