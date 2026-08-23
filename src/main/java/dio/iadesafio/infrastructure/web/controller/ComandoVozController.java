package dio.iadesafio.infrastructure.web.controller;

import dio.iadesafio.application.dto.response.ComandoVozResponse;
import dio.iadesafio.application.dto.response.StatusComandoResponse;
import dio.iadesafio.application.service.StatusProcessamentoService;
import dio.iadesafio.domain.model.ComandoVoz;
import dio.iadesafio.domain.repository.ComandoVozRepository;
import dio.iadesafio.domain.voz.ComandoVozId;
import dio.iadesafio.infrastructure.messaging.event.ComandoDeVozRecebidoEvent;
import dio.iadesafio.infrastructure.messaging.publisher.ComandoVozPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@RestController
@RequestMapping("/comandos")
public class ComandoVozController {

    private static final Logger log = LoggerFactory.getLogger(ComandoVozController.class);

    private final ComandoVozRepository comandoVozRepository;
    private final ComandoVozPublisher publisher;
    private final StatusProcessamentoService statusProcessamentoService;

    public ComandoVozController(
            ComandoVozRepository comandoVozRepository,
            ComandoVozPublisher publisher,
            StatusProcessamentoService statusProcessamentoService
    ) {
        this.comandoVozRepository = comandoVozRepository;
        this.publisher = publisher;
        this.statusProcessamentoService = statusProcessamentoService;
    }

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<ComandoVozResponse> receberAudio(@RequestParam("audio") MultipartFile audio) {
        ComandoVozId comandoId = ComandoVozId.novo();
        log.info("Audio recebido. comandoId={}, tamanho={} bytes", comandoId, audio.getSize());

        String caminhoArquivo = salvarArquivoTemporario(audio, comandoId);

        ComandoVoz comando = ComandoVoz.receber(comandoId);
        comandoVozRepository.save(comando);

        publisher.publicar(new ComandoDeVozRecebidoEvent(comandoId.toString(), caminhoArquivo));

        return ResponseEntity.accepted().body(ComandoVozResponse.recebido(comandoId.toString()));
    }

    @GetMapping("/{comandoId}/status")
    public ResponseEntity<StatusComandoResponse> consultarStatus(@PathVariable String comandoId) {
        StatusComandoResponse status = statusProcessamentoService.buscar(comandoId);
        return ResponseEntity.ok(status);
    }

    private String salvarArquivoTemporario(MultipartFile audio, ComandoVozId comandoId) {
        try {
            String extensao = extrairExtensaoSegura(audio.getOriginalFilename());
            Path destino = Files.createTempFile("orcamento-voz-" + comandoId + "-", extensao);
            Files.copy(audio.getInputStream(), destino, StandardCopyOption.REPLACE_EXISTING);
            return destino.toString();
        } catch (IOException ex) {
            log.error("Falha ao salvar arquivo de audio temporario", ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Falha ao processar o audio enviado");
        }
    }

    /**
     * Extrai apenas a extensao do nome original (ex: ".mp3"), nunca o nome inteiro -
     * o nome enviado pelo cliente e' entrada nao confiavel e pode conter caminhos
     * ("../../etc/passwd") ou caracteres invalidos; o Files.createTempFile acima
     * gera o nome real do arquivo, evitando qualquer risco de path traversal.
     */
    private String extrairExtensaoSegura(String nomeOriginal) {
        if (nomeOriginal == null) {
            return ".audio";
        }
        int pontoIdx = nomeOriginal.lastIndexOf('.');
        if (pontoIdx < 0 || pontoIdx == nomeOriginal.length() - 1) {
            return ".audio";
        }
        String extensao = nomeOriginal.substring(pontoIdx).replaceAll("[^a-zA-Z0-9.]", "");
        return extensao.isBlank() ? ".audio" : extensao;
    }
}
