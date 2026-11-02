-- Formula Base: consolidated PostgreSQL schema for dbdiagram.io
-- Purpose: paste this file into dbdiagram.io (Import > PostgreSQL) to render the ERD.
-- This is documentation SQL only. Do NOT run it against an existing Supabase database;
-- Flyway migrations in src/main/resources/db/migration remain the source of deployment truth.

CREATE TABLE users (
  id UUID PRIMARY KEY,
  version BIGINT NOT NULL DEFAULT 0,
  username VARCHAR(80) NOT NULL UNIQUE,
  password VARCHAR(255) NOT NULL,
  full_name VARCHAR(255) NOT NULL,
  role VARCHAR(20) NOT NULL,
  active BOOLEAN NOT NULL
);

CREATE TABLE materials (
  id UUID PRIMARY KEY,
  version BIGINT NOT NULL DEFAULT 0,
  code VARCHAR(50) NOT NULL UNIQUE,
  name VARCHAR(255) NOT NULL,
  stock_unit VARCHAR(20) NOT NULL
);

CREATE TABLE formulas (
  id UUID PRIMARY KEY,
  version BIGINT NOT NULL DEFAULT 0,
  document_no VARCHAR(50) NOT NULL UNIQUE,
  name VARCHAR(255) NOT NULL,
  revision INTEGER NOT NULL,
  status VARCHAR(20) NOT NULL,
  rejection_reason VARCHAR(255),
  calculation_basis VARCHAR(30) NOT NULL DEFAULT 'PER_PRODUCT_UNIT',
  product_unit_weight_grams NUMERIC(19,8),
  previous_revision_id UUID REFERENCES formulas(id),
  created_by_id UUID REFERENCES users(id),
  submitted_by_id UUID REFERENCES users(id),
  submitted_at TIMESTAMP WITH TIME ZONE,
  approved_by_id UUID REFERENCES users(id),
  approved_at TIMESTAMP WITH TIME ZONE,
  approval_reason VARCHAR(255)
);

CREATE TABLE formula_materials (
  id UUID PRIMARY KEY,
  formula_id UUID NOT NULL REFERENCES formulas(id),
  material_id UUID NOT NULL REFERENCES materials(id),
  quantity_per_unit NUMERIC(19,8) NOT NULL,
  percentage NUMERIC(9,6),
  unit VARCHAR(20) NOT NULL,
  phase CHAR(1) NOT NULL DEFAULT 'A',
  CONSTRAINT uk_formula_material UNIQUE (formula_id, material_id)
);

CREATE TABLE production_steps (
  id UUID PRIMARY KEY,
  formula_id UUID NOT NULL REFERENCES formulas(id),
  step_number INTEGER NOT NULL,
  instruction VARCHAR(1000) NOT NULL,
  instruction_th VARCHAR(1000),
  instruction_en VARCHAR(1000),
  target_seconds INTEGER,
  requires_temperature BOOLEAN NOT NULL,
  temperature_min_c NUMERIC(8,2),
  temperature_max_c NUMERIC(8,2),
  CONSTRAINT uk_formula_step UNIQUE (formula_id, step_number)
);

CREATE TABLE monthly_planning_cycles (
  id UUID PRIMARY KEY,
  version BIGINT NOT NULL DEFAULT 0,
  planning_month VARCHAR(7) NOT NULL UNIQUE,
  status VARCHAR(20) NOT NULL
);

CREATE TABLE production_plans (
  id UUID PRIMARY KEY,
  version BIGINT NOT NULL DEFAULT 0,
  planning_cycle_id UUID NOT NULL REFERENCES monthly_planning_cycles(id),
  formula_id UUID NOT NULL REFERENCES formulas(id),
  assigned_worker_id UUID REFERENCES users(id),
  planned_date DATE NOT NULL,
  target_quantity INTEGER NOT NULL,
  loss_percentage NUMERIC(7,4),
  loss_allowance_quantity INTEGER,
  planned_quantity INTEGER,
  actual_produced_quantity INTEGER,
  rejected_quantity INTEGER,
  started_at TIMESTAMP WITH TIME ZONE,
  completed_at TIMESTAMP WITH TIME ZONE,
  status VARCHAR(20) NOT NULL,
  quality_review_note VARCHAR(255),
  quality_reviewed_by VARCHAR(255),
  quality_reviewed_at TIMESTAMP WITH TIME ZONE
);

CREATE TABLE step_executions (
  id UUID PRIMARY KEY,
  production_plan_id UUID NOT NULL REFERENCES production_plans(id),
  step_id UUID NOT NULL REFERENCES production_steps(id),
  worker_id UUID REFERENCES users(id),
  status VARCHAR(20) NOT NULL,
  recorded_temperature NUMERIC(8,2),
  started_at TIMESTAMP WITH TIME ZONE,
  completed_at TIMESTAMP WITH TIME ZONE,
  overdue BOOLEAN NOT NULL DEFAULT FALSE,
  was_overdue BOOLEAN NOT NULL DEFAULT FALSE,
  acknowledgement_note VARCHAR(255),
  acknowledged_at TIMESTAMP WITH TIME ZONE,
  acknowledged_by_id UUID REFERENCES users(id),
  CONSTRAINT uk_plan_step UNIQUE (production_plan_id, step_id)
);

CREATE TABLE reschedule_logs (
  id UUID PRIMARY KEY,
  production_plan_id UUID NOT NULL REFERENCES production_plans(id),
  previous_date DATE NOT NULL,
  new_date DATE NOT NULL,
  reason VARCHAR(255) NOT NULL,
  changed_by_id UUID REFERENCES users(id),
  changed_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE production_histories (
  id UUID PRIMARY KEY,
  production_plan_id UUID NOT NULL UNIQUE REFERENCES production_plans(id),
  target_quantity INTEGER NOT NULL,
  loss_percentage NUMERIC(7,4) NOT NULL,
  loss_allowance_quantity INTEGER NOT NULL,
  planned_quantity INTEGER NOT NULL,
  actual_produced_quantity INTEGER NOT NULL,
  rejected_quantity INTEGER NOT NULL,
  good_quantity INTEGER NOT NULL,
  surplus_quantity INTEGER NOT NULL,
  completed_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE audit_logs (
  id UUID PRIMARY KEY,
  actor_id UUID REFERENCES users(id),
  action VARCHAR(80) NOT NULL,
  subject_type VARCHAR(80) NOT NULL,
  subject_id UUID,
  details VARCHAR(1000) NOT NULL,
  created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

