package dio.iadesafio.infrastructure.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executors;

/**
 * Declara a exchange/fila usadas para desacoplar o recebimento do audio
 * do processamento (transcricao + IA + persistencia).
 * Nomes configuraveis via application.yml (nunca hardcoded).
 */
@Configuration
public class RabbitMqConfig {

    @Value("${app.rabbitmq.exchange}")
    private String exchangeName;

    @Value("${app.rabbitmq.queue}")
    private String queueName;

    @Value("${app.rabbitmq.routing-key}")
    private String routingKey;

    @Bean
    public DirectExchange comandoVozExchange() {
        return new DirectExchange(exchangeName, true, false);
    }

    @Bean
    public Queue comandoVozQueue() {
        return new Queue(queueName, true);
    }

    @Bean
    public Binding comandoVozBinding(Queue comandoVozQueue, DirectExchange comandoVozExchange) {
        return BindingBuilder.bind(comandoVozQueue).to(comandoVozExchange).with(routingKey);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * Cada consumer roda em uma Virtual Thread: o metodo do listener pode
     * "bloquear" durante a chamada IA sem prender uma thread de plataforma,
     * mantendo o ack automatico apos o metodo terminar (garante que a
     * mensagem so seja confirmada quando o processamento realmente concluir).
     */
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            MessageConverter jsonMessageConverter
    ) {
        var factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter);
        factory.setTaskExecutor(Executors.newVirtualThreadPerTaskExecutor());
        factory.setConcurrentConsumers(4);
        return factory;

    }
