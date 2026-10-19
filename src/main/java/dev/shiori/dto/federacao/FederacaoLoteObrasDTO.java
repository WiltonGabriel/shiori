package dev.shiori.dto.federacao;

import java.util.List;

public record FederacaoLoteObrasDTO(
        String origemNo,
        int hopsRestantes,
        List<ObraFederadaItemDTO> obras
) {}
