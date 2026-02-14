package com.library.auth.service;

public interface EmailService {
    void sendPasswordResetEmail(String to, String resetLink);
}
