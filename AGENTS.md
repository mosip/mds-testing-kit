# AGENTS.md

## Repository Overview

`mds-testing-kit` holds tools for testing MOSIP-compliant Mobile/Biometric
Device Service (MDS) implementations. It is a loose collection of three
independent projects, not a single build:

- **`mds-test-ui`** — an Angular web UI for driving MDS conformance test
  runs and viewing results.
- **`mosip-device-service`** — a Spring Boot backend that composes test
  requests, calls the device under test, and validates/reports responses.
  This is the service the UI talks to.
- **`mosip-device-reg`** — two separate, standalone Java command-line
  utilities used to register/de-register biometric devices against a MOSIP
  Partner Management environment before running MDS tests:
  - `DeviceRegisterAndDeRegister` — the maintained utility (has its own
    README at `mosip-device-reg/README`).
  - `DeviceRegister` — an older module with the same general purpose. Its
    `pom.xml` has no assembly/main-class configuration, so it does not
    build a runnable jar the way `DeviceRegisterAndDeRegister` does. Treat
    it as legacy reference code, not the tool to run.

There is no root build file, no `.github/workflows` CI pipeline, and no
Dockerfile anywhere in this repository (verified against the `master`
branch tree) — each module is built and run independently, by hand.

## Technology Stack

- `mds-test-ui`: Angular 9 (Angular CLI 9.0.1), TypeScript, Karma
  (unit tests), Protractor (e2e), Bulma/Angular Material for styling.
- `mosip-device-service`: Java 11, Spring Boot 2.0.2 (WebFlux), Maven,
  Lombok, REST-assured, iText (PDF report generation), Velocity
  (report templating).
- `mosip-device-reg/DeviceRegisterAndDeRegister`: Java 11, plain Maven
  project (no Spring), built into a single `jar-with-dependencies` via
  `maven-assembly-plugin`, run as a CLI tool. Uses Hibernate + PostgreSQL
  JDBC driver, REST-assured, TestNG.
- `mosip-device-reg/DeviceRegister`: same stack as above but without the
  assembly plugin configured — not designed to be run as a standalone jar.

## Build & Test Commands

Each module is built from inside its own directory — there is no parent
`pom.xml` or workspace file tying them together.

### mds-test-ui

```bash
cd mds-test-ui
npm install
npm run start   # ng serve, dev server at http://localhost:4200
npm run build   # ng build, output in dist/
npm run test    # ng test, Karma unit tests
npm run e2e     # ng e2e, Protractor end-to-end tests
npm run lint    # ng lint
```

Before running the dev server, point it at your backend by editing
`mds-test-ui/src/environments/environment.ts` (`base_url`, default is
`http://localhost:8080/`).

### mosip-device-service

```bash
cd mosip-device-service
./mvnw clean install
./mvnw spring-boot:run
```

On Windows use `mvnw.cmd` instead of `./mvnw`. The service listens on
port `8080` by default (`server.port` in
`src/main/resources/bootstrap.properties`). Its two REST controllers are
mounted under `/testrunner` (`TestRunnerController`) and the equivalent
path in `TestManagerController` — read those classes under
`src/main/java/io/mosip/mds/controller/` for the exact endpoint list
before wiring a client against them.

### mosip-device-reg/DeviceRegisterAndDeRegister

```bash
cd mosip-device-reg/DeviceRegisterAndDeRegister
mvn clean install
```

This produces `target/DeviceRegisterAndDeRegister-1.1-jar-with-dependencies.jar`
(main class `com.mosip.io.pmp.Runner`). To run it from the command line,
copy the `dataFolder` and `request` directories into `target/` first (the
utility reads its input files relative to the jar), then run from inside
`target/`:

```bash
ENV_USER="dev"
BASE_URL="https://dev.mosip.net"
java -Dtype=Iris -Denv.user=$ENV_USER -DbaseUrl=$BASE_URL \
  -jar DeviceRegisterAndDeRegister-1.1-jar-with-dependencies.jar
```

Note the `-D` system properties come **before** `-jar` — placing them
after the jar path passes them as program arguments instead, and the JVM
will not see them. `-Dtype` accepts `Finger`, `Face`, `Iris`, `Auth`, or
`All`. Full step-by-step instructions (including running from an IDE) are
in `mosip-device-reg/README`.

There is no dedicated automated test suite (`mvn test` target with real
tests) in any of the three modules as of this writing — verify with
`mvn test` / `npm run test` output on the module you touch rather than
assuming coverage exists.

## Configuration

Configuration for each module lives in plain properties/JSON files that
are checked directly into the repository, with placeholder or shared
sandbox-environment values rather than real production secrets:

- `mosip-device-service/src/main/resources/application.properties` and
  `bootstrap.properties` — MOSIP base URL, IDA/keymanager endpoints, DB
  connection string, and a `<pwd>` placeholder for the DB password.
