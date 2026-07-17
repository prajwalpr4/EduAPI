package com.eduapi.dto;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Enrollment response DTO — includes student and course names for display.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnrollmentDTO {
    private Long id;
    private Long studentId;
    private String studentName;
    private String studentCode;
    private Long courseId;
    private String courseTitle;
    private String courseCode;
    private LocalDate enrollmentDate;
    private String grade;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
