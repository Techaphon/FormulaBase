package com.formulabase.domain;

/**
 * Keeps legacy per-piece formulas valid while allowing cosmetic formulations
 * to use percentage by weight as their canonical definition.
 */
public enum FormulaCalculationBasis {
    PER_PRODUCT_UNIT,
    PERCENT_W_W
}
