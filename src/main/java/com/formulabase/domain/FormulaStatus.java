package com.formulabase.domain;

public enum FormulaStatus {
    DRAFT,
    PENDING_APPROVAL,
    CONFIRMED,
    REJECTED,
    /** A newer confirmed revision exists. Historical plans may still reference this record. */
    SUPERSEDED
}
