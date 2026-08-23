package dio.iadesafio.application.service;

import dio.iadesafio.application.dto.response.StatusComandoResponse;
import dio.iadesafio.domain.model.ComandoVoz;
import dio.iadesafio.domain.repository.ComandoVozRepository;
import dio.iadesafio.domain.voz.ComandoVozId;
import dio.iadesafio.infrastructure.ia.InterpretadorService;
import dio.iadesafio.infrastructure.ia.TranscricaoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Orquestra o pipeline completo apos o comando de voz ser consumido da fila:
 * transcricao -> interpretacao/tool calling -> atualizacao de status.
 * E' aqui que o ComandoVozListener delega o trabalho pesado.
 */
@Service
public class OrcamentoService {

    private static final Logger log = LoggerFactory.getLogger(OrcamentoService.class);

    private final TranscricaoService transcricaoService;
    private final InterpretadorService interpretadorService;
    private final ComandoVozRepository comandoVozRepository;
    private final StatusProcessamentoService statusProcessamentoService;

    public OrcamentoService(
            TranscricaoService transcricaoService,
            InterpretadorService interpretadorService,
            ComandoVozRepository comandoVozRepository,
            StatusProcessamentoService statusProcessamentoService
    ) {
        this.transcricaoService = transcricaoService;
        this.interpretadorService = interpretadorService;
        this.comandoVozRepository = comandoVozRepository;
        this.statusProcessamentoService = statusProcessamentoService;
    }

    @Transactional
    public void processar(String comandoIdStr, String caminhoArquivo) {
        ComandoVozId comandoId = ComandoVozId.de(comandoIdStr);
        log.info("Iniciando processamento do comando {}", comandoId);

        ComandoVoz comando = comandoVozRepository.findById(comandoId)
                .orElseGet(() -> ComandoVoz.receber(comandoId));

        comando.marcarProcessando();
        comandoVozRepository.save(comando);
        atualizarStatusRedis(comando, null);

        try {
            String textoTranscrito = transcricaoService.transcrever(caminhoArquivo);
            String respostaFinal = interpretadorService.interpretarEExecutar(textoTranscrito);

            comando.marcarConcluido(textoTranscrito);
            comandoVozRepository.save(comando);
            atualizarStatusRedis(comando, respostaFinal);

            log.info("Comando {} processado com sucesso", comandoId);
        } catch (Exception ex) {
            log.error("Erro ao processar comando {}", comandoId, ex);
            comando.marcarErro(ex.getMessage());
            comandoVozRepository.save(comando);
            atualizarStatusRedis(comando, null);
        } finally {
            removerArquivoTemporario(caminhoArquivo);
        }
    }

    private void removerArquivoTemporario(String caminhoArquivo) {
        try {
            Files.deleteIfExists(Path.of(caminhoArquivo));
        } catch (IOException ex) {
            log.warn("Nao foi possivel remover o arquivo temporario: {}", caminhoArquivo, ex);
        }
    }

    private void atualizarStatusRedis(ComandoVoz comando, String respostaIa) {
        var resposta = new StatusComandoResponse(
                comando.getId().toString(),
                comando.getStatus(),
                comando.getTextoTranscrito(),
                respostaIa,
                null, // pode ser enriquecido com a ultima transacao criada, se necessario
                comando.getMensagemErro()
        );
        statusProcessamentoService.salvar(comando.getId().toString(), resposta);
    }
}
