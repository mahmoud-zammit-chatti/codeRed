package com.mahmoud.incident_service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;

import com.mahmoud.incident_service.DTOs.*;
import com.mahmoud.incident_service.Exceptions.IncidentNotFoundException;
import com.mahmoud.incident_service.Models.*;
import com.mahmoud.incident_service.Repositories.IncidentRepo;
import com.mahmoud.incident_service.Services.IncidentService;
import com.mahmoud.schemas.events.IncidentAiEvalEvent;
import com.mahmoud.schemas.events.IncidentCreatedEvent;


@ExtendWith(MockitoExtension.class)
class IncidentServiceApplicationTests {

	@Autowired
	//setup mock services and repositories
	@Mock
	IncidentRepo incidentRepository;
	
	@Mock
	IncidentMapper incidentMapper;

	@Mock
	AmqpTemplate amqpTemplate;

	private IncidentService incidentService;
	
	ZoneId BuisnessZoneId = ZoneId.of("Africa/Tunis");
	
	Clock buisnessClock = Clock.system(BuisnessZoneId);

	String EXCHANGE_NAME = "incident_exchange";
	String INCIDENT_CREATED_ROUTING_KEY = "incident.created";

	@BeforeEach
	public void setup() {

		incidentService = new IncidentService(incidentRepository, incidentMapper, amqpTemplate, buisnessClock);
		ReflectionTestUtils.setField(incidentService, "EXCHANGE_NAME", EXCHANGE_NAME);
		ReflectionTestUtils.setField(incidentService, "INCIDENT_CREATED_ROUTING_KEY", INCIDENT_CREATED_ROUTING_KEY);
		ReflectionTestUtils.setField(incidentService, "AI_EVALUATION_QUEUE", "ai-evaluation-queue");
	}

	@Test
	void create_incident_when_happy_path() {
		Incident incident = createSampleIncident(Status.PENDING);
		CreateIncidentRequestDTO request = createSampleCreateIncidentRequestDTO();
		IncidentCreatedEvent payload = createSampleIncidentCreatedEvent(incident);
		IncidentResponseDTO responseDTO = createSampleIncidentResponseDTO(incident);

		when(incidentMapper.toEntity(request)).thenReturn(incident);
		when(incidentRepository.save(incident)).thenReturn(incident);
		when(incidentMapper.toIncidentCreatedEvent(incident)).thenReturn(payload);
		when(incidentMapper.toResponse(incident)).thenReturn(responseDTO);

		IncidentResponseDTO response = incidentService.createIncident(request);
		
		//assert that the response is correct
		verify(amqpTemplate, times(1)).convertAndSend(EXCHANGE_NAME, INCIDENT_CREATED_ROUTING_KEY, payload);
		verify(incidentRepository, times(1)).save(incident);
		verify(incidentMapper, times(1)).toEntity(request);
		verify(incidentMapper, times(1)).toIncidentCreatedEvent(incident);
		verify(incidentMapper, times(1)).toResponse(incident);

		assertNotNull(response);
		assertEquals(incident.getId(), response.getId());
		

	}

	
	
	@Test
	void evaluate_incident_when_happy_path(){
		Incident incident = createSampleIncident(Status.PENDING);
		IncidentAiEvalEvent evalResult = createSampleIncidentAiEvalEvent(incident);

		when(incidentRepository.findById(incident.getId())).thenReturn(Optional.of(incident));

		incidentService.handleAiEvaluation(evalResult);

		assertEquals(Severity.HIGH, incident.getSeverity());
		assertEquals(Type.MEDICAL, incident.getType());
		verify(incidentRepository, times(1)).findById(incident.getId());
		verify(incidentRepository, times(1)).save(incident);
	}


	@Test
	void evaluate_incident_when_incident_not_found(){
		Incident incident = createSampleIncident(Status.PENDING);
		IncidentAiEvalEvent evalResult = createSampleIncidentAiEvalEvent(incident);
		incident.setId(UUID.randomUUID()); //simulate an incident that is not in the database
		System.out.println("incident ID: "+incident.getId());
		System.out.println("Eval result ID: "+evalResult);

		//incidentService.handleAiEvaluation(evalResult);

		assertThrows(IncidentNotFoundException.class,()->incidentService.handleAiEvaluation(evalResult));

		verify(incidentRepository, times(1)).findById(evalResult.getIncidentId());
		verify(incidentRepository, never()).save(any(Incident.class));



		
	}

	

	private Incident createSampleIncident(Status status) {
		Incident incident = new Incident();
		incident.setId(UUID.randomUUID());
		incident.setTitle("Sample Incident");
		incident.setDescription("This is a sample incident for testing.");
		incident.setType(Type.MEDICAL);
		incident.setSeverity(Severity.MEDIUM);
		incident.setStatus(status);
		incident.setCreatedAt(Instant.now(buisnessClock));

		return incident;
	}

	private CreateIncidentRequestDTO createSampleCreateIncidentRequestDTO() {
		CreateIncidentRequestDTO request = new CreateIncidentRequestDTO();
		request.setTitle("Sample Incident");
		request.setDescription("This is a sample incident for testing.");
		request.setType(Type.MEDICAL);
		request.setLocation("Sample Location");
		request.setReportedBy("12345678");
		return request;
	}

	private IncidentCreatedEvent createSampleIncidentCreatedEvent(Incident incident) {
		IncidentCreatedEvent event = new IncidentCreatedEvent();
		event.setId(incident.getId());
		event.setTitle(incident.getTitle());
		event.setDescription(incident.getDescription());
		event.setLocation("Sample Location");
		event.setType(incident.getType().name());
		event.setSeverity(incident.getSeverity().name());
		event.setReportedBy("12345678");
		event.setCreatedAt(Date.from(incident.getCreatedAt()));
		return event;
	}

	private IncidentResponseDTO createSampleIncidentResponseDTO(Incident incident) {
		return IncidentResponseDTO.builder()
				.id(incident.getId())
				.title(incident.getTitle())
				.description(incident.getDescription())
				.location("Sample Location")
				.type(incident.getType())
				.status(incident.getStatus())
				.severity(incident.getSeverity())
				.reportedBy("12345678")
				.createdAt(incident.getCreatedAt())
				.build();
	}

	private IncidentAiEvalEvent createSampleIncidentAiEvalEvent(Incident incident) {
		IncidentAiEvalEvent event = new IncidentAiEvalEvent();
		event.setIncidentId(incident.getId());
		event.setEvaluation(Severity.HIGH.name());
		event.setRecommendedAction(Type.MEDICAL.name());
		return event;
	}
}
