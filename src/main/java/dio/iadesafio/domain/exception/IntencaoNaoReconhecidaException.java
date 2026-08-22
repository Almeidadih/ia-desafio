package dio.iadesafio.domain.exception;

public class IntencaoNaoReconhecidaException extends RuntimeException {
    public IntencaoNaoReconhecidaException(String textoTranscrito) {
        super("Nao foi possivel entender o comando: \"" + textoTranscrito + "\"");
    }

    public IntencaoNaoReconhecidaException(String textoTranscrito, Throwable causa) {
        super("Nao foi possivel entender o comando: \"" + textoTranscrito + "\"", causa);
    }
}
