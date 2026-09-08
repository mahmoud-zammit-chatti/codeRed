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
    @Value("${INCIDENT_DLX_EXCHANGE_NAME}")
    String INCIDENT_DLX_EXCHANGE_NAME;
    @Value("${INCIDENT_DLX_ROUTING_KEY}")
    String INCIDENT_DLX_ROUTING_KEY;
    @Value("${INCIDENT_DLX_QUEUE_NAME}")
    String INCIDENT_DLX_QUEUE_NAME;


    @Bean
    public DirectExchange directExchange() {
        // Declares (or uses) a direct exchange with the given name, in this case it uses it because incident service is responsible for the exchange
        return new DirectExchange(EXCHANGE_NAME);
    }

    @Bean
    public DirectExchange directDlxExchange() {
        return new DirectExchange(INCIDENT_DLX_EXCHANGE_NAME);
    }

    @Bean

    public Queue incidentCreationQueue(){
        return QueueBuilder.durable(INCIDENT_CREATION_QUEUE)
                .withArgument("x-dead-letter-exchange", INCIDENT_DLX_EXCHANGE_NAME)
                .withArgument("x-dead-letter-routing-key", INCIDENT_DLX_ROUTING_KEY)
                .build();
    }

    @Bean
    public Queue incidentDLXQueue(){
        return QueueBuilder.durable(INCIDENT_DLX_QUEUE_NAME).build();
    }



    @Bean
    public Binding bindIncidentDlxQueue(){
        return  BindingBuilder
                .bind(incidentDLXQueue())
                .to(directDlxExchange())
                .with(INCIDENT_DLX_ROUTING_KEY);
    }

    @Bean
    public Binding bindIncidentCreationQueue(){
        return  BindingBuilder
                .bind(incidentCreationQueue())
                .to(directExchange())
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
