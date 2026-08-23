package dio.iadesafio.infrastructure.web.exception;

import dio.iadesafio.domain.exception.ComandoVozNaoEncontradoException;
import dio.iadesafio.domain.exception.IntencaoNaoReconhecidaException;
import dio.iadesafio.domain.exception.TransacaoInvalidaException;
import dio.iadesafio.domain.exception.TranscricaoFalhouException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ComandoVozNaoEncontradoException.class)
    public ResponseEntity<Object> tratarNaoEncontrado(ComandoVozNaoEncontradoException ex) {
        log.warn("Comando nao encontrado: {}", ex.getMessage());
        return responder(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(TranscricaoFalhouException.class)
    public ResponseEntity<Object> tratarFalhaTranscricao(TranscricaoFalhouException ex) {
        log.error("Falha na transcricao de audio", ex);
        return responder(HttpStatus.UNPROCESSABLE_ENTITY, "Nao foi possivel transcrever o audio enviado.");
    }

    @ExceptionHandler(IntencaoNaoReconhecidaException.class)
    public ResponseEntity<Object> tratarIntencaoNaoReconhecida(IntencaoNaoReconhecidaException ex) {
        log.warn("Intencao nao reconhecida: {}", ex.getMessage());
        return responder(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
    }

    @ExceptionHandler(TransacaoInvalidaException.class)
    public ResponseEntity<Object> tratarTransacaoInvalida(TransacaoInvalidaException ex) {
        log.warn("Transacao invalida: {}", ex.getMessage());
        return responder(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    /**
     * Disparada quando um @Valid no corpo da requisicao (ex: RegistrarTransacaoRequest)
     * falha - devolve os campos invalidos em vez de uma mensagem generica.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> tratarValidacaoInvalida(MethodArgumentNotValidException ex) {
        Map<String, String> erros = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(erro ->
                erros.put(erro.getField(), erro.getDefaultMessage()));

        log.warn("Validacao falhou: {}", erros);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "timestamp", LocalDateTime.now().toString(),
                "status", HttpStatus.BAD_REQUEST.value(),
                "mensagem", "Dados invalidos",
                "campos", erros
        ));
    }

    /**
     * Corpo JSON malformado ou enum invalido (ex: "tipo": "compra" quando so
     * RECEITA/DESPESA sao aceitos) - sem isso, a Jackson exception vazaria
     * como 500 generico.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Object> tratarCorpoInvalido(HttpMessageNotReadableException ex) {
        log.warn("Corpo da requisicao invalido: {}", ex.getMessage());
        return responder(HttpStatus.BAD_REQUEST, "Corpo da requisicao invalido ou com valor nao aceito.");
    }

    /**
     * @PathVariable/@RequestParam que nao bate com o tipo esperado, ex:
     * /transacoes/categoria/invalida/total quando "invalida" nao e' uma Categoria valida.
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Object> tratarParametroInvalido(MethodArgumentTypeMismatchException ex) {
        String mensagem = "Valor invalido para o parametro '" + ex.getName() + "': " + ex.getValue();
        log.warn(mensagem);
        return responder(HttpStatus.BAD_REQUEST, mensagem);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Object> tratarArgumentoInvalido(IllegalArgumentException ex) {
        log.warn("Argumento invalido: {}", ex.getMessage());
        return responder(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> tratarErroGenerico(Exception ex) {
        log.error("Erro inesperado", ex);
        return responder(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno inesperado.");
    }

    private ResponseEntity<Object> responder(HttpStatus status, String mensagem) {
        return ResponseEntity.status(status).body(Map.of(
                "timestamp", LocalDateTime.now().toString(),
                "status", status.value(),
                "mensagem", mensagem
        ));
    }
}
