package dio.iadesafio.application.dto.response;

import dio.iadesafio.domain.model.Categoria;
import dio.iadesafio.domain.model.TipoTransacao;
import dio.iadesafio.domain.model.Transacao;

import java.math.BigDecimal;

public record TransacaoDTO(
        String id ,
        String descricao ,
        BigDecimal valor ,
        TipoTransacao tipo,
        Categoria categoria
) {
    public static TransacaoDTO de(Transacao transacao) {
        return new TransacaoDTO(
                transacao.getId().toString(),
                transacao.getDescricao(),
                transacao.getValor().quantia(),
                transacao.getTipo(),
                transacao.getCategoria()
        );
    }
}
