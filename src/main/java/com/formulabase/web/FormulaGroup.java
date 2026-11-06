package com.formulabase.web;

import com.formulabase.domain.Formula;

import java.util.List;

/** One row per product formula, with its individual revisions available on demand. */
public final class FormulaGroup {
    private final String documentNo;
    private final Formula latest;
    private final List<Formula> versions;

    public FormulaGroup(String documentNo, Formula latest, List<Formula> versions) {
        this.documentNo = documentNo;
        this.latest = latest;
        this.versions = List.copyOf(versions);
    }

    public String getDocumentNo() { return documentNo; }
    public Formula getLatest() { return latest; }
    public List<Formula> getVersions() { return versions; }
}
