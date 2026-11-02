ALTER TABLE production_steps
    ADD COLUMN IF NOT EXISTS instruction_th VARCHAR(1000),
    ADD COLUMN IF NOT EXISTS instruction_en VARCHAR(1000),
    ADD COLUMN IF NOT EXISTS temperature_min_c NUMERIC(8,2),
    ADD COLUMN IF NOT EXISTS temperature_max_c NUMERIC(8,2);

-- Existing procedures were created before bilingual instructions were introduced.
-- Preserve their visible instruction as the English fallback rather than changing
-- historical work orders.
UPDATE production_steps
SET instruction_en = instruction
WHERE instruction_en IS NULL;
