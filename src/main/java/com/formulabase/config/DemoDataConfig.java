package com.formulabase.config;

import com.formulabase.domain.User;
import com.formulabase.domain.UserRole;
import com.formulabase.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DemoDataConfig {
    @Bean
    CommandLineRunner seedUsers(UserRepository repository) {
        return args -> {
            createIfMissing(repository, new User("ceo", "123456", "Demo CEO", UserRole.CEO));
            createIfMissing(repository, new User("qc", "123456", "Demo QC", UserRole.QC));
            createIfMissing(repository, new User("pc", "123456", "Demo PC", UserRole.PC));
            createIfMissing(repository, new User("worker1", "123456", "Demo Worker", UserRole.WORKER));
        };
    }

    private void createIfMissing(UserRepository repository, User user) {
        if (repository.findByUsernameIgnoreCase(user.getUsername()).isEmpty()) repository.save(user);
    }
}
