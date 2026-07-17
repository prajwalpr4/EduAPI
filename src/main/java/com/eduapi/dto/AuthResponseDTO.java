package com.eduapi.dto;

import lombok.*;

/**
 * Auth response DTO — returned after login/register.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponseDTO {
    private String token;
    private String username;
    private String role;
    private Long userId;
    private String firstName;
    private String lastName;
    private String email;
}
