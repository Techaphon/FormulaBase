package com.formulabase;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties={"spring.datasource.url=jdbc:h2:mem:contexttest", "spring.jpa.hibernate.ddl-auto=create-drop"})
class FormulaBaseApplicationTests {

    @Test
    void contextLoads() {
    }
}
