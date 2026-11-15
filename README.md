# Formula Base

Formula Base is a manufacturing workflow application for managing product formulas, raw materials, monthly production plans, and production records. It supports formula review and approval, work assignments, step-by-step production, quality review, and printable work orders. The application provides separate workspaces for management, quality control, production control, and production workers, with Thai and English interfaces.

## Group members and responsibilities

<!-- Replace every placeholder with a member's real student ID, full name, and actual contributions before submission. Add or remove rows as needed. -->

| Student ID | Full name | Features or work responsible for |
| --- | --- | --- |
| [6831503040] | [Nathida Somkwamkid] | [Quality Exception Function] |
| [6831503042] | [Natchanon Inpha] | [print pages Function] |
| [6831503046] | [Nicha Hiruncharoen] | [logout function] |
| [6831503048] | [Techaphon Srebencharat] | [Core system] |
| [6831503060] | [Ploychompoo Ngamratprasert] | [planning status filter & navigator function] |

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

2. Start the application using a fresh, temporary H2 database. In Windows PowerShell:

   ```powershell
   .\mvnw.cmd spring-boot:run '-Dspring-boot.run.arguments=--server.port=8080 --spring.datasource.url=jdbc:h2:mem:formulabase;DB_CLOSE_DELAY=-1'

   ```mvn spring-boot:run

   On macOS or Linux:

   ```sh
   chmod +x mvnw
   ./mvnw spring-boot:run '-Dspring-boot.run.arguments=--server.port=8080 --spring.datasource.url=jdbc:h2:mem:formulabase;DB_CLOSE_DELAY=-1'
   ```

3. Open [http://localhost:8080/login](http://localhost:8080/login). Stop the application with `Ctrl+C` in the terminal.

The temporary database starts empty on each run and is deleted when the application stops. The application creates the demo accounts below automatically. This command avoids using an older H2 database file that may be present in the repository. For a different port, change `--server.port=8080` and the URL accordingly.

### Local demo accounts

These accounts are seeded only for local non-production use. They are not production credentials. Each account uses the demo password `123456`.

| Username | Role |
| --- | --- |
| `ceo` | CEO / management |
| `qc` | Quality control |
| `pc` | Production control |
| `worker1` | Production worker |

### Suggested demo workflow

1. Sign in as QC, create materials and a formula with production steps, and submit the formula for approval.
2. Sign in as CEO and review the submitted formula.
3. After approval, sign in as PC, create a monthly production plan, assign a worker and date, and lock the plan.
4. Sign in as the assigned worker on the scheduled day, perform the production steps, and record the total produced and the rejected quantity.
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