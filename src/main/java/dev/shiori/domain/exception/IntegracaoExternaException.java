package dev.shiori.domain.exception;

import org.springframework.http.HttpStatus;

/**
 * Exceção lançada quando ocorre uma falha na comunicação ou resposta de um provedor de metadados externo.
 * Permite mapeamento limpo para ProblemDetail (RFC 9457) com status 502 (Bad Gateway) ou 504 (Gateway Timeout).
 */
public class IntegracaoExternaException extends RuntimeException {

    private final HttpStatus status;

    public IntegracaoExternaException(String mensagem) {
        this(mensagem, HttpStatus.BAD_GATEWAY);
    }

    public IntegracaoExternaException(String mensagem, HttpStatus status) {
        super(mensagem);
        this.status = status != null ? status : HttpStatus.BAD_GATEWAY;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
