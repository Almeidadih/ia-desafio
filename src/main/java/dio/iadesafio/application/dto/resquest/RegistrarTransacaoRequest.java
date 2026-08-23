package dio.iadesafio.application.dto.resquest;

import dio.iadesafio.domain.model.Categoria;
import dio.iadesafio.domain.model.TipoTransacao;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * Corpo aceito por POST /transacoes - registro direto via REST,
 * sem passar pelo pipeline de voz/IA (util para testes e integracoes).
 */
public record RegistrarTransacaoRequest(
        @NotBlank(message = "A descricao e obrigatoria")
        String descricao,

        @NotNull(message = "O valor e obrigatorio")
        @DecimalMin(value = "0.01", message = "O valor deve ser maior que zero")
        BigDecimal valor,

        @NotNull(message = "O tipo e obrigatorio (RECEITA ou DESPESA)")
        TipoTransacao tipo,

        @NotNull(message = "A categoria e obrigatoria")
        Categoria categoria
) {
}
