package dio.iadesafio.infrastructure.messaging.event;

import java.io.Serializable;

public record ComandoDeVozRecebidoEvent(
        String comandoId,
        String caminhoArquivoTemporario
) implements Serializable {
}
