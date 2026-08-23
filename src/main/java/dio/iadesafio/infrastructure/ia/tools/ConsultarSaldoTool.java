package dio.iadesafio.infrastructure.ia.tools;

import dio.iadesafio.application.dto.response.SaldoResponse;
import dio.iadesafio.application.service.TransacaoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

import java.util.Locale;

/**
 * Ferramenta exposta ao LLM para consultar o saldo atual
 * (total de receitas - total de despesas). O cache vive no
 * TransacaoService, nao aqui.
 */
@Service
public class ConsultarSaldoTool {

    private static final Logger log = LoggerFactory.getLogger(ConsultarSaldoTool.class);

    private final TransacaoService transacaoService;

    public ConsultarSaldoTool(TransacaoService transacaoService) {
        this.transacaoService = transacaoService;
    }

    @Tool(description = "Consulta o saldo atual do usuario (receitas menos despesas)")
    public String consultarSaldo() {
        SaldoResponse saldo = transacaoService.consultarSaldo();

        log.info("Consulta de saldo. receitas={}, despesas={}, saldo={}",
                saldo.receitas(), saldo.despesas(), saldo.saldo());

        return String.format(Locale.US, "Seu saldo atual e' de R$ %.2f (receitas: R$ %.2f, despesas: R$ %.2f)",
                saldo.saldo(), saldo.receitas(), saldo.despesas());
    }
}
