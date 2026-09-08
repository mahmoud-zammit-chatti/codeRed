package com.mahmoud.incident_service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;


@SpringBootTest(properties = "spring.rabbitmq.enabled=false")
@AutoConfigureMockMvc
@Import(IntegrationTestingConfiguration.class)
public class IncidentControllerIT {

    @MockBean
    private ConnectionFactory connectionFactory;

    @Autowired
    private MockMvc mockMvc;
   


    @Test
    public void testCreateIncident() throws Exception{

    mockMvc.perform(post("/api/incidents/create")
    .contentType(MediaType.APPLICATION_JSON)
    .content(
        """
        {
                    "title": "broken leg",
                    "location": "cairo",
                    "description": "fell down the stairs",
                    "type": "ACCIDENT",
                    "reportedBy": "12345678"
        }

    """))
    
    .andExpect(status().isOk())
    .andDo(print());
    }
}
