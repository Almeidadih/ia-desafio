package dio.iadesafio.application.dto.response;

import dio.iadesafio.domain.model.StatusComando;

public record ComandoVozResponse (
        String comandoId,
        StatusComando status,
        String mensagem
) {

    public static ComandoVozResponse recebido(String comandoId){
        return new ComandoVozResponse(comandoId, StatusComando.RECEBIDO,
                "Comando recebido , processando em segundo plano. ");
    }
}
