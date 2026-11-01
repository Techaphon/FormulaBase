ALTER TABLE formulas ADD COLUMN previous_revision_id UUID NULL REFERENCES formulas(id);
ALTER TABLE formulas ADD COLUMN created_by_id UUID NULL REFERENCES users(id);
ALTER TABLE formulas ADD COLUMN submitted_by_id UUID NULL REFERENCES users(id);
ALTER TABLE formulas ADD COLUMN submitted_at TIMESTAMP WITH TIME ZONE NULL;
ALTER TABLE formulas ADD COLUMN approved_by_id UUID NULL REFERENCES users(id);
ALTER TABLE formulas ADD COLUMN approved_at TIMESTAMP WITH TIME ZONE NULL;
ALTER TABLE formulas ADD COLUMN approval_reason VARCHAR(255) NULL;
CREATE INDEX idx_formula_previous_revision ON formulas(previous_revision_id);
