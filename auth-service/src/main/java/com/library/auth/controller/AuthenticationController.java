package com.library.auth.controller;

import com.library.auth.dto.AuthenticationRequest;
import com.library.auth.dto.AuthenticationResponse;
import com.library.auth.dto.ChangePasswordRequest;
import com.library.auth.dto.NewPasswordRequest;
import com.library.auth.dto.PasswordResetRequest;
import com.library.auth.dto.RegisterRequest;
import com.library.auth.service.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("")
@RequiredArgsConstructor
@Tag(name = "authentication-controller", description = "Authentication and User Management")
public class AuthenticationController {

        private final AuthenticationService service;

        @PostMapping("/register")
        @Operation(summary = "Register new user", description = "Create a new user account")
        public ResponseEntity<?> register(
                        @RequestBody @Valid RegisterRequest request,
                        jakarta.servlet.http.HttpServletResponse response) {
                AuthenticationResponse authResponse = service.register(request);
                setTokenCookies(response, authResponse);
                return ResponseEntity.ok().build();
        }

        @PostMapping("/login")
        @Operation(summary = "Authenticate user", description = "Login with email and password")
        public ResponseEntity<?> authenticate(
                        @RequestBody AuthenticationRequest request,
                        jakarta.servlet.http.HttpServletResponse response) {
                AuthenticationResponse authResponse = service.authenticate(request);
                setTokenCookies(response, authResponse);
                return ResponseEntity.ok().build();
        }

        @PostMapping("/refresh")
        public ResponseEntity<?> refreshToken(
                        @CookieValue(name = "refresh_token", required = false) String refreshToken,
                        jakarta.servlet.http.HttpServletResponse response) {

                if (refreshToken == null) {
                        return ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED).build();
                }

                try {
                        String newAccessToken = service.refreshAccessToken(refreshToken);

                        jakarta.servlet.http.Cookie accessCookie = new jakarta.servlet.http.Cookie("access_token",
                                        newAccessToken);
                        accessCookie.setHttpOnly(true);
                        accessCookie.setSecure(true);
                        accessCookie.setPath("/");
                        accessCookie.setMaxAge(15 * 60); // 15 minutes
                        accessCookie.setAttribute("SameSite", "Strict");
                        response.addCookie(accessCookie);

                        return ResponseEntity.ok().build();
                } catch (Exception e) {
                        return ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED).build();
                }
        }

        @PostMapping("/logout")
        public ResponseEntity<?> logout(jakarta.servlet.http.HttpServletResponse response) {
                jakarta.servlet.http.Cookie accessCookie = new jakarta.servlet.http.Cookie("access_token", null);
                accessCookie.setMaxAge(0);
                accessCookie.setPath("/");
                accessCookie.setHttpOnly(true);
                response.addCookie(accessCookie);

                jakarta.servlet.http.Cookie refreshCookie = new jakarta.servlet.http.Cookie("refresh_token", null);
                refreshCookie.setMaxAge(0);
                refreshCookie.setPath("/");
                refreshCookie.setHttpOnly(true);
                response.addCookie(refreshCookie);

                return ResponseEntity.ok().build();
        }

        private void setTokenCookies(jakarta.servlet.http.HttpServletResponse response,
                        AuthenticationResponse authResponse) {
                jakarta.servlet.http.Cookie accessCookie = new jakarta.servlet.http.Cookie("access_token",
                                authResponse.getAccessToken());
                accessCookie.setHttpOnly(true);
                accessCookie.setSecure(true); // TODO: check if environment is local, maybe false for local testing
                                              // without https?
                // Actually for localhost secure=true might block cookies if not https.
                // Chrome allows secure cookies on localhost. Safari might not.
                // Let's set Secure for now as per plan.
                accessCookie.setPath("/");
                accessCookie.setMaxAge(15 * 60); // 15 minutes
                accessCookie.setAttribute("SameSite", "Strict");
                response.addCookie(accessCookie);

                jakarta.servlet.http.Cookie refreshCookie = new jakarta.servlet.http.Cookie("refresh_token",
                                authResponse.getRefreshToken());
                refreshCookie.setHttpOnly(true);
                refreshCookie.setSecure(true);
                refreshCookie.setPath("/");
                refreshCookie.setMaxAge(7 * 24 * 60 * 60); // 7 days
                refreshCookie.setAttribute("SameSite", "Strict");
                response.addCookie(refreshCookie);
        }

        @PatchMapping("/change-password")
        @Operation(summary = "Change password", description = "Change the password for the authenticated user. Requires current password for verification.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Password successfully changed"),
                        @ApiResponse(responseCode = "400", description = "Invalid current password or passwords don't match"),
                        @ApiResponse(responseCode = "401", description = "User not authenticated")
        })
        public ResponseEntity<?> changePassword(
                        @RequestBody @Valid ChangePasswordRequest request,
                        Principal connectedUser) {
                service.changePassword(request, connectedUser);
                return ResponseEntity.ok().build();
        }

        @PostMapping("/forgot-password")
        @Operation(summary = "Request password reset", description = "Send an email with a password reset link")
        public ResponseEntity<?> forgotPassword(@RequestBody @Valid PasswordResetRequest request) {
                service.forgotPassword(request);
                return ResponseEntity.ok("Password reset email sent");
        }

        @PostMapping("/reset-password")
        @Operation(summary = "Reset password", description = "Reset password using a valid token")
        public ResponseEntity<?> resetPassword(@RequestBody @Valid NewPasswordRequest request) {
                service.resetPassword(request);
                return ResponseEntity.ok("Password successfully reset");
        }
}
