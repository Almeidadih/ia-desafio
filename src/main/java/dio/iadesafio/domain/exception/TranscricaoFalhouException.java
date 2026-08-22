package dio.iadesafio.domain.exception;

public class TranscricaoFalhouException extends RuntimeException {
    public TranscricaoFalhouException(String message , Throwable cause) {
        super(message, cause);
    }
}
