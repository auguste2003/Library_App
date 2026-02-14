package com.library.auth.service;

import com.library.auth.entity.Role;
import com.library.auth.entity.User;
import com.library.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    public User findUserById(Integer id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Transactional
    public User updateUserRole(Integer userId, Role newRole) {
        User user = findUserById(userId);

        // Security check: If target user is an ADMIN and we're trying to change role to
        // something else
        if (user.getRole() == Role.ADMIN && newRole != Role.ADMIN) {
            // Check if this is the last admin
            long adminCount = userRepository.findByRole(Role.ADMIN).size();
            if (adminCount <= 1) {
                throw new IllegalStateException("Cannot remove the last admin from the system");
            }
        }

        user.setRole(newRole);
        return userRepository.save(user);
    }
}
