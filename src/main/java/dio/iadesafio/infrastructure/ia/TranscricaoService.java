package dio.iadesafio.infrastructure.ia;

import dio.iadesafio.domain.exception.TranscricaoFalhouException;
import org.slf4j.Logger;

import org.slf4j.LoggerFactory;
import org.springframework.ai.audio.transcription.AudioTranscriptionPrompt;
import org.springframework.ai.openai.OpenAiAudioTranscriptionModel;
import org.springframework.ai.openai.OpenAiAudioTranscriptionOptions;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;

import java.io.File;

/**
 * Converte o audio recebido em texto usando o modelo Whisper via Spring AI.
 */

@Service
public class TranscricaoService {

    private static final Logger log = LoggerFactory.getLogger(TranscricaoService.class);

    private final OpenAiAudioTranscriptionModel transcriptionModel;

    public TranscricaoService(OpenAiAudioTranscriptionModel transcriptionModel) {
        this.transcriptionModel = transcriptionModel;
    }
    public String transcrever(String caminhoArquivo) {
        log.debug("Iniciando transcricao do arquivo: {}", caminhoArquivo);
        try {
            var recurso = new FileSystemResource(new File(caminhoArquivo));
            var opcoes = OpenAiAudioTranscriptionOptions.builder()
                    .language("pt")
                    .build();

            var resposta = transcriptionModel.call(new AudioTranscriptionPrompt(recurso, opcoes));
            String texto = resposta.getResult().getOutput();

            log.info("Transcricao concluida. Tamanho do texto: {} caracteres", texto.length());
            return texto;
        } catch (Exception ex) {
            log.error("Falha ao transcrever audio: {}", caminhoArquivo, ex);
            throw new TranscricaoFalhouException("Erro ao transcrever o audio", ex);
        }
    }
}
