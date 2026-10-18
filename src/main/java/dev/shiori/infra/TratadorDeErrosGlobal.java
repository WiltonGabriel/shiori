package dev.shiori.infra;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import dev.shiori.domain.exception.RecursoNaoEncontradoException;
import dev.shiori.domain.exception.RegraDeNegocioException;

@RestControllerAdvice
public class TratadorDeErrosGlobal {

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<DadosErro> tratar404(RecursoNaoEncontradoException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new DadosErro(ex.getMessage()));
    }

    @ExceptionHandler(RegraDeNegocioException.class)
    public ResponseEntity<DadosErro> tratar400(RegraDeNegocioException ex) {
        return ResponseEntity.badRequest()
                .body(new DadosErro(ex.getMessage()));
    }

    public record DadosErro(String mensagem) {
    }
}
