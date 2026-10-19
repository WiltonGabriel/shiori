package dev.shiori.domain.enums;

public enum StatusLeitura {
    LENDO,
    ENGAVETADO,
    TERMINOU,
    PLANEJADO,
    PAUSADO,
    DROPOU,
    ABDICOU;

    @com.fasterxml.jackson.annotation.JsonCreator
    public static StatusLeitura fromString(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String normalizado = value.trim().toUpperCase();
        if ("DROPOU".equals(normalizado)) {
            return ABDICOU;
        }
        return StatusLeitura.valueOf(normalizado);
    }
}
