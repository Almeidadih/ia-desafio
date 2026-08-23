package dio.iadesafio.infrastructure.ia.tools;

import dio.iadesafio.application.service.TransacaoService;
import dio.iadesafio.domain.model.Categoria;
import dio.iadesafio.domain.model.Transacao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Ferramenta exposta ao LLM para listar as transacoes mais recentes,
 * opcionalmente filtradas por categoria.
 */
@Component
public class ConsultarHistoricoTool {

    private static final Logger log = LoggerFactory.getLogger(ConsultarHistoricoTool.class);

    private final TransacaoService transacaoService;

    public ConsultarHistoricoTool(TransacaoService transacaoService) {
        this.transacaoService = transacaoService;
    }

    @Tool(description = "Lista as transacoes mais recentes do usuario, opcionalmente filtradas por categoria")
    public String consultarHistorico(
            @ToolParam(description = "Categoria para filtrar (ALIMENTACAO, TRANSPORTE, MORADIA, SAUDE, LAZER, "
                    + "EDUCACAO, SALARIO, OUTROS). Deixe vazio para nao filtrar", required = false) String categoria,
            @ToolParam(description = "Quantidade maxima de transacoes a retornar, padrao 5", required = false) Integer limite
    ) {
        Categoria categoriaFiltro = (categoria == null || categoria.isBlank())
                ? null
                : Categoria.valueOf(categoria.toUpperCase());
        int limiteFinal = (limite == null) ? 5 : limite;

        log.info("Tool consultarHistorico chamada. categoria={}, limite={}", categoriaFiltro, limiteFinal);

        List<Transacao> transacoes = transacaoService.consultarHistorico(categoriaFiltro, limiteFinal);

        if (transacoes.isEmpty()) {
            return "Nenhuma transacao encontrada" + (categoriaFiltro != null ? " na categoria " + categoriaFiltro : "") + ".";
        }

        return transacoes.stream()
                .map(t -> String.format(Locale.US, "- %s: R$ %.2f (%s, %s)",
                        t.getDescricao(), t.getValor().quantia(), t.getTipo(), t.getCategoria()))
                .collect(Collectors.joining("\n"));
    }
}
