package dio.iadesafio.infrastructure.ia;

import dio.iadesafio.infrastructure.ia.tools.ConsultarResumoPeriodoTool;
import dio.iadesafio.infrastructure.ia.tools.ConsultarSaldoTool;
import dio.iadesafio.infrastructure.ia.tools.RegistrarTransacaoTool;
import dio.iadesafio.domain.exception.IntencaoNaoReconhecidaException;
import dio.iadesafio.infrastructure.ia.tools.ConsultarGastoPorCategoriaTool;
import dio.iadesafio.infrastructure.ia.tools.ConsultarHistoricoTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

/**
 * Interpreta o texto transcrito e decide (via Tool Calling) qual acao real
 * da aplicacao deve ser executada: registrar transacao, consultar saldo,
 * historico, gasto por categoria ou resumo por periodo.
 */
@Service
public class InterpretadorService {
    private static final Logger log = LoggerFactory.getLogger(InterpretadorService.class);

    private final ChatClient chatClient;
    private final RegistrarTransacaoTool registrarTransacaoTool;
    private final ConsultarSaldoTool consultarSaldoTool;
    private final ConsultarHistoricoTool consultarHistoricoTool;
    private final ConsultarGastoPorCategoriaTool consultarGastoPorCategoriaTool;
    private final ConsultarResumoPeriodoTool consultarResumoPeriodoTool;

    public InterpretadorService(
            ChatClient chatClient,
            RegistrarTransacaoTool registrarTransacaoTool,
            ConsultarSaldoTool consultarSaldoTool,
            ConsultarHistoricoTool consultarHistoricoTool,
            ConsultarGastoPorCategoriaTool consultarGastoPorCategoriaTool,
            ConsultarResumoPeriodoTool consultarResumoPeriodoTool
    ) {
        this.chatClient = chatClient;
        this.registrarTransacaoTool = registrarTransacaoTool;
        this.consultarSaldoTool = consultarSaldoTool;
        this.consultarHistoricoTool = consultarHistoricoTool;
        this.consultarGastoPorCategoriaTool = consultarGastoPorCategoriaTool;
        this.consultarResumoPeriodoTool = consultarResumoPeriodoTool;
    }

    public String interpretarEExecutar(String textoTranscrito) {
        log.debug("Interpretando comando: {}", textoTranscrito);

        if (textoTranscrito == null || textoTranscrito.isBlank()) {
            throw new IntencaoNaoReconhecidaException(textoTranscrito);
        }

        try {
            String resposta = chatClient.prompt()
                    .user(textoTranscrito)
                    .tools(
                            registrarTransacaoTool,
                            consultarSaldoTool,
                            consultarHistoricoTool,
                            consultarGastoPorCategoriaTool,
                            consultarResumoPeriodoTool
                    )
                    .call()
                    .content();

            log.info("Comando interpretado e executado com sucesso");
            return resposta;
        } catch (Exception ex) {
            log.error("Falha ao interpretar comando: {}", textoTranscrito, ex);
            throw new IntencaoNaoReconhecidaException(textoTranscrito, ex);
        }
    }
}
