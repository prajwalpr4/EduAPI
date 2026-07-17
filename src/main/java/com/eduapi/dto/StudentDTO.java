package com.eduapi.dto;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Student response DTO — returned from API.
 * Field names match entity fields in camelCase.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentDTO {
    private Long id;
    private Long userId;
    private String studentCode;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String profilePictureUrl;
    private LocalDate dateOfBirth;
    private LocalDate enrollmentDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
