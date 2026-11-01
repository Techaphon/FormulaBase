package com.formulabase.service;

import com.formulabase.domain.User;
import com.formulabase.domain.UserRole;
import com.formulabase.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

@Service
public class UserService {
    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    public UserService(UserRepository repository, PasswordEncoder passwordEncoder) { this.repository = repository; this.passwordEncoder = passwordEncoder; }

    @Transactional(readOnly = true)
    public User authenticate(String username, String password) {
        User user = repository.findByUsernameIgnoreCase(username)
                .filter(User::isActive)
                .orElseThrow(() -> new IllegalArgumentException("Invalid username or password"));
        boolean legacyPassword = user.getPassword() != null && !user.getPassword().startsWith("$2");
        boolean matches = password != null && (legacyPassword ? java.security.MessageDigest.isEqual(password.getBytes(java.nio.charset.StandardCharsets.UTF_8), user.getPassword().getBytes(java.nio.charset.StandardCharsets.UTF_8)) : passwordEncoder.matches(password, user.getPassword()));
        if (!matches) {
            throw new IllegalArgumentException("Invalid username or password");
        }
        if (legacyPassword || passwordEncoder.upgradeEncoding(user.getPassword())) user.setPassword(passwordEncoder.encode(password));
        return user;
    }

    @Transactional(readOnly = true)
    public User findActive(String username) {
        return repository.findByUsernameIgnoreCase(username)
                .filter(User::isActive)
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public List<User> workers() { return repository.findByRoleAndActiveTrue(UserRole.WORKER); }
    @Transactional(readOnly = true)
    public List<User> all() { return repository.findAll(); }
    @Transactional
    public User create(String username, String password, String fullName, UserRole role, User actor) {
        if (actor == null || actor.getRole() != UserRole.CEO) throw new IllegalStateException("Only CEO can manage users");
        if (username == null || username.isBlank() || password == null || password.isBlank() || fullName == null || fullName.isBlank() || role == null) throw new IllegalArgumentException("Complete all user fields");
        if (repository.findByUsernameIgnoreCase(username.trim()).isPresent()) throw new IllegalArgumentException("Username already exists");
        if (password.length() < 10) throw new IllegalArgumentException("Password must contain at least 10 characters");
        return repository.save(new User(username.trim(), passwordEncoder.encode(password), fullName.trim(), role));
    }
    @Transactional
    public User toggle(java.util.UUID id, User actor) {
        if (actor == null || actor.getRole() != UserRole.CEO) throw new IllegalStateException("Only CEO can manage users");
        User user = repository.findById(id).orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (user.getId().equals(actor.getId())) throw new IllegalArgumentException("CEO cannot deactivate the current account");
        user.setActive(!user.isActive()); return user;
    }
}
