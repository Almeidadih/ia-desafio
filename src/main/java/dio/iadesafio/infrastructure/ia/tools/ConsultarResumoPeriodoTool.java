package dio.iadesafio.infrastructure.ia.tools;

import dio.iadesafio.application.dto.response.ResumoPeriodoResponse;
import dio.iadesafio.application.service.TransacaoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.util.Locale;

/**
 * Ferramenta exposta ao LLM para resumir receitas/despesas/saldo
 * dos ultimos N dias, ex: "como estao meus gastos essa semana?".
 */
@Service
public class ConsultarResumoPeriodoTool {
    private static final Logger log = LoggerFactory.getLogger(ConsultarResumoPeriodoTool.class);

    private final TransacaoService transacaoService;

    public ConsultarResumoPeriodoTool(TransacaoService transacaoService) {
        this.transacaoService = transacaoService;
    }

    @Tool(description = "Resume receitas, despesas e saldo dos ultimos N dias, ex: 'como estao meus gastos essa semana'")
    public String consultarResumoPeriodo(
            @ToolParam(description = "Quantidade de dias pra tras a considerar, ex: 7 para a ultima semana, 30 para o ultimo mes")
            int dias
    ) {
        log.info("Tool consultarResumoPeriodo chamada. dias={}", dias);

        ResumoPeriodoResponse resumo = transacaoService.consultarResumoPeriodo(dias);

        return String.format(Locale.US,
                "Nos ultimos %d dias: receitas de R$ %.2f, despesas de R$ %.2f, saldo de R$ %.2f",
                resumo.dias(), resumo.receitas(), resumo.despesas(), resumo.saldo());
    }
}
