# Demo verification — 30 October 2026

Tested through the browser at http://localhost:8102 with demo accounts.
The final instance stores its records in `data/browser-demo.mv.db`.

| Area | Browser result |
|---|---|
| Login / logout | All four roles sign in and reach their own workspace; language survives logout/login. |
| QC formulas | Created UI-QA-01 with new material UI-RM-01, submitted for CEO approval. |
| CEO approval | Approved from the workflow dashboard; formula became confirmed. |
| PC planning | Created October plan, selected approved formula, assigned Worker, saved 100-unit task, locked month. |
| Worker mobile | Checked 390 × 844 viewport, both languages and dark/light appearance. Empty loss percentage blocks Start. |
| Batch calculation | 100 + 5% = 105; 10 ML per unit = 1050 ML. Also 101 + 5% rounds extra units up to 6, total 107 and 1070 ML. |
| Actual output | 105 produced including 10 rejected = 95 good; 107 including 10 rejected = 97 good. |
| Warning | Abnormal output shows a confirmation checkbox; batch stays in progress until confirmed and submitted. |
| QC review | Saved notes in English and Thai. Recorded counts remain unchanged; review form disappears and dashboard pending count decreases. |
| Review status | Worker detail/list and PC planning show recorded review instead of pending review after QC saves. |
| Materials | Added UI-MAT-02 / Glycerin / G and searched by code. Search survives language switch. |
| Role navigation | QC has formulas/materials/quality; PC has formulas/planning without approval/create controls; Worker has task UI; CEO has all workspaces. |
| Locked plan | CEO added a task in locked month; PC view exposes no Add/Lock form. |
| Translation | Template message keys exist in both bundles. Actual browser checks cover login, role dashboards, materials, formulas, planning, Worker execution and QC review. User-entered names/notes are preserved as data. |

Automated verification: 7 JUnit tests passed, including quantity calculations,
normal production flow, abnormal production review, immutable recorded counts,
duplicate completion/review protection and locked-plan restriction.
HTTP smoke flow passed normal completion, invalid rejected count, TH/EN rendering,
cookie persistence and QC approval denial. Quality smoke passed Worker review denial,
CEO/PC locked-plan permissions, scaled materials, QC persistence and duplicate-review denial.

Fixed during browser testing: missing material totals on Worker screen; unclear inclusive
actual quantity; missing confirmation for abnormal counts; completed QC reviews counted as
pending; stale pending labels after review; empty planning heading in QC sidebar;
CEO Add task hidden in locked month. Versioned JS asset to avoid stale browser code.

This verifies the local demonstration flow. GCP deployment and physical-device testing
were not performed in this run; the mobile check used a browser viewport.
