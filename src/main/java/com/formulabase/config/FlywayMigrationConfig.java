package com.formulabase.config;

import javax.sql.DataSource;

import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Runs the versioned database schema before JPA validates it in production.
 *
 * <p>Spring Boot 4 does not auto-configure Flyway from {@code flyway-core}
 * alone, so the application owns this deliberately small bootstrap sequence.
 * Migrations remain the single source of truth; Hibernate only validates them.
 */
@Configuration(proxyBeanMethods = false)
@Profile("prod")
public class FlywayMigrationConfig {

    @Bean
    Flyway flyway(DataSource dataSource) {
        return Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration")
                .validateMigrationNaming(true)
                .cleanDisabled(true)
                .load();
    }

    @Bean
    InitializingBean flywayMigrationInitializer(Flyway flyway) {
        return flyway::migrate;
    }

    /** Ensures schema migration completes before Hibernate's ddl-auto=validate. */
    @Bean
    static BeanFactoryPostProcessor entityManagerFactoryDependsOnFlywayMigration() {
        return beanFactory -> {
            if (beanFactory.containsBeanDefinition("entityManagerFactory")) {
                beanFactory.getBeanDefinition("entityManagerFactory")
                        .setDependsOn("flywayMigrationInitializer");
            }
        };
    }
}
