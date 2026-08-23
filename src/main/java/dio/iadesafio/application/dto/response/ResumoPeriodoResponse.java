package dio.iadesafio.application.dto.response;

import java.math.BigDecimal;

public record ResumoPeriodoResponse(
        int dias ,
        BigDecimal receitas ,
        BigDecimal despesas ,
        BigDecimal saldo
) {
}
