package com.formulabase.service;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class QuantityCalculationServiceTest {

    private final QuantityCalculationService service = new QuantityCalculationService();

    @Test
    void calculatesAdditiveLossForWholePercentage() {
        var result = service.calculate(100, new BigDecimal("5"));

        assertEquals(5, result.lossAllowanceQuantity());
        assertEquals(105, result.plannedQuantity());
    }

    @Test
    void roundsFractionalAllowanceUp() {
        var result = service.calculate(101, new BigDecimal("5"));

        assertEquals(6, result.lossAllowanceQuantity());
        assertEquals(107, result.plannedQuantity());
    }

    @Test
    void zeroLossDoesNotIncreasePlan() {
        var result = service.calculate(100, BigDecimal.ZERO);

        assertEquals(0, result.lossAllowanceQuantity());
        assertEquals(100, result.plannedQuantity());
    }

    @Test
    void rejectsInvalidInputs() {
        assertThrows(IllegalArgumentException.class,
                () -> service.calculate(-1, BigDecimal.ZERO));
        assertThrows(IllegalArgumentException.class,
                () -> service.calculate(100, new BigDecimal("-0.1")));
        assertThrows(IllegalArgumentException.class,
                () -> service.calculate(100, new BigDecimal("100.01")));
    }
}
