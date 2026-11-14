# Formula Base

Formula Base is a manufacturing workflow application for managing product formulas, raw materials, monthly production plans, and production records. It supports formula review and approval, work assignments, step-by-step production, quality review, and printable work orders. The application provides separate workspaces for management, quality control, production control, and production workers, with Thai and English interfaces.

## Group members and responsibilities

<!-- Replace every placeholder with a member's real student ID, full name, and actual contributions before submission. Add or remove rows as needed. -->

| Student ID | Full name | Features or work responsible for |
| --- | --- | --- |
| [6831503040] | [Nathida Somkwamkid] | [Actual contributions] |
| [6831503042] | [Natchanon Inpha] | [Actual contributions] |
| [6831503046] | [Nicha Hiruncharoen] | [Actual contributions] |
| [6831503048] | [Techaphon Srebencharat] | [Actual contributions] |
| [6831503060] | [Ploychompoo Ngamratprasert] | [Actual contributions] |

## Requirements

- Java 17 or later (JDK).
- Internet access on the first run to download Maven and project dependencies.
- Port 8080 available on the local machine.

The project includes the Maven Wrapper, so a separate Maven installation is not required.

## Run locally

1. Clone this repository and open a terminal in its root directory, where `pom.xml` is located.

   ```sh
   git clone https://github.com/Techaphon/FormulaBase.git
   cd FormulaBase
   ```

2. Start the application with the small local sample dataset and a fresh, temporary H2 database. If Maven is installed, run:

   ```sh
   mvn spring-boot:run "-Dspring-boot.run.profiles=dev,local-demo"
   ```

   Alternatively, use the included Maven Wrapper. In Windows PowerShell:

   ```powershell
   .\mvnw.cmd spring-boot:run '-Dspring-boot.run.profiles=dev,local-demo'
   ```

   On macOS or Linux:

   ```sh
   chmod +x mvnw
   ./mvnw spring-boot:run '-Dspring-boot.run.profiles=dev,local-demo'
   ```

3. Open [http://localhost:8080/login](http://localhost:8080/login). Stop the application with `Ctrl+C` in the terminal.

The `local-demo` profile uses an in-memory H2 database and creates four demo accounts, two sample materials, an approved sample formula with two steps, and one task assigned to `worker1` for today in Bangkok time. It is disposable test data, not a production recipe. The sample data and any changes disappear when the application stops. For another port, append `"-Dspring-boot.run.arguments=--server.port=8081"` and open port 8081 instead. The sample profile refuses non-memory databases and is disabled when `prod` is active.

Running plain `mvn spring-boot:run` uses the default `dev` profile and its local file-based H2 database instead; it does not load this sample dataset and may use an existing database file. Use the explicit command above for a clean, repeatable demo.

### Local demo accounts

These accounts are seeded only for local non-production use. They are not production credentials. Each account uses the demo password `123456`.

| Username | Role |
| --- | --- |
| `ceo` | CEO / management |
| `qc` | Quality control |
| `pc` | Production control |
| `worker1` | Production worker |

### Suggested demo workflow

1. Sign in as QC, inspect the sample materials and formula, or create a new formula with production steps and submit it for approval.
2. Sign in as CEO and review the submitted formula.
3. Sign in as PC to inspect the sample monthly plan, or plan another approved formula and assign a worker.
4. Sign in as `worker1`, open today's sample task, perform the production steps, and record the total produced and the rejected quantity.
5. Review production progress and records using the appropriate role. QC can review items that require quality attention.

The exact actions available depend on the account role and the current state of the formula or production plan.

## Run tests

In Windows PowerShell:

```powershell
.\mvnw.cmd test
```

On macOS or Linux:

```sh
./mvnw test
```

Some PostgreSQL integration tests require a separately configured test database and may be skipped when it is unavailable. The local demo above does not require Supabase or a PostgreSQL server.

## Production configuration

The `prod` profile uses PostgreSQL and Flyway migrations. Configure `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD` through the deployment environment or a secret manager. Do not commit real credentials, tokens, database dumps, or production data to the repository. Local demo accounts are not automatically seeded by the production profile.
