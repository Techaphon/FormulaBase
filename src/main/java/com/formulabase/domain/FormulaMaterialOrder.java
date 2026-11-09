package com.formulabase.domain;

import java.util.Comparator;

/** Stable phase grouping for manufacturing documents and the worker material list. */
public final class FormulaMaterialOrder {
    private FormulaMaterialOrder() { }

    public static Comparator<FormulaMaterial> byPhaseAndCode() {
        return Comparator.comparing(FormulaMaterial::getPhase)
                .thenComparing(item -> item.getMaterial().getCode(), String.CASE_INSENSITIVE_ORDER)
                .thenComparing(item -> item.getMaterial().getCode());
    }
}
