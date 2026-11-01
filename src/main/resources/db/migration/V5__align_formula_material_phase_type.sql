-- Hibernate maps the one-character FormulaPhase enum to CHAR(1).
-- V1/V2 intentionally used VARCHAR(1), so existing and new databases are
-- aligned without modifying an already-applied migration checksum.
ALTER TABLE formula_materials
    ALTER COLUMN phase TYPE CHAR(1)
    USING phase::CHAR(1);
