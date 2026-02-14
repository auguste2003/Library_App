package com.library.auth.dto;

import com.library.auth.entity.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Request object for updating a user's role")
public class UpdateUserRoleRequest {

    @NotNull(message = "Role is required")
    @Schema(description = "New role for the user", example = "ADMIN")
    private Role role;
}
