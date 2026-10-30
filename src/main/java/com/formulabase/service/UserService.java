package com.formulabase.service;

import com.formulabase.domain.User;
import com.formulabase.domain.UserRole;
import com.formulabase.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {
    private final UserRepository repository;
    public UserService(UserRepository repository) { this.repository = repository; }

    @Transactional(readOnly = true)
    public User authenticate(String username, String password) {
        User user = repository.findByUsernameIgnoreCase(username)
                .filter(User::isActive)
                .orElseThrow(() -> new IllegalArgumentException("Invalid username or password"));
        if (!user.getPassword().equals(password)) {
            throw new IllegalArgumentException("Invalid username or password");
        }
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
}
