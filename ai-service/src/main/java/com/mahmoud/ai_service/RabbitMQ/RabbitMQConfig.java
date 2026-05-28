package com.mahmoud.ai_service.RabbitMQ;

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
    @Value("${INCIDENT_CREATION_QUEUE}")
    String INCIDENT_CREATION_QUEUE;
    @Value("${INCIDENT_CREATED_BINDING_KEY}")
    String INCIDENT_CREATED_BINDING_KEY;

    @Bean
    public TopicExchange topicExchange() {
        // Declares (or uses) a topic exchange with the given name
        return new TopicExchange(EXCHANGE_NAME);
    }

    @Bean
    public Queue incidentCreationQueue(){
        return new Queue(INCIDENT_CREATION_QUEUE,true);
    }

    @Bean
    public Binding bindIncidentCreationQueue(){
        return  BindingBuilder
                .bind(incidentCreationQueue())
                .to(topicExchange())
                .with(INCIDENT_CREATED_BINDING_KEY);
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
