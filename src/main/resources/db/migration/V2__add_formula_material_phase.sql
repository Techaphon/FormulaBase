-- V1 in the current release already contains this field.  IF NOT EXISTS keeps a
-- fresh PostgreSQL/Flyway installation repeatable without altering existing data.
ALTER TABLE formula_materials ADD COLUMN IF NOT EXISTS phase VARCHAR(1) NOT NULL DEFAULT 'A';
