package com.formulabase.service;

import com.formulabase.domain.Formula;
import com.formulabase.domain.FormulaMaterialOrder;
import com.formulabase.domain.FormulaPhase;
import com.formulabase.domain.Material;
import com.formulabase.domain.MaterialUnit;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class FormulaMaterialOrderTest {
    @Test void groupsPhasesAndUsesCodeForStableOrderWithoutChangingFormula() {
        var formula = new Formula("TEST", "Test");
        formula.addMaterial(new Material("C1", "C", MaterialUnit.G), BigDecimal.ONE, FormulaPhase.C);
        formula.addMaterial(new Material("A2", "A2", MaterialUnit.G), BigDecimal.ONE, FormulaPhase.A);
        formula.addMaterial(new Material("B1", "B", MaterialUnit.G), BigDecimal.ONE, FormulaPhase.B);
        formula.addMaterial(new Material("A1", "A1", MaterialUnit.G), BigDecimal.ONE, FormulaPhase.A);
        formula.addMaterial(new Material("P1", "P", MaterialUnit.G), BigDecimal.ONE, FormulaPhase.P);
        var result = formula.getMaterials().stream().sorted(FormulaMaterialOrder.byPhaseAndCode())
                .map(item -> item.getMaterial().getCode()).toList();
        assertEquals(List.of("A1", "A2", "B1", "C1", "P1"), result);
        assertEquals("C1", formula.getMaterials().get(0).getMaterial().getCode());
    }
}
