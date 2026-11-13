# PostgreSQL review — 2026-11-12

Scope: isolated PostgreSQL 17.11 cluster on loopback port 55439. No production or Supabase data changed. Synthetic audit data only.

## Passed
- Application production Flyway initialization V1–V9, followed by Hibernate schema validation.
- V9 upgrade scenarios with existing users: active ceo selected; inactive ceo skipped; archived ceo skipped. Each retained both original rows and protected exactly one eligible CEO.
- Two concurrent service requests to delete and disable the primary administrator: both rejected; primary remained active and unarchived.
- PostgreSQL-backed HTTP rendering: audit list, translated action search, action filter plus page 2, history, users, Worker tasks; all returned 200.

## Performance
100,000 synthetic audit rows. Local warm database; not Cloud Run/Supabase network latency.
Seven measured service calls after one warmup:

| Service request | Median ms | Maximum ms |
| --- | ---: | ---: |
| First page, no search | 22.06 | 26.30 |
| Review batch quality | 53.02 | 56.02 |
| Synthetic benchmark 99999 | 49.70 | 54.52 |

Five measured HTTP requests after one warmup, including HTML rendering:

| HTTP request | Median ms | Maximum ms |
| --- | ---: | ---: |
| /audit | 67.6 | 77.6 |
| /audit?keyword=Review%20batch%20quality | 95.2 | 200.2 |
| /audit?action=QUALITY_REVIEW&page=1 | 68.6 | 70.8 |

EXPLAIN ANALYZE for ordered first 50 audit rows: index scan using idx_audit_created with incremental sort; execution 0.233 ms (excludes count, options, ORM and rendering).

## Defect found during review — fixed in pre-deploy verification
The original run of `PostgresReviewTest.requestCapturedBeforeDeactivationMustNotManageUsers` failed. The subsequent fix revalidates the persisted administrator under ordered row locks inside each user-management transaction. The regression now passes.
1. Load active non-primary CEO as the actor of an in-flight request.
2. Another administrator disables that account and commits.
3. Invoke UserService.create with the previously loaded actor.
4. Expected: reject inactive actor. Actual: account creation succeeds.

UserService verifies role on the detached actor but does not re-read/lock the acting user's current authorization state. This is an in-flight authorization race, not evidence that a disabled account can log in afresh. Fix should revalidate the actor within the mutation transaction and use consistent lock ordering for operations involving several users. Then rerun the failing regression and simultaneous cross-account operations.

## Not verified
- Visual browser, touch resizing and real print dialog: CUA browser access unavailable; native browser launch also failed.
- Supabase-host-specific permissions and network performance.
- Primary administrator transfer: no transfer feature exists yet.

## Reproduction
The opt-in tests WRITE synthetic data. Use a fresh, disposable LOCAL database only, with a local test role named review_admin. Never point them at real data.

```powershell
$env:FORMULABASE_TEST_PG_URL='jdbc:postgresql://127.0.0.1:55439/review_tests'
.\mvnw.cmd '-Dtest=PostgresReviewTest' test
```

The class now contains five passing PostgreSQL checks, including the fixed stale-actor regression and simultaneous cross-account deletion. Full pre-deploy clean package passed 35 tests with zero skipped. Without this environment variable the PostgreSQL checks are skipped. The initial review was test-only. The subsequent deployment task includes the UserService authorization fix. See the deployment report for rollout status.
