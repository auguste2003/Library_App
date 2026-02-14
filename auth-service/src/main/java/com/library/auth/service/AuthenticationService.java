package com.library.auth.service;

import com.library.auth.dto.AuthenticationRequest;
import com.library.auth.dto.AuthenticationResponse;
import com.library.auth.dto.ChangePasswordRequest;
import com.library.auth.dto.RegisterRequest;
import com.library.auth.entity.Role;
import com.library.auth.entity.PasswordResetToken;
import com.library.auth.repository.PasswordResetTokenRepository;
import com.library.auth.dto.PasswordResetRequest;
import com.library.auth.dto.NewPasswordRequest;
import org.springframework.beans.factory.annotation.Value;
import java.time.LocalDateTime;
import java.util.UUID;
import com.library.auth.entity.User;
import com.library.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

        private final UserRepository repository;
        private final PasswordResetTokenRepository tokenRepository;
        private final PasswordEncoder passwordEncoder;
        private final JwtService jwtService;
        private final AuthenticationManager authenticationManager;
        private final EmailService emailService;

        @Value("${FRONTEND_URL:http://localhost:4200}")
        private String frontendUrl;

        public AuthenticationResponse register(RegisterRequest request) {
                if (repository.findByEmail(request.getEmail()).isPresent()) {
                        throw new com.library.auth.exception.UserAlreadyExistsException(
                                        "User with email " + request.getEmail() + " already exists");
                }
                var user = User.builder()
                                .firstname(request.getFirstname())
                                .lastname(request.getLastname())
                                .email(request.getEmail())
                                .password(passwordEncoder.encode(request.getPassword()))
                                .role(Role.USER)
                                .build();
                repository.save(user);
                var jwtToken = jwtService.generateToken(user);
                var refreshToken = jwtService.generateRefreshToken(user);
                return AuthenticationResponse.builder()
                                .accessToken(jwtToken)
                                .refreshToken(refreshToken)
                                .build();
        }

        public AuthenticationResponse authenticate(AuthenticationRequest request) {
                var user = repository.findByEmail(request.getEmail())
                                .orElseThrow(() -> new org.springframework.security.authentication.BadCredentialsException(
                                                "Invalid email or password"));

                try {
                        authenticationManager.authenticate(
                                        new UsernamePasswordAuthenticationToken(
                                                        request.getEmail(),
                                                        request.getPassword()));
                        // Reset failed attempts on success
                        user.setFailedAttempts(0);
                        user.setLockTime(null);
                        repository.save(user);

                } catch (org.springframework.security.authentication.BadCredentialsException e) {
                        // Increment failed attempts
                        if (user.isAccountNonLocked()) {
                                int attempts = user.getFailedAttempts() == null ? 0 : user.getFailedAttempts();
                                if (attempts < 3) {
                                        user.setFailedAttempts(attempts + 1);
                                }
                                if (user.getFailedAttempts() >= 3) {
                                        user.setLockTime(java.time.LocalDateTime.now().plusMinutes(15));
                                }
                                repository.save(user);
                        }
                        throw e;
                }

                var jwtToken = jwtService.generateToken(user);
                var refreshToken = jwtService.generateRefreshToken(user);
                return AuthenticationResponse.builder()
                                .accessToken(jwtToken)
                                .refreshToken(refreshToken)
                                .build();
        }

        public String refreshAccessToken(String refreshToken) {
                final String username = jwtService.extractUsername(refreshToken);
                if (username != null) {
                        var user = repository.findByEmail(username)
                                        .orElseThrow(() -> new RuntimeException("User not found"));
                        if (jwtService.isTokenValid(refreshToken, user)) {
                                return jwtService.generateToken(user);
                        }
                }
                throw new RuntimeException("Invalid refresh token");
        }

        public void changePassword(ChangePasswordRequest request, java.security.Principal connectedUser) {
                var user = (User) ((UsernamePasswordAuthenticationToken) connectedUser).getPrincipal();

                // Check if the current password is correct
                if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
                        throw new IllegalStateException("Wrong password");
                }
                // check if the two new passwords are the same
                if (!request.getNewPassword().equals(request.getConfirmationPassword())) {
                        throw new IllegalStateException("Password are not the same");
                }
                user.setPassword(passwordEncoder.encode(request.getNewPassword()));
                repository.save(user);
        }

        public void forgotPassword(PasswordResetRequest request) {
                var user = repository.findByEmail(request.getEmail())
                                .orElseThrow(() -> new RuntimeException("User not found"));

                String token = UUID.randomUUID().toString();
                // Token valid for 24 hours
                PasswordResetToken resetToken = PasswordResetToken.builder()
                                .token(token)
                                .user(user)
                                .expiryDate(LocalDateTime.now().plusHours(24))
                                .build();

                tokenRepository.save(resetToken);

                String resetLink = frontendUrl + "/reset-password?token=" + token;
                emailService.sendPasswordResetEmail(user.getEmail(), resetLink);
        }

        public void resetPassword(NewPasswordRequest request) {
                PasswordResetToken resetToken = tokenRepository.findByToken(request.getToken())
                                .orElseThrow(() -> new RuntimeException("Invalid token"));

                if (resetToken.isExpired()) {
                        throw new RuntimeException("Token expired");
                }

                User user = resetToken.getUser();
                user.setPassword(passwordEncoder.encode(request.getNewPassword()));
                repository.save(user);

                tokenRepository.delete(resetToken);
        }
}
