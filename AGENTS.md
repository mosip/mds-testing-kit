# AGENTS.md

## Repository Overview

`mds-testing-kit` holds tools for testing MOSIP-compliant MOSIP Device
Service (MDS) implementations — three independent projects, not one build,
no root build file, no CI, no Dockerfile (verified against `master`):

- **`mds-test-ui`** — Angular UI for driving MDS conformance test runs.
- **`mosip-device-service`** — Spring Boot backend the UI talks to; composes
  test requests, calls the device under test, validates/reports responses.
- **`mosip-device-reg`** — two standalone Java CLI utilities to
  register/de-register biometric devices against a MOSIP Partner
  Management environment before running MDS tests:
  - `DeviceRegisterAndDeRegister` — maintained, runnable (own README at
    `mosip-device-reg/README`).
  - `DeviceRegister` — legacy; no assembly/main-class config, so it does
    not build a runnable jar. Reference only, do not try to run it.

## Technology Stack

- `mds-test-ui`: Angular 9 (CLI 9.0.1), TypeScript, Karma (unit), Protractor
  (e2e), Bulma/Angular Material.
- `mosip-device-service`: Java 11, Spring Boot 2.0.2 (WebFlux), Maven,
  Lombok, REST-assured, iText (PDF reports), Velocity (templating).
- `mosip-device-reg/DeviceRegisterAndDeRegister`: Java 11, plain Maven
  (no Spring), built via `maven-assembly-plugin` into a
  `jar-with-dependencies` CLI. Hibernate + PostgreSQL JDBC, REST-assured,
  TestNG.
- `mosip-device-reg/DeviceRegister`: same stack, no assembly plugin.

## Build & Test Commands

Each module builds from its own directory — no parent `pom.xml`/workspace.

### mds-test-ui

```bash
cd mds-test-ui
npm install
npm run start   # ng serve, http://localhost:4200
npm run build   # ng build -> dist/
npm run test    # Karma unit tests
npm run e2e     # Protractor e2e
npm run lint
```

Point it at your backend via `mds-test-ui/src/environments/environment.ts`
(`base_url`, default `http://localhost:8081/`).

### mosip-device-service

```bash
cd mosip-device-service
./mvnw clean install
./mvnw spring-boot:run   # mvnw.cmd on Windows
```

Listens on port `8081` (`server.port` in
`src/main/resources/bootstrap.properties`). REST controllers mounted at
`/testrunner` (`TestRunnerController`) and `/testmanager`
(`TestManagerController`) — check
`src/main/java/io/mosip/mds/controller/` for the exact endpoint list.

### mosip-device-reg/DeviceRegisterAndDeRegister

```bash
cd mosip-device-reg/DeviceRegisterAndDeRegister
mvn clean install
```

Produces `target/DeviceRegisterAndDeRegister-1.1-jar-with-dependencies.jar`
(main class `com.mosip.io.pmp.Runner`). Copy the `dataFolder` and `request`
directories into `target/` first (it reads input relative to the jar), then
run from inside `target/`:

```bash
ENV_USER="dev"
BASE_URL="https://dev.mosip.net"
java -Dtype=Iris -Denv.user=$ENV_USER -DbaseUrl=$BASE_URL \
  -jar DeviceRegisterAndDeRegister-1.1-jar-with-dependencies.jar
```

`-D` properties must come **before** `-jar` — after it, they become
program arguments the JVM never sees. `-Dtype` accepts `Finger`, `Face`,
`Iris`, `Auth`, or `All`. Full instructions (incl. running from an IDE) in
`mosip-device-reg/README`.

Only `mds-test-ui` has real tests (Karma/Protractor). `mosip-device-service`
and `mosip-device-reg` have no `src/test` despite declaring test deps
(TestNG) in `pom.xml` — `mvn test` there exercises nothing. Verify against
actual command output, not assumed coverage.

## Configuration

Config lives in properties/JSON files committed to the repo with
placeholder or shared-sandbox values, not real production secrets:

- `mosip-device-service/src/main/resources/application.properties` +
  `bootstrap.properties` — MOSIP/IDA/keymanager endpoints, DB connection
  string, `ida.auth.secretkey=<pwd>` and
  `javax.persistence.jdbc.password=<pwd>` placeholders.
  `application.properties` also has **live, non-placeholder** committed
  values — `auth.request.misplicense.key`, `auth.request.partnerid`,
  `auth.request.partnerapi.key` — treat as compromised, same as below.
