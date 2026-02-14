package com.library.auth.service;

import com.library.auth.dto.AuthenticationResponse;
import com.library.auth.dto.SetupAdminRequest;
import com.library.auth.entity.Role;
import com.library.auth.entity.User;
import com.library.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SetupService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public boolean isSetupRequired() {
        return userRepository.count() == 0 || userRepository.findByRole(Role.ADMIN).isEmpty();
    }

    public AuthenticationResponse createInitialAdmin(SetupAdminRequest request) {
        if (!isSetupRequired()) {
            throw new IllegalStateException("Setup is not required. Admin already exists.");
        }

        var user = User.builder()
                .firstname(request.getFirstname())
                .lastname(request.getLastname())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.ADMIN)
                .build();

        userRepository.save(user);

        var jwtToken = jwtService.generateToken(user);
        return AuthenticationResponse.builder()
                .accessToken(jwtToken)
                .build();
    }
}
