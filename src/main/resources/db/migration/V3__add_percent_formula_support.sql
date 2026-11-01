ALTER TABLE formulas ADD COLUMN calculation_basis VARCHAR(30) NOT NULL DEFAULT 'PER_PRODUCT_UNIT';
ALTER TABLE formulas ADD COLUMN product_unit_weight_grams NUMERIC(19,8);
ALTER TABLE formula_materials ADD COLUMN percentage NUMERIC(9,6);
