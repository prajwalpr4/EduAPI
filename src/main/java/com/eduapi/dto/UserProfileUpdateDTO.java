package com.eduapi.dto;

import jakarta.validation.constraints.*;
import lombok.*;

/**
 * DTO for updating user profile.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfileUpdateDTO {

    @NotBlank(message = "First name is required")
    @Size(max = 50, message = "First name must be at most 50 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 50, message = "Last name must be at most 50 characters")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Enter a valid email address")
    private String email;

    @Size(max = 20, message = "Phone must be at most 20 characters")
    private String phone;

    private String profilePictureUrl;
}
