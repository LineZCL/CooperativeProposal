package com.miyazaki.cooperativeproposals.rabbitmq.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.CustomExchange;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
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

    @Bean
    public CustomExchange delayedExchange(){
        Map<String, Object> args = new HashMap<>();
        args.put("x-delayed-type", "topic");
        return new CustomExchange(EXCHANGE_DELAYED, "x-delayed-message", true, false, args);
    }

    @Bean
    public Queue closeQueue() {
        return new Queue(QUEUE_CLOSE, true);
    }

    @Bean
    public Binding closeBinding(Queue fecharQueue, CustomExchange delayedExchange) {
        return BindingBuilder.bind(fecharQueue).to(delayedExchange).with(ROUTE_KEY_CLOSE).noargs();
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
