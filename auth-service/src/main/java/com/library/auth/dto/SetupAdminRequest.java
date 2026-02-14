package com.library.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Request object for creating the initial admin user")
public class SetupAdminRequest {

    @NotBlank(message = "Firstname is required")
    @Schema(description = "Admin's first name", example = "Admin")
    private String firstname;

    @NotBlank(message = "Lastname is required")
    @Schema(description = "Admin's last name", example = "User")
    private String lastname;

    @Email(message = "Email should be valid")
    @NotBlank(message = "Email is required")
    @Schema(description = "Admin's email address", example = "admin@library.com")
    private String email;

    @NotBlank(message = "Password is required")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$", message = "Password must be at least 8 characters long and contain at least one digit, one lowercase letter, one uppercase letter, and one special character")
    @Schema(description = "Admin's password (strong constraints apply)", example = "SecureAdminPass1!23")
    private String password;
}