- `mosip-device-service/data/config/masterdata.json` and
  `test-definitions.json` — test case master data, copied into
  `target/data` at build time by the `maven-resources-plugin` binding in
  `pom.xml`.
- `mosip-device-service/data/keys/PrivateKey.pem` and
  `PrivateKey_old.pem` — pre-existing test-only key files already
  committed to this repo. Do not add real/production private keys next
  to them, and do not assume these are safe to reuse for anything beyond
  local test runs.
- `mosip-device-reg/DeviceRegisterAndDeRegister/src/main/resources/commonData.properties` —
  admin/partner login credentials and device-provider test data (checked
  in with default MOSIP sandbox test credentials).
- `mosip-device-reg/DeviceRegisterAndDeRegister/src/main/resources/dbFiles/` —
  one Hibernate `cfg.xml` per target environment (dev, qa, qa2, sandbox,
  extint).
- `mds-test-ui/src/environments/environment.ts` /
  `environment.prod.ts` — backend base URL used by the Angular app.

If you need to point any module at a different MOSIP environment or
credential set, edit these files locally and **do not commit** real
credentials over the placeholder/sandbox values — there is no secret
injection script or `--set`-style mechanism in this repo; config is read
directly from these files.

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
    └── DeviceRegister/                    older module, no runnable jar configured
```

The `mosip-device-reg/DeviceRegister/target/` and
`DeviceRegisterAndDeRegister/testRun/logs/` directories contain build
output and log files that are already tracked in git history on
`master`. Avoid adding new build artifacts or logs to commits — check
`git status` before committing to catch anything your local Maven/Angular
build regenerated in place.

## Development Workflow

1. Fork the repo and clone your fork.
2. Branch from `master` — this is the repository's actual default
   branch (verify with `gh repo view mosip/mds-testing-kit --json
   defaultBranchRef` if unsure; it is not `develop` here even though
   many other MOSIP repos use `develop`).
3. Make changes inside the one module you are working on. Build and run
   that module's own commands (above) to verify your change — there is
   no top-level command that builds all three at once.
4. Since there is no CI workflow defined in this repository, your local
   build/test run is the only verification step before opening a PR —
   run it and check the output yourself.

## Pull Request Guidelines

- Target the `master` branch on `mosip/mds-testing-kit`.
- Reference the relevant tracking issue in the PR description.
- Since there is no CI pipeline here, describe in the PR body what you
  ran locally (e.g. `npm run build`, `mvn clean install`) and the
  result, so reviewers know what was actually verified.
- Keep changes scoped to one module per PR where practical, given the
  three modules are functionally independent.

## Repository-Specific Considerations

- This is an older, lightly-maintained test-tooling repository (Spring
  Boot 2.0.2, Angular 9, Java 11 across all Java modules). Do not
  casually bump major framework versions as a side effect of an
  unrelated change.
- `mosip-device-reg` contains two similarly-named modules
  (`DeviceRegister` and `DeviceRegisterAndDeRegister`) that are easy to
  confuse. Only `DeviceRegisterAndDeRegister` has a documented, runnable
  build; check which one an issue actually refers to before editing.
- Credentials and keys in this repo (see Configuration above) are
  already-committed test fixtures for shared MOSIP sandbox environments,
  not secrets you introduced. Treat any new secret-like value the same
  way, but flag to a human reviewer before adding new real credentials
  anywhere in the tree.

## Agent rules

### Do

1. Verify which module (`mds-test-ui`, `mosip-device-service`,
   `mosip-device-reg/DeviceRegisterAndDeRegister`, or the legacy
   `mosip-device-reg/DeviceRegister`) an issue is actually about before
   editing, since their build systems and READMEs are independent.
2. Run that module's own build/test command (npm or Maven, as listed
   above) after making a change, and report the actual output in the PR.
3. Put JVM `-D` system properties before `-jar` in any command you write
   or document.
4. Quote or name placeholder values explicitly (e.g. `ENV_USER="dev"`)
   instead of writing bare `<placeholder>` tokens in example commands.
5. Check `git status` before committing to avoid accidentally
   re-committing regenerated build output (`target/`, `dist/`, logs).

### Do not

1. Do not assume `develop` is the default branch here — it is `master`;
   confirm before branching if this ever changes.
2. Do not invent a CI workflow, Dockerfile, or automated test suite that
   does not exist in this repository — none of the three modules has one
   as of this writing.
3. Do not commit real credentials, keys, or connection strings over the
   placeholder/sandbox values in `application.properties`,
   `bootstrap.properties`, `commonData.properties`, or the `data/keys/`
   PEM files.
4. Do not try to build `mosip-device-reg/DeviceRegister` as a runnable
   jar — its `pom.xml` has no assembly/main-class configuration for that.
5. Do not merge the three modules' build systems or assume a shared
   parent POM/workspace exists — there isn't one.
