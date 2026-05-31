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

    @Value("${INCIDENT_EVALUATED_BINDING_KEY}")
    String INCIDENT_EVALUATED_BINDING_KEY;

    @Value("${AI_EVALUATION_QUEUE}")
    String AI_SERVICE_QUEUE;

    @Bean
    public TopicExchange topicExchange(){
        return new TopicExchange(EXCHANGE_NAME);
    }

    @Bean
    public Queue aiServiceEvalQueue(){
        return new Queue(AI_SERVICE_QUEUE,true);
    }


    @Bean
    public Binding bindAiService(){
        return BindingBuilder.
                bind(aiServiceEvalQueue())
                .to(topicExchange())
                .with(INCIDENT_EVALUATED_BINDING_KEY);
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
