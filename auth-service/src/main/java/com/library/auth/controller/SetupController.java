package com.library.auth.controller;

import com.library.auth.dto.AuthenticationResponse;
import com.library.auth.dto.SetupAdminRequest;
import com.library.auth.service.SetupService;
import io.swagger.v3.oas.annotations.Operation;

import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/setup")
@RequiredArgsConstructor
@Tag(name = "Setup", description = "Endpoints for initial system setup")
public class SetupController {

    private final SetupService setupService;

    @GetMapping("/status")
    @Operation(summary = "Check setup status", description = "Checks if initial admin setup is required")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Status returned successfully")
    })
    public ResponseEntity<Map<String, Boolean>> getSetupStatus() {
        return ResponseEntity.ok(Map.of("setupRequired", setupService.isSetupRequired()));
    }

    @PostMapping("/admin")
    @Operation(summary = "Create initial admin", description = "Creates the first admin user. Only works if no admins exist.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Admin created successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Admin already exists"),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    public ResponseEntity<?> createInitialAdmin(@Valid @RequestBody SetupAdminRequest request) {
        if (!setupService.isSetupRequired()) {
            return ResponseEntity.status(403).body(Map.of("error", "Setup is not required. Admin already exists."));
        }

        try {
            AuthenticationResponse response = setupService.createInitialAdmin(request);
            return ResponseEntity.ok(response);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(403).body(Map.of("error", e.getMessage()));
        }
    }
}
