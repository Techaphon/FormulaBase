package com.formulabase.service;

import com.formulabase.domain.User;
import com.formulabase.domain.UserRole;
import com.formulabase.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(properties = {"spring.datasource.url=jdbc:h2:mem:legacyupgrade", "spring.jpa.hibernate.ddl-auto=create-drop"})
class LegacyPasswordUpgradeTest {
    @Autowired UserService service;
    @Autowired UserRepository users;

    @Test void successfulLegacyLoginPersistsUpgradedPassword() {
        var user = users.saveAndFlush(new User("legacy-test", "legacy-password", "Legacy user", UserRole.WORKER));
        service.authenticate("legacy-test", "legacy-password");
        assertTrue(users.findById(user.getId()).orElseThrow().getPassword().startsWith("$2"));
    }
}
