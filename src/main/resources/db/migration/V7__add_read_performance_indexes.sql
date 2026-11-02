-- Supports the read paths used by the dashboard, monthly planning and worker queue.
-- These indexes avoid sequential scans as demo data grows; they do not alter data.
CREATE INDEX IF NOT EXISTS idx_formula_status_document
    ON formulas(status, document_no);

CREATE INDEX IF NOT EXISTS idx_plan_status_planned_date
    ON production_plans(status, planned_date);

CREATE INDEX IF NOT EXISTS idx_plan_worker_date_status
    ON production_plans(assigned_worker_id, planned_date, status);

CREATE INDEX IF NOT EXISTS idx_history_completed_at
    ON production_histories(completed_at DESC);
