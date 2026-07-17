package com.eduapi.dto;

import lombok.*;

import java.time.LocalDateTime;

/**
 * User profile response DTO.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfileDTO {
    private Long id;
    private String username;
    private String email;
    private String role;
    private String firstName;
    private String lastName;
    private String phone;
    private String profilePictureUrl;
    private Long studentId;
    private String studentCode;
    private LocalDateTime createdAt;
}
