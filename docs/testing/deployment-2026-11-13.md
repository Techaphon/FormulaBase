# Deployment — 2026-11-13

- Build: 68dff75a-adca-4e79-9814-17d656f54fce (SUCCESS)
- Image digest: sha256:cb0b8815187b3429e6ccfd6f402cc5aacda3fa819ec645ea0637ea3782ab8824
- Service: formula-base; project: formulabase-demo; region: asia-southeast3
- Previous revision: formula-base-00018-nm4
- Active revision: formula-base-00019-foj, 100% traffic, Ready=True
- Local clean package: 35 tests passed, zero skipped, including PostgreSQL 17.11 migration/schema and concurrent authorization regressions.
- Fixed stale actor authorization in user management by revalidating the actor inside the transaction under deterministically ordered administrator locks.
- Live startup logs: schema migrated from V8 to V9 successfully; PostgreSQL 17.11 connected; application started.
- Candidate HTTP checks: Thai/English login, new table JS/CSS, authentication redirects for /audit and /worker/tasks passed using curl. Python urllib encountered a client read timeout; independent curl checks completed successfully.
- Main URL login returned HTTP 200 after promotion. Temporary verification tag removed.
- No ERROR-level logs found during final deployment check.
- Full authenticated browser interaction, mobile touch resizing, and print dialog visual checks remain unverified because browser automation was unavailable. Do not interpret HTTP checks as complete visual QA.

Rollback application traffic (additive V9 column remains):

```powershell
gcloud run services update-traffic formula-base --project formulabase-demo --region asia-southeast3 --to-revisions=formula-base-00018-nm4=100
```
