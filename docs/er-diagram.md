# Formula Base — ER Diagram

```mermaid
erDiagram
    USERS {
        uuid id PK
        string username UK
        string password
        string full_name
        string role
        boolean active
    }

    MATERIALS {
        uuid id PK
        string code UK
        string name
        string stock_unit
    }

    FORMULAS {
        uuid id PK
        string document_no UK
        string name
        int revision
        string calculation_basis
        decimal product_unit_weight_grams
        string status
        uuid previous_revision_id FK
        uuid created_by_id FK
        uuid submitted_by_id FK
        datetime submitted_at
        uuid approved_by_id FK
        datetime approved_at
    }

    FORMULA_MATERIALS {
        uuid id PK
        uuid formula_id FK
        uuid material_id FK
        decimal quantity_per_unit
        decimal percentage
        string unit
        string phase
    }

    PRODUCTION_STEPS {
        uuid id PK
        uuid formula_id FK
        int step_number
        string instruction_th
        string instruction_en
        int target_seconds
        boolean requires_temperature
        decimal temperature_min_c
        decimal temperature_max_c
    }

    MONTHLY_PLANNING_CYCLES {
        uuid id PK
        string planning_month UK
        string status
    }

    PRODUCTION_PLANS {
        uuid id PK
        uuid planning_cycle_id FK
        uuid formula_id FK
        uuid assigned_worker_id FK
        date planned_date
        int target_quantity
        decimal loss_percentage
        int loss_allowance_quantity
        int planned_quantity
        int actual_produced_quantity
        int rejected_quantity
        string status
    }

    STEP_EXECUTIONS {
        uuid id PK
        uuid production_plan_id FK
        uuid step_id FK
        uuid worker_id FK
        string status
        decimal recorded_temperature
        datetime started_at
        datetime completed_at
        boolean overdue
        uuid acknowledged_by_id FK
    }

    PRODUCTION_HISTORIES {
        uuid id PK
        uuid production_plan_id FK
        int target_quantity
        decimal loss_percentage
        int planned_quantity
        int actual_produced_quantity
        int rejected_quantity
        int good_quantity
        datetime completed_at
    }

    RESCHEDULE_LOGS {
        uuid id PK
        uuid production_plan_id FK
        date previous_date
        date new_date
        string reason
        uuid changed_by_id FK
        datetime changed_at
    }

    AUDIT_LOGS {
        uuid id PK
        uuid actor_id FK
        string action
        string subject_type
        uuid subject_id
        string details
        datetime created_at
    }

    USERS o|--o{ FORMULAS : creates
    USERS o|--o{ FORMULAS : submits_approves
    FORMULAS o|--o{ FORMULAS : previous_revision
    FORMULAS ||--o{ FORMULA_MATERIALS : contains
    MATERIALS ||--o{ FORMULA_MATERIALS : used_by
    FORMULAS ||--o{ PRODUCTION_STEPS : defines
    MONTHLY_PLANNING_CYCLES ||--o{ PRODUCTION_PLANS : schedules
    FORMULAS ||--o{ PRODUCTION_PLANS : planned_as
    USERS o|--o{ PRODUCTION_PLANS : assigned_to
    PRODUCTION_PLANS ||--o{ STEP_EXECUTIONS : executes
    PRODUCTION_STEPS ||--o{ STEP_EXECUTIONS : tracks
    USERS o|--o{ STEP_EXECUTIONS : performs_acknowledges
    PRODUCTION_PLANS ||--o| PRODUCTION_HISTORIES : records
    PRODUCTION_PLANS ||--o{ RESCHEDULE_LOGS : rescheduled_by
    USERS o|--o{ RESCHEDULE_LOGS : changes
    USERS o|--o{ AUDIT_LOGS : produces
```

Notes:

- `FORMULA_MATERIALS` is the junction table between formulas and materials.
- `PRODUCTION_STEPS` holds bilingual production instructions and QC-defined temperature limits.
- A production plan snapshots the planned batch, including its Worker-entered loss allowance.
- `STEP_EXECUTIONS` records the execution of every formula step within a production plan.
- `PRODUCTION_HISTORIES` has a one-to-one relationship with a completed production plan.
