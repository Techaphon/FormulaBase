package com.formulabase.config;

import com.formulabase.domain.FormulaCalculationBasis;
import com.formulabase.domain.MaterialUnit;
import com.formulabase.repository.FormulaRepository;
import com.formulabase.repository.MaterialRepository;
import com.formulabase.repository.MonthlyPlanningCycleRepository;
import com.formulabase.repository.UserRepository;
import com.formulabase.service.FormulaService;
import com.formulabase.service.MaterialService;
import com.formulabase.service.PlanningService;
import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import javax.sql.DataSource;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.transaction.support.TransactionTemplate;

/** A small, disposable sample for the explicitly selected local-demo profile. */
@Configuration
@Profile("local-demo & !prod")
public class LocalDemoDataConfig {
    @Bean
    @Order(10)
    CommandLineRunner seedLocalExample(DataSource dataSource, TransactionTemplate transactions,
            UserRepository users, MaterialRepository materials, FormulaRepository formulas,
            MonthlyPlanningCycleRepository cycles, MaterialService materialService,
            FormulaService formulaService, PlanningService planningService) {
        return args -> {
            try (var connection = dataSource.getConnection()) {
                if (!connection.getMetaData().getURL().startsWith("jdbc:h2:mem:")) {
                    throw new IllegalStateException("local-demo requires an in-memory H2 database");
                }
            }
            transactions.executeWithoutResult(status -> {
                // Never mix sample data into an existing database.
                if (materials.count() != 0 || formulas.count() != 0 || cycles.count() != 0) return;
                var qc = users.findByUsernameIgnoreCase("qc").orElseThrow();
                var ceo = users.findByUsernameIgnoreCase("ceo").orElseThrow();
                var pc = users.findByUsernameIgnoreCase("pc").orElseThrow();
                var worker = users.findByUsernameIgnoreCase("worker1").orElseThrow();
                var water = materialService.create("DEMO-WATER", "Purified water (sample)", MaterialUnit.G, qc);
                var glycerin = materialService.create("DEMO-GLYCERIN", "Glycerin (sample)", MaterialUnit.G, qc);
                var formula = formulaService.createWithBilingualSteps("DEMO-001", "Sample moisturising serum",
                        List.of(water.getId().toString(), glycerin.getId().toString()), null, null, null,
                        List.of("A", "A"), FormulaCalculationBasis.PERCENT_W_W, new BigDecimal("50"),
                        List.of(new BigDecimal("95"), new BigDecimal("5")), null,
                        List.of("ชั่งวัตถุดิบให้ครบ", "ผสมจนเข้ากัน"),
                        List.of("Weigh the ingredients", "Mix until uniform"),
                        List.of(300, 600), List.of(false, false), null, null, qc);
                formulaService.submitForApproval(formula.getId(), qc);
                formulaService.confirm(formula.getId(), ceo);
                var today = BusinessTime.today();
                var cycle = planningService.createCycle(YearMonth.from(today).toString(), pc);
                planningService.addPlan(cycle.getId(), formula.getId(), worker.getId(), today, 20, pc);
                planningService.lockCycle(cycle.getId(), pc);
            });
        };
    }
}
