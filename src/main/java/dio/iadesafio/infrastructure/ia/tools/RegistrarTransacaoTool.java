package dio.iadesafio.infrastructure.ia.tools;

import dio.iadesafio.application.service.TransacaoService;
import dio.iadesafio.domain.model.Categoria;
import dio.iadesafio.domain.model.TipoTransacao;
import dio.iadesafio.domain.voz.Valor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.Locale;

/**
 * Ferramenta exposta ao LLM (Tool Calling) para registrar uma nova
 * transacao financeira a partir da intencao interpretada do comando de voz.
 * A regra de negocio e a invalidacao do cache de saldo vivem no
 * TransacaoService - esta classe so traduz a chamada do LLM em texto.
 */
@Component
public class RegistrarTransacaoTool {


    private static final Logger log = LoggerFactory.getLogger(RegistrarTransacaoTool.class);

    private final TransacaoService transacaoService;

    public RegistrarTransacaoTool(TransacaoService transacaoService) {
        this.transacaoService = transacaoService;
    }

    @Tool(description = "Registra uma nova transacao financeira (receita ou despesa) informada pelo usuario")
    public String registrar(
            @ToolParam(description = "Descricao curta da transacao, ex: 'mercado', 'salario'") String descricao,
            @ToolParam(description = "Valor monetario da transacao, ex: 50.00") double valor,
            @ToolParam(description = "Tipo: RECEITA ou DESPESA") String tipo,
            @ToolParam(description = "Categoria: ALIMENTACAO, TRANSPORTE, MORADIA, SAUDE, LAZER, EDUCACAO, SALARIO ou OUTROS") String categoria
    ) {
        log.info("Tool registrar chamada. descricao={}, valor={}, tipo={}, categoria={}",
                descricao, valor, tipo, categoria);

        transacaoService.registrar(
                descricao,
                Valor.de(valor),
                TipoTransacao.valueOf(tipo.toUpperCase()),
                Categoria.valueOf(categoria.toUpperCase())
        );

        return String.format(Locale.US, "Transacao registrada com sucesso: %s de R$ %.2f em %s",
                tipo.toLowerCase(), valor, descricao);
    }
}
