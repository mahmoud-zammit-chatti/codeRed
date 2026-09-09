package com.mahmoud.incident_service.DTOs;


import com.mahmoud.incident_service.Models.incident.Type;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor    // <--- Jackson needs this to create the empty object!
@AllArgsConstructor   // <--- Builder needs this to function!
@Builder
public class CreateIncidentRequestDTO {

    @NotBlank(message="Title is required")
    private String title;
    @NotBlank(message="Description is required")
    private String description;
    @NotBlank(message="Location is required")
    private String location;
    @NotNull(message="Type is required")
    private Type type;
    @NotBlank(message="Reported by is required")
    @Pattern(regexp="^[0-9]{8}$", message="Reporter ID must be an 8-digit number")
    private String reportedBy;

}