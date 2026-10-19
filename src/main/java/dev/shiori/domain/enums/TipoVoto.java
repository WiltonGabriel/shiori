package dev.shiori.domain.enums;

public enum TipoVoto {
    UPVOTE(1),
    DOWNVOTE(-1);

    private final int valor;

    TipoVoto(int valor) {
        this.valor = valor;
    }

    public int getValor() {
        return valor;
    }
}
