package dio.iadesafio.infrastructure.ia.tools;

import dio.iadesafio.domain.model.Categoria;
import dio.iadesafio.infrastructure.ia.TranscricaoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Locale;

@Component
public class ConsultarGastoPorCategoriaTool {
    private static final Logger log = LoggerFactory.getLogger(ConsultarGastoPorCategoriaTool.class);

    private final TranscricaoService transacaoService;

    public ConsultarGastoPorCategoriaTool(TranscricaoService transacaoService) {
        this.transacaoService = transacaoService;
    }

    @Tool(description = "Consulta o total gasto (despesas) em uma categoria especifica, ex: quanto gastei com transporte")
    public String consultarGastoPorCategoria(
            @ToolParam(description = "Categoria: ALIMENTACAO, TRANSPORTE, MORADIA, SAUDE, LAZER, EDUCACAO, SALARIO ou OUTROS")
            String categoria
    ) {
        Categoria categoriaEnum = Categoria.valueOf(categoria.toUpperCase());
        log.info("Tool consultarGastoPorCategoria chamada. categoria={}", categoriaEnum);

        BigDecimal total = transacaoService.consultar

        return String.format(Locale.US, "Voce gastou R$ %.2f na categoria %s", total, categoriaEnum);
    }
}
