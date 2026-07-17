package com.eduapi.dto;

import jakarta.validation.constraints.*;
import lombok.*;

/**
 * DTO for updating an enrollment (grade/status).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnrollmentUpdateDTO {

    @Size(max = 5, message = "Grade must be at most 5 characters")
    private String grade;

    @Pattern(regexp = "ACTIVE|COMPLETED|DROPPED", message = "Status must be ACTIVE, COMPLETED, or DROPPED")
    private String status;
}
