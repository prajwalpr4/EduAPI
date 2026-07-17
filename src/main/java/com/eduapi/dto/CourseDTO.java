package com.eduapi.dto;

import lombok.*;

import java.time.LocalDateTime;

/**
 * Course response DTO.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseDTO {
    private Long id;
    private String courseCode;
    private String title;
    private String description;
    private Integer credits;
    private Integer maxCapacity;
    private Integer enrolledCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
