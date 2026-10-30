# Formula Base

New clean implementation of the Formula Base university project.

The previous implementation is preserved at:

`C:\0Work\Formula Base Legacy`

The new implementation prioritizes accurate quantity calculations, explicit
loss allowance, traceable production plans, and a mobile-first worker screen.

## Run the local demo

Requires Java 17+ and Internet access on the first Maven run.

```powershell
.\scripts\start-demo.ps1
```

Open http://localhost:8080/login. This uses a separate persistent H2 database
at `data/browser-demo.mv.db`; it does not replace `data/formulabase.mv.db`.
Do not launch a second instance on the same port/database while it is running.

Demo accounts: `ceo`, `qc`, `pc`, `worker1`; password for all: `123456`.

1. QC creates a formula, using existing materials or creating new materials inline, then submits it.
2. CEO confirms the formula.
3. PC creates a monthly plan, assigns a worker and production date, then locks it.
4. Worker opens their due task, enters loss percentage (zero is valid), and starts the entire batch.
5. Worker sees material totals for target plus rounded-up extra product units, then records actual production including rejected units.
6. Abnormal counts require confirmation in the UI and appear in QC's quality-review page.
7. QC records a review note. Counts remain unchanged; the pending-review counter decreases.

CEO can use all workspaces and add tasks to a locked month. PC cannot change a locked month.
TH/EN persists through the locale cookie; light/dark theme persists in the browser.

## Verification

```powershell
.\mvnw.cmd test
# First script expects a fresh demo month (run against an isolated test instance).
.\scripts\demo-smoke.ps1 -Base http://localhost:8080
# Requires the confirmed HTTP test formula/month created by demo-smoke.
.\scripts\quality-smoke.ps1 -Base http://localhost:8080
```

Both smoke scripts create test records. Browser test results are in `DEMO_VERIFICATION.md`.
