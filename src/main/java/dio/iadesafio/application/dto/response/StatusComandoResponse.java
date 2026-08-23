package dio.iadesafio.application.dto.response;

import dio.iadesafio.domain.model.StatusComando;

public record StatusComandoResponse(
        String comandoId,
        StatusComando status ,
        String textoTranscrito,
        String respostaIa,
        TransacaoDTO transacao,
        String mensagemErro
) {
}
