package com.formulabase.domain;

/** Manufacturing phase of a material within one formula. */
public enum FormulaPhase {
    A("Water phase", "เฟสน้ำ"),
    B("Oil phase", "เฟสน้ำมัน"),
    C("Cool down phase", "เฟสสารเติมแต่ง"),
    P("Cool down phase", "เฟสสารเติมแต่ง");

    private final String englishDescription;
    private final String thaiDescription;
    FormulaPhase(String englishDescription, String thaiDescription) {
        this.englishDescription = englishDescription;
        this.thaiDescription = thaiDescription;
    }
    /** Kept for existing callers; use descriptionFor(language) in the UI. */
    public String getDescription() { return englishDescription + " / " + thaiDescription; }
    public String descriptionFor(String language) { return "th".equalsIgnoreCase(language) ? thaiDescription : englishDescription; }
}
