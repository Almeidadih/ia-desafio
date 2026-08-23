package dio.iadesafio.infrastructure.messaging.listener;

import dio.iadesafio.application.service.OrcamentoService;
import dio.iadesafio.infrastructure.messaging.event.ComandoDeVozRecebidoEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Consome os eventos publicados pelo ComandoVozPublisher.
 * O container roda em Virtual Threads (ver RabbitMQConfig.rabbitListenerContainerFactory),
 * entao o metodo pode "bloquear" durante a chamada IA sem custo de thread real -
 * nao usamos @Async aqui de proposito, pois isso faria o container confirmar (ack)
 * a mensagem antes do processamento terminar, perdendo o controle de falha/backpressure.
 */
@Component
public class ComandoVozListener {
    private static final Logger log = LoggerFactory.getLogger(ComandoVozListener.class);

    private final OrcamentoService orcamentoService;

    public ComandoVozListener(OrcamentoService orcamentoService) {
        this.orcamentoService = orcamentoService;
    }

    @RabbitListener(queues = "${app.rabbitmq.queue}", containerFactory = "rabbitListenerContainerFactory")
    public void consumir(ComandoDeVozRecebidoEvent evento) {
        log.info("Evento consumido da fila. comandoId={}", evento.comandoId());
        orcamentoService.processar(evento.comandoId(), evento.caminhoArquivoTemporario());
    }
}
