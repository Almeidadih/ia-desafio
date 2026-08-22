package dio.iadesafio.domain.exception;

public class ComandoVozNaoEncontradoException extends RuntimeException {
    public ComandoVozNaoEncontradoException(String comandoId) {
        super( "Comando de voz nao encontrado: "+ comandoId);
    }
}
