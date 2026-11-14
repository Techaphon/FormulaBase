package com.formulabase.config;

import com.formulabase.domain.User;
import com.formulabase.domain.UserRole;
import com.formulabase.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@Profile("!prod")
public class DemoDataConfig {
    @Bean
    @org.springframework.core.annotation.Order(0)
    CommandLineRunner seedUsers(UserRepository repository, PasswordEncoder passwordEncoder) {
        return args -> {
            createIfMissing(repository, new User("ceo", passwordEncoder.encode("123456"), "Demo CEO", UserRole.CEO));
            createIfMissing(repository, new User("qc", passwordEncoder.encode("123456"), "Demo QC", UserRole.QC));
            createIfMissing(repository, new User("pc", passwordEncoder.encode("123456"), "Demo PC", UserRole.PC));
            createIfMissing(repository, new User("worker1", passwordEncoder.encode("123456"), "Demo Worker", UserRole.WORKER));
        };
    }

    private void createIfMissing(UserRepository repository, User user) {
        if (repository.findByUsernameIgnoreCase(user.getUsername()).isEmpty()) {
            if (user.getRole() == UserRole.CEO && repository.findByRoleAndActiveTrueAndArchivedFalse(UserRole.CEO).isEmpty()) user.protectAsPrimaryAdmin();
            repository.save(user);
        }
    }
}
