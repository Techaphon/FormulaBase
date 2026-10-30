package com.formulabase.service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Centralizes product quantity rules. Loss is an additive percentage of the
 * target quantity, and product units are always whole numbers.
 */
@Service
public class QuantityCalculationService {

    private static final BigDecimal ZERO = BigDecimal.ZERO;
    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);

    public QuantityBreakdown calculate(int targetQuantity, BigDecimal lossPercentage) {
        if (targetQuantity < 0) {
            throw new IllegalArgumentException("Target quantity cannot be negative");
        }
        if (lossPercentage == null || lossPercentage.compareTo(ZERO) < 0
                || lossPercentage.compareTo(ONE_HUNDRED) > 0) {
            throw new IllegalArgumentException("Loss percentage must be between 0 and 100");
        }

        BigDecimal exactAllowance = BigDecimal.valueOf(targetQuantity)
                .multiply(lossPercentage)
                .divide(ONE_HUNDRED, 8, RoundingMode.HALF_UP);
        int allowance = exactAllowance.setScale(0, RoundingMode.CEILING).intValueExact();
        int planned = Math.addExact(targetQuantity, allowance);

        return new QuantityBreakdown(targetQuantity, lossPercentage, allowance, planned);
    }

    public record QuantityBreakdown(
            int targetQuantity,
            BigDecimal lossPercentage,
            int lossAllowanceQuantity,
            int plannedQuantity) {
    }
}
