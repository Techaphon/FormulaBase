package com.formulabase.domain;

/** Manufacturing phase of a material within one formula. */
public enum FormulaPhase {
    A("Water phase / เฟสน้ำ"),
    B("Oil phase / เฟสน้ำมัน"),
    C("Cool down phase / เฟสสารเติมแต่ง"),
    P("Cool down phase / เฟสสารเติมแต่ง");

    private final String description;
    FormulaPhase(String description) { this.description = description; }
    public String getDescription() { return description; }
}
