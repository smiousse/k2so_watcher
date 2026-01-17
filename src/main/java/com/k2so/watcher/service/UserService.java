package com.k2so.watcher.service;

import com.k2so.watcher.model.User;
import com.k2so.watcher.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${k2so.admin.default-username:admin}")
    private String defaultAdminUsername;

    @Value("${k2so.admin.default-password:admin}")
    private String defaultAdminPassword;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostConstruct
    public void initDefaultAdmin() {
        if (!userRepository.existsByUsername(defaultAdminUsername)) {
            User admin = new User();
            admin.setUsername(defaultAdminUsername);
            admin.setPassword(passwordEncoder.encode(defaultAdminPassword));
            admin.setRole("ADMIN");
            admin.setEnabled(true);
            userRepository.save(admin);
        }
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    @Transactional
    public User createUser(String username, String password, String role) {
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username already exists");
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);
        user.setEnabled(true);

        return userRepository.save(user);
    }

    @Transactional
    public User updateUser(Long id, String username, String role, boolean enabled) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!user.getUsername().equals(username) && userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username already exists");
        }

        user.setUsername(username);
        user.setRole(role);
        user.setEnabled(enabled);

        return userRepository.save(user);
    }

    @Transactional
    public void changePassword(Long id, String newPassword) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Transactional
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    @Transactional
    public void updateTotpSecret(String username, String secret) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setTotpSecret(secret);
        userRepository.save(user);
    }

    @Transactional
    public void enableTotp(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setTotpEnabled(true);
        userRepository.save(user);
    }

    @Transactional
    public void disableTotp(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setTotpEnabled(false);
        user.setTotpSecret(null);
        userRepository.save(user);
    }

    @Transactional
    public void recordSuccessfulLogin(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setLastLogin(LocalDateTime.now());
        user.setFailedLoginAttempts(0);
        user.setLockTime(null);
        userRepository.save(user);
    }

    @Transactional
    public void recordFailedLogin(String username) {
        Optional<User> optUser = userRepository.findByUsername(username);
        if (optUser.isPresent()) {
            User user = optUser.get();
            int attempts = user.getFailedLoginAttempts() + 1;
            user.setFailedLoginAttempts(attempts);
            if (attempts >= 5) {
                user.setLockTime(LocalDateTime.now());
            }
            userRepository.save(user);
        }
    }

    public boolean isPasswordValid(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
}
