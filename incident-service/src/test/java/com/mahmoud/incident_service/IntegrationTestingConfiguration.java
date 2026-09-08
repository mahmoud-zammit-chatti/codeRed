package com.mahmoud.incident_service;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.springframework.amqp.core.AmqpTemplate;
import org.mockito.Mockito;


@TestConfiguration
public class IntegrationTestingConfiguration {

    @Bean
    @ServiceConnection
    PostgreSQLContainer<?> postgresTestContainer() {
        return new PostgreSQLContainer<>("postgres:18-alpine");
    }
    @Bean
    AmqpTemplate amqpTemplateMock() {
        return Mockito.mock(AmqpTemplate.class);
    }

}
