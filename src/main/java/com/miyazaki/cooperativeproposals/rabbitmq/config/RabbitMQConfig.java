package com.miyazaki.cooperativeproposals.rabbitmq.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.CustomExchange;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.retry.RejectAndDontRequeueRecoverer;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class RabbitMQConfig {
    public static final String EXCHANGE_DELAYED = "session.delayed";
    public static final String ROUTE_KEY_CLOSE  = "session.close";
    public static final String QUEUE_CLOSE  = "session.close.q";

    public static final String EXCHANGE_DLX     = "session.dlx";
    public static final String QUEUE_CLOSE_DLQ  = "session.close.dlq";

    @Bean
    public CustomExchange delayedExchange(){
        Map<String, Object> args = new HashMap<>();
        args.put("x-delayed-type", "topic");
        return new CustomExchange(EXCHANGE_DELAYED, "x-delayed-message", true, false, args);
    }

    @Bean
    public DirectExchange dlx() {
        return new DirectExchange(EXCHANGE_DLX);
    }

    @Bean
    public Queue closeQueue() {
        return QueueBuilder.durable(QUEUE_CLOSE)
                .withArgument("x-dead-letter-exchange", EXCHANGE_DLX)
                .withArgument("x-dead-letter-routing-key", ROUTE_KEY_CLOSE)
                .build();
    }

    @Bean
    public Queue closeDlq() {
        return QueueBuilder.durable(QUEUE_CLOSE_DLQ).build();
    }

    @Bean
    public Binding closeBinding(Queue closeQueue, CustomExchange delayedExchange) {
        return BindingBuilder.bind(closeQueue)
                .to(delayedExchange).with(ROUTE_KEY_CLOSE).noargs();
    }

    @Bean
    public Binding closeDlqBinding(Queue closeDlq, DirectExchange dlx) {
        return BindingBuilder.bind(closeDlq)
                .to(dlx).with(ROUTE_KEY_CLOSE);
    }

    @Bean
    public SimpleRabbitListenerContainerFactory listenerFactory(
            ConnectionFactory cf, Jackson2JsonMessageConverter conv) {

        var f = new SimpleRabbitListenerContainerFactory();
        f.setConnectionFactory(cf);
        f.setMessageConverter(conv);

        f.setDefaultRequeueRejected(false);
        f.setAdviceChain(
                RetryInterceptorBuilder.stateless()
                        .maxAttempts(4)
                        .maxAttempts(4)
                        .backOffOptions(1000, 2.0, 10000) // 1s -> 2s -> 4s
                        .recoverer(new RejectAndDontRequeueRecoverer())
                        .build()
        );
        return f;
    }

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory cf, Jackson2JsonMessageConverter conv) {
        RabbitTemplate rt = new RabbitTemplate(cf);
        rt.setMessageConverter(conv);
        return rt;
    }

}
