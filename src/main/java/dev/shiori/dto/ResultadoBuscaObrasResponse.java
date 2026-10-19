package dev.shiori.dto;

import java.util.List;
import java.util.Set;

import dev.shiori.domain.enums.TipoObra;

/**
 * Resposta inteligente de busca assistida no catálogo.
 * Se nenhuma obra for encontrada dentro dos formatos permitidos pelo usuário,
 * mas existirem correspondências em outros formatos literários desativados,
 * oculta capas e cards, retornando apenas os formatos detectados e uma mensagem contextual amigável.
 */
public record ResultadoBuscaObrasResponse(
        List<ObraResponse> obras,
        boolean correspondenciasEmOutrosFormatos,
        Set<TipoObra> formatosDetectadosNaoPermitidos,
        String mensagemContextual
) {

    public static ResultadoBuscaObrasResponse comResultados(List<ObraResponse> obras) {
        return new ResultadoBuscaObrasResponse(obras, false, Set.of(), null);
    }

    public static ResultadoBuscaObrasResponse avisoOutrosFormatos(Set<TipoObra> formatosNaoPermitidos, String mensagem) {
        return new ResultadoBuscaObrasResponse(List.of(), true, formatosNaoPermitidos, mensagem);
    }

    public static ResultadoBuscaObrasResponse vazio() {
        return new ResultadoBuscaObrasResponse(List.of(), false, Set.of(), null);
    }
}
