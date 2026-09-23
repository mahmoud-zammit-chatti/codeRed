package com.mahmoud.incident_service.IntegrationTesting;


import com.mahmoud.incident_service.DTOs.CreateIncidentRequestDTO;
import com.mahmoud.incident_service.DTOs.IncidentResponseDTO;
import com.mahmoud.incident_service.Models.incident.Type;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@Testcontainers
@ActiveProfiles("test")
@Import(IncidentServiceIntegrationTest.RabbitTestConfig.class)
public class IncidentServiceIntegrationTest {

    private static final String TEST_QUEUE_NAME = "incident.test.queue";
    private static final String TEST_ROUTING_KEY = "incident.created";

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:18")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");
    @Container
    @ServiceConnection
    static RabbitMQContainer rabbitMQContainer = new RabbitMQContainer("rabbitmq:3.11-management-alpine");

    @Autowired
    TestRestTemplate restTemplate;


    @Autowired
    AmqpTemplate amqpTemplate;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    DataSource dataSource;

    @TestConfiguration
    static class RabbitTestConfig {

        @Bean
        Queue testQueue() {
            return new Queue(TEST_QUEUE_NAME, true);
        }

        @Bean
        Binding testQueueBinding(Queue testQueue, org.springframework.amqp.core.DirectExchange directExchange) {
            return BindingBuilder.bind(testQueue).to(directExchange).with(TEST_ROUTING_KEY);
        }
    }




    @Test
    void printJdbcUrl() throws Exception {
        try (Connection c = dataSource.getConnection()) {
            System.out.println("JDBC URL used by DataSource = " + c.getMetaData().getURL());
        }
    }


    @Test
    public void testCorrelationIdPropagatesThroughMessageAndDb() throws Exception {
        CreateIncidentRequestDTO request = CreateIncidentRequestDTO.builder()
                .description("Test incident")
                .title("Test incident")
                .type(Type.OTHER)
                .location("test location")
                .reportedBy("12345678")
                .build();
        var response = restTemplate.postForEntity("/api/incidents/create", request, IncidentResponseDTO.class);
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isNotNull();
        System.out.println("Response: " + response);
        IncidentResponseDTO incident = response.getBody();

        Message received = amqpTemplate.receive(TEST_QUEUE_NAME, 10000);
        assertThat(received).isNotNull();
        String correlationId = received.getMessageProperties().getCorrelationId();
        if(correlationId==null){
            Object hdr = received.getMessageProperties().getHeaders().get("x-correlation-id");
            if(hdr!=null) correlationId = hdr.toString();
        }
        assertThat(correlationId).isNotNull();
        System.out.println("Correlation ID: " + correlationId);
        String sqlQuery = "SELECT correlation_id FROM outbox_event WHERE incident_ref = ?";
        List<String> dbCorrelationIds = jdbcTemplate.queryForList(sqlQuery, String.class, incident.getId());
        assertThat(dbCorrelationIds).isNotNull();

        String dbCorrelationId = dbCorrelationIds.get(0);
        System.out.println("DB Correlation ID: " + dbCorrelationId);
        assertThat(dbCorrelationId).isEqualTo(correlationId);
    }
}