package dio.iadesafio.infrastructure.messaging.publisher;

import dio.iadesafio.infrastructure.messaging.event.ComandoDeVozRecebidoEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ComandoVozPublisher {

    private static final Logger log = LoggerFactory.getLogger(ComandoVozPublisher.class);

    private final RabbitTemplate rabbitTemplate;
    private final String exchange;
    private final String routingKey;

    public ComandoVozPublisher(
            RabbitTemplate rabbitTemplate,
            @Value("${app.rabbitmq.exchange}") String exchange,
            @Value("${app.rabbitmq.routing-key}") String routingKey
    ) {
        this.rabbitTemplate = rabbitTemplate;
        this.exchange = exchange;
        this.routingKey = routingKey;
    }

    public void publicar(ComandoDeVozRecebidoEvent evento) {
        log.info("Publicando evento de comando de voz recebido. comandoId={}", evento.comandoId());
        rabbitTemplate.convertAndSend(exchange, routingKey, evento);
    }
}
