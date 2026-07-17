package com.eduapi.dto;

import jakarta.validation.constraints.*;
import lombok.*;

/**
 * DTO for creating a new course.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseCreateDTO {

    @NotBlank(message = "Course code is required")
    @Size(max = 20, message = "Course code must be at most 20 characters")
    private String courseCode;

    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must be at most 200 characters")
    private String title;

    private String description;

    @NotNull(message = "Credits are required")
    @Min(value = 1, message = "Credits must be at least 1")
    @Max(value = 12, message = "Credits must be at most 12")
    private Integer credits;

    @NotNull(message = "Max capacity is required")
    @Min(value = 1, message = "Max capacity must be at least 1")
    private Integer maxCapacity;
}
