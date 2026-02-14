package com.library.auth.bootstrap;

import com.library.auth.dto.RegisterRequest;
import com.library.auth.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final AuthenticationService authenticationService;

    @Override
    public void run(String... args) throws Exception {
        // We can't easily check if users exist via service without throwing exception,
        // so we'll wrap in try-catch or assume empty DB on first run.
        // Since we suggest 'docker compose down -v', DB is empty.

        try {
            createuser("Alice", "Smith", "alice@example.com");
            createuser("Bob", "Johnson", "bob@example.com");
            createuser("Charlie", "Brown", "charlie@example.com");
            createuser("David", "Wilson", "david@example.com");
            createuser("Eve", "Davis", "eve@example.com");

            System.out.println("5 Test users initialized.");
        } catch (Exception e) {
            System.out.println("Users might already exist: " + e.getMessage());
        }
    }

    private void createuser(String firstname, String lastname, String email) {
        var request = RegisterRequest.builder()
                .firstname(firstname)
                .lastname(lastname)
                .email(email)
                .password("Password123!")
                .build();
        authenticationService.register(request);
    }
}
