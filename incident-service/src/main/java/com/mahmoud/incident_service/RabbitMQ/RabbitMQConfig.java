package com.mahmoud.incident_service.RabbitMQ;


import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableRabbit
public class RabbitMQConfig {

    @Value("${EXCHANGE_NAME}")
    String EXCHANGE_NAME;


    @Value("${INCIDENT_CLASSIFIED_BINDING_KEY}")
    String INCIDENT_CLASSIFIED_BINDING_KEY;


    @Value("${AI_CLASSIFICATION_QUEUE}")
    String AI_CLASSIFICATION_QUEUE;

    @Bean
    public DirectExchange directExchange(){
        return new DirectExchange(EXCHANGE_NAME);
    }

    @Bean
    public Queue aiServiceClasificationQueue(){
        return new Queue(AI_CLASSIFICATION_QUEUE,true);
    }


    @Bean
    public Binding bindAiService(){
        return BindingBuilder.
                bind(aiServiceClasificationQueue())
                .to(directExchange())
                .with(INCIDENT_CLASSIFIED_BINDING_KEY);
    }



    @Bean
    public MessageConverter messageConverter(){
        return new JacksonJsonMessageConverter();
    }

    @Bean
    public AmqpTemplate amqpTemplate (ConnectionFactory connectionFactory){
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter());
        return rabbitTemplate;
    }

}
