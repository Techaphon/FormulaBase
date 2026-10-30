# Formula Base v2 Rebuild Plan

## Goal

Build a small, reliable university project focused on accurate formula scaling,
production quantity planning, and worker execution with loss allowance.

The domain includes a `MonthlyPlanningCycle` parent record and multiple
`ProductionPlan` tasks beneath it. This keeps the PC's once-per-month planning
workflow explicit instead of trying to infer a monthly cycle from individual dates.

## Planning permissions

| Role | Monthly plan | Loss percentage | Daily execution |
|---|---|---|---|
| PC | Create, edit in draft, lock | View | View planning status |
| Worker | View today's assigned tasks | Enter before Start | Start and complete own Batch |
| CEO | View and edit any plan, including locked plans | View/edit with audit | View oversight |
| QC | View relevant formula/output data | Review quality exceptions | Confirm quality when required |

CEO is the system-wide administrator for this project: CEO may perform every
available business function, while actions that are specifically assigned to
another role remain available to that role as well. Formula confirmation is an
intentional exception to the general workflow: only CEO can make the final
confirmation decision.

## Delivery order

1. Domain rules and quantity calculation tests
2. Entities and H2 persistence
3. Formula and material CRUD
4. Formula approval workflow
5. Monthly production planning and loss allowance
6. Worker mobile execution from the locked monthly plan
7. Thai/English UI using message keys
8. Role smoke tests and production profile

## Quantity rules

- Target quantity is the number of good finished products required by the order.
- Loss allowance is entered as a percentage of the target product quantity and is
  converted into additional product units.
- Planned production quantity is `target quantity + rounded-up(target quantity * loss rate)`.
- Example: target `100` units with a 5% allowance = 5 extra units, so planned
  production is `105` units.
- When the calculated allowance is fractional, round it up so the allowance never
  understates the requested percentage. Example: 101 units at 5% = 5.05, rounded
  up to 6 allowance units, so planned production is 107 units.
- Formula material quantities are defined per one finished product unit.
- PC defines the target quantity for each Production Plan/Task. Raw-material
  requirements are calculated as `per-unit material quantity × planned product
  quantity`, including the loss allowance units.
- QC selects the material unit from a controlled dropdown (for example g, kg, ml,
  L, or piece). The system stores the calculated value at full precision and
  applies display precision only when presenting it to users.
- The system must distinguish target, allowance, planned quantity, actual good
  quantity, actual rejected/lost quantity, and remaining quantity.
- Product quantities are whole non-negative units. The loss percentage uses
  `BigDecimal`, while the calculated allowance and product counts use whole units.
- Every adjustment records the actor, timestamp, previous value, new value, and reason.

## Formula approval workflow

- QC creates and edits Formula drafts, including the per-unit material quantities.
- QC submits the Formula for CEO confirmation.
- Only CEO can confirm or reject a submitted Formula (CEO may also manage a
  draft directly when needed).
- PC can select only CEO-confirmed Formula versions for a Monthly Planning Cycle.
- A Formula that is not confirmed must never be used to create a Production Plan.

## Actual output and loss recording

- A Worker start action starts one production run for the entire Production Plan;
  it does not represent producing one product unit.
- Progress is tracked by production step for the whole Batch. For example, one
  Start action may begin Step 1 for all 105 planned units.
- The Worker completes each step for the Batch, then records the final total
  produced and rejected/lost quantities for that run.
- Worker records total units produced and rejected/lost units at the end of a batch.
- Good units are calculated as `total produced - rejected/lost` and cannot be negative.
- The system shows target, planned, produced, good, rejected/lost, and remaining units.
- A batch can be completed when good units meet the target and the required QC
  confirmation is complete.
- If rejected/lost units exceed the calculated allowance, the batch is marked as an
  exception and requires PC/QC review with a reason before final closure.

## Immutable production history

- When a Worker starts a Batch, the system snapshots the Formula revision, target
  quantity, loss percentage, allowance units, planned quantity, assigned Worker,
  and the start timestamp.
- When the Batch is completed, the system records actual produced units, rejected /
  lost units, calculated good units, surplus units, completion timestamp, and any
  exception reason.
- A later Formula revision, CEO plan edit, or new Loss percentage must not rewrite
  the historical values used by a completed Batch.
- Corrections are recorded as an adjustment with before/after values, actor,
  timestamp, and reason instead of silently overwriting the original history.

## Monthly PC planning cycle

- PC performs planning once per month, creating the production schedule for the
  entire selected month rather than opening individual batches during daily work.
- A monthly planning cycle is identified by `year-month` (for example `2026-11`)
  and contains the month's Production Plan tasks/batches.
- The cycle moves through `DRAFT -> SUBMITTED -> LOCKED`.
- PC can add and edit tasks while the cycle is in DRAFT. Submitting the cycle
  validates dates, assigned workers, formulas, and quantities for the month.
- PC can lock the monthly cycle directly. Once LOCKED, it is the source of truth
  for Worker tasks and PC cannot casually change it during daily execution.
- The Worker is responsible for entering the loss percentage for the assigned task
  before starting production. The system then calculates allowance units and
  planned production quantity for that task.
- Loss percentage is not a reusable Formula default. It is a situational value
  entered by the Worker for each Production Plan and is mandatory before Start.
- Once the Worker starts the Batch, the exact loss percentage used for that task
  is stored as an immutable calculation snapshot. This is historical evidence,
  not a value that another role can silently pre-fill or reuse.
- Worker sees today's assigned tasks plus overdue tasks carried over from a prior
  day and tasks rescheduled to today, not the entire future monthly schedule.
- CEO can view all monthly plans and may edit a locked plan. CEO edits must record
  the previous value, new value, timestamp, and reason so the monthly plan remains
  traceable.
- The PC screen is a monthly calendar/list view; the Worker screen is a focused
  today-only task list.

## Scope boundary

Keep the architecture as one Spring Boot application. Do not add microservices,
complex permission matrices, or unrelated enterprise features.
