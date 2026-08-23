package dio.iadesafio.application.dto.response;

import java.math.BigDecimal;

public record SaldoResponse(
        BigDecimal receitas ,
        BigDecimal despesas ,
        BigDecimal saldo
) {
}
