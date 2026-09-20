package com.abodebase.api.auth.service;

import com.abodebase.api.auth.entity.User;
import com.abodebase.api.auth.repository.UserRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getUserById(UUID id) {

        return userRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException(
                "User not found."
            ));
    }

    public void deleteUser(String email) {

        User user = userRepository
            .findByEmailIgnoreCase(email)
            .orElseThrow(() -> new UsernameNotFoundException(
                "User not found."
            ));

        userRepository.delete(user);
    }

    public User updateUserStatus(UUID id, boolean enabled) {

        User user = getUserById(id);

        user.setEnabled(enabled);

        return userRepository.save(user);
    }

    public List<User> getAllUsers() {

        return userRepository.findAll();
    }
}
