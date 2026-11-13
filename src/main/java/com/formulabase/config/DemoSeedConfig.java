package com.formulabase.config;

import com.formulabase.domain.User;
import com.formulabase.domain.UserRole;
import com.formulabase.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Explicit, one-time demo bootstrap. It is deliberately inactive in a normal
 * production run; enable it only with profiles prod,demo and DEMO_SEED_ENABLED=true.
 */
@Configuration
@Profile("demo")
@ConditionalOnProperty(prefix = "demo.seed", name = "enabled", havingValue = "true")
public class DemoSeedConfig {
    @Bean
    CommandLineRunner seedDemoUsers(UserRepository repository, PasswordEncoder passwordEncoder,
                                    @Value("${demo.seed.password:}") String password) {
        return args -> {
            if (password.length() < 10) {
                throw new IllegalStateException("DEMO_SEED_PASSWORD must contain at least 10 characters");
            }
            createIfMissing(repository, passwordEncoder, "ceo", "Demo CEO", UserRole.CEO, password);
            createIfMissing(repository, passwordEncoder, "qc", "Demo QC", UserRole.QC, password);
            createIfMissing(repository, passwordEncoder, "pc", "Demo PC", UserRole.PC, password);
            createIfMissing(repository, passwordEncoder, "worker1", "Demo Worker", UserRole.WORKER, password);
        };
    }

    private void createIfMissing(UserRepository repository, PasswordEncoder encoder, String username,
                                 String fullName, UserRole role, String password) {
        if (repository.findByUsernameIgnoreCase(username).isEmpty()) {
            User user = new User(username, encoder.encode(password), fullName, role);
            if (role == UserRole.CEO && repository.findByRoleAndActiveTrueAndArchivedFalse(UserRole.CEO).isEmpty()) user.protectAsPrimaryAdmin();
            repository.save(user);
        }
    }
}
