package com.miyazaki.cooperativeproposals.rabbitmq.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.CustomExchange;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.retry.RejectAndDontRequeueRecoverer;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Configuration
@Slf4j
public class RabbitMQConfig {
    public static final String EXCHANGE_DELAYED = "session.delayed";
    public static final String ROUTE_KEY_CLOSE  = "session.close";
    public static final String QUEUE_CLOSE  = "session.close.q";

    public static final String EXCHANGE_DLX     = "session.dlx";
    public static final String QUEUE_CLOSE_DLQ  = "session.close.dlq";
    
    private static final int RETRY_INITIAL_INTERVAL = 1000;
    private static final int RETRY_MAX_INTERVAL = 10000;

    /**
     * Creates a delayed exchange for session messages.
     * This exchange allows messages to be delayed before being delivered.
     *
     * @return the configured CustomExchange
     */
    @Bean
    public CustomExchange delayedExchange() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-delayed-type", "topic");
        return new CustomExchange(EXCHANGE_DELAYED, "x-delayed-message", true, false, args);
    }

    /**
     * Creates a dead letter exchange for failed messages.
     *
     * @return the configured DirectExchange
     */
    @Bean
    public DirectExchange dlx() {
        return new DirectExchange(EXCHANGE_DLX);
    }

    /**
     * Creates the main queue for session close messages.
     * Configured with dead letter exchange for failed messages.
     *
     * @return the configured Queue
     */
    @Bean
    public Queue closeQueue() {
        return QueueBuilder.durable(QUEUE_CLOSE)
                .withArgument("x-dead-letter-exchange", EXCHANGE_DLX)
                .withArgument("x-dead-letter-routing-key", ROUTE_KEY_CLOSE)
                .build();
    }

    /**
     * Creates the dead letter queue for failed session close messages.
     *
     * @return the configured Queue
     */
    @Bean
    public Queue closeDlq() {
        return QueueBuilder.durable(QUEUE_CLOSE_DLQ).build();
    }

    /**
     * Creates binding between the close queue and delayed exchange.
     *
     * @param closeQueue the queue to bind
     * @param delayedExchange the exchange to bind to
     * @return the configured Binding
     */
    @Bean
    public Binding closeBinding(final Queue closeQueue, final CustomExchange delayedExchange) {
        return BindingBuilder.bind(closeQueue)
                .to(delayedExchange).with(ROUTE_KEY_CLOSE).noargs();
    }

    /**
     * Creates binding between the dead letter queue and dead letter exchange.
     *
     * @param closeDlq the dead letter queue to bind
     * @param dlx the dead letter exchange to bind to
     * @return the configured Binding
     */
    @Bean
    public Binding closeDlqBinding(final Queue closeDlq, final DirectExchange dlx) {
        return BindingBuilder.bind(closeDlq)
                .to(dlx).with(ROUTE_KEY_CLOSE);
    }

    /**
     * Creates the listener container factory with retry configuration.
     * Configures retry attempts, backoff strategy, and concurrency settings.
     *
     * @param cf the connection factory
     * @param conv the message converter
     * @return the configured SimpleRabbitListenerContainerFactory
     */
    @Bean
    public SimpleRabbitListenerContainerFactory listenerFactory(
            final ConnectionFactory cf, final Jackson2JsonMessageConverter conv) {

        var f = new SimpleRabbitListenerContainerFactory();
        f.setConnectionFactory(cf);
        f.setMessageConverter(conv);

        f.setDefaultRequeueRejected(false);
        
        f.setAdviceChain(
                RetryInterceptorBuilder.stateless()
                        .maxAttempts(4) 
                        .backOffOptions(RETRY_INITIAL_INTERVAL, 2.0, RETRY_MAX_INTERVAL)
                        .recoverer(new RejectAndDontRequeueRecoverer())
                        .build()
        );
        
        f.setConcurrentConsumers(1);
        f.setMaxConcurrentConsumers(1);
        
        return f;
    }

    /**
     * Creates the JSON message converter for RabbitMQ messages.
     *
     * @return the configured Jackson2JsonMessageConverter
     */
    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * Creates the RabbitTemplate for sending messages.
     * Configured with JSON message converter.
     *
     * @param cf the connection factory
     * @param conv the message converter
     * @return the configured RabbitTemplate
     */
    @Bean

    public RabbitTemplate rabbitTemplate(final ConnectionFactory cf, final Jackson2JsonMessageConverter conv) {
        RabbitTemplate rt = new RabbitTemplate(cf);
        rt.setMessageConverter(conv);
        return rt;
    }

}