- `mosip-device-service/data/config/masterdata.json` /
  `test-definitions.json` — test-case master data, copied to `target/data`
  at build time (`maven-resources-plugin`).
- `mosip-device-service/data/keys/PrivateKey.pem`, `PrivateKey_old.pem`,
  `rp-partner.p12` — real RSA key material + partner keystore, committed.
  **Compromised** — don't copy this pattern.
- `mosip-device-reg/DeviceRegisterAndDeRegister/src/main/resources/commonData.properties` —
  admin/partner login creds (`admin_password`, `partner_password`) +
  device-provider test data, committed plaintext. Live sandbox creds, same
  treatment.
- `.../DeviceRegisterAndDeRegister/src/main/resources/dbFiles/` — per-env
  Hibernate `cfg.xml` sets (`masterdata*`, `pms*`, `regdevice*` ×
  dev/qa/qa2/sandbox/extint as available).
- `mds-test-ui/src/environments/environment*.ts` — backend base URL.

No secret-injection mechanism exists — edit these files locally for a
different environment, and never commit real credentials over the
placeholder/sandbox values.

## Project Structure Notes

```text
mds-testing-kit/
├── mds-test-ui/                          Angular front end
├── mosip-device-service/                 Spring Boot backend (test runner/report API)
│   ├── data/                             config + key fixtures copied into target/ at build
│   └── src/main/java/io/mosip/mds/       controllers, dto, service, validator packages
└── mosip-device-reg/
    ├── DeviceRegisterAndDeRegister/       maintained device (de)registration CLI utility
    │   └── README (at mosip-device-reg/README) has full run instructions
    └── DeviceRegister/                    legacy, no runnable jar configured
```

`mosip-device-reg/DeviceRegister/target/` and
`DeviceRegisterAndDeRegister/testRun/logs/` (build output/logs) are already
tracked in git history on `master`. Check `git status` before committing to
avoid re-adding regenerated artifacts.

## Development Workflow

1. Fork and clone.
2. GitHub reports `master` as default, but **`develop` is the active
   integration branch** (ahead of `master`; recent PRs target it) — branch
   from and PR against `develop` unless specifically backporting.
3. Work inside one module; build/run only that module's commands (above) —
   no top-level build exists. For root-only doc changes (this file),
   validate Markdown/command references instead of running a module build.
4. No CI exists — your local build/test run is the only verification step.

## Pull Request Guidelines

- Target `develop` (see Development Workflow) unless backporting to
  `master`.
- Reference the tracking issue in the PR description.
- No CI: state in the PR body what you ran locally and the result.
- Keep changes scoped to one module per PR — the three are independent.

## Repository-Specific Considerations

- Older, lightly-maintained (Spring Boot 2.0.2, Angular 9, Java 11). Don't
  bump major framework versions as a side effect of an unrelated change.
- Committed credentials/keys (see Configuration) are live sandbox material,
  already compromised — not a pattern to follow. Flag to a human reviewer
  before adding any new real credential anywhere in the tree.

## Agent rules

### Do

1. Confirm which module an issue is about before editing — build systems
   and READMEs are independent per module.
2. Run the touched module's own build/test command and report actual
   output in the PR (or the doc/command checks performed, for a root-only
   doc change).
3. Put JVM `-D` system properties before `-jar`.
4. Quote/name placeholder values explicitly (e.g. `ENV_USER="dev"`) instead
   of bare `<placeholder>` tokens in example commands.
5. Check `git status` before committing to avoid re-adding regenerated
   `target/`/`dist/`/log output.

### Do not

1. Don't assume `master` is where feature work lands — `develop` is active;
   confirm before opening a PR.
2. Don't invent a CI workflow or Dockerfile (neither exists), or assume
   `mosip-device-service`/`mosip-device-reg` have real tests — they don't
   (`mds-test-ui` is the one exception).
3. Don't commit real credentials/keys/connection strings over the
   placeholder/sandbox values anywhere in Configuration.
4. Don't try to build `mosip-device-reg/DeviceRegister` as a jar — no
   assembly/main-class config exists for it.
5. Don't merge the three modules' build systems or assume a shared parent
   POM/workspace.
