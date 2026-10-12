# AGENTS.md

Single-module Spring Boot 3.4 / Java 21 server-rendered app (Thymeleaf + Bootstrap, no SPA).
Maven is the only build tool. There is no lint/format/typecheck task:
`mvn test` is the whole verification gate. `google_checks.xml` is a stock Checkstyle file that is
**not** wired into the build — ignore it.

## Commands

- Build + all tests: `.\mvnw.cmd -B package` (Windows) / `mvn -B package` (what CI runs)
- Single test class: `.\mvnw.cmd -B test -Dtest=UtentiTest`
- Run app: `.\mvnw.cmd spring-boot:run`, artifact is `target/greenbottle-0.0.1-SNAPSHOT.jar`
- Tests need MySQL credentials overridden (see below):
  `.\mvnw.cmd -B test -Dspring.datasource.password=peppe`

## Database prerequisites (tests fail without this)

- Tests hit a **real MySQL 8** at `localhost:3306`, database `greenbottledb`. There is no
  H2/testcontainers fallback: `@SpringBootTest` boots the real datasource and `dataJPATests`
  use `@AutoConfigureTestDatabase(replace = NONE)`.
- Credentials are hardcoded in `src/main/resources/application.properties` as `root`/`root`
  (commented "Only for testing"). On this machine the local root password is `peppe`
  (see `start.bat`), so pass `-Dspring.datasource.password=peppe` or tests fail with a
  misleading `Unable to determine Dialect without JDBC metadata`.
- Schema comes from Hibernate `ddl-auto=update`. `configurationFiles/databaseInit.sql` creates
  the empty schema; `configurationFiles/databasePopulator.sql` seeds demo data.
- **Tests are not isolated from DB state and write real rows.** Running the populator before
  tests makes `dataJPATests` (`AbbonamentoTest`, `CatalogoTest`, `OrdineTest`) fail with
  `Duplicate entry ... PRIMARY`: seed rows occupy IDs 1..N while the `*_seq` counters stay at 1.
  Re-running advances the counters; the reliable baseline is a DB with only the schema
  (`databaseInit.sql` or empty). CI (`.github/workflows/maven.yml`) creates an empty
  `greenbottledb` (root/root) and runs `mvn -B package` on push/PR to `master` — that is the
  green baseline (50 tests).
- `start.bat` is a personal local shortcut (hardcoded path + password), not a project entry point.

## Architecture

- Package root `it.unisa.greenbottle`, sliced **by feature, not by layer**:
  - `controller/<feature>/` — `@Controller`s, plus `form/` (form-backing objects) and `util/`.
  - `storage/<feature>/{dao,entity}` — Spring Data JPA repositories and `@Entity` classes.
- Views live in `src/main/resources/templates/<Feature>View/*.html`; shared markup in
  `templates/fragments/`. Controllers return view names as strings, e.g. `"/CatalogoView/Home"`.
- The only JSON/byte endpoints are two `@RestController`s streaming images: `/productImg`
  (`RenderImg`) and `/userImg` (`RenderCliente`, which reads the fallback PNG from
  `src/main/resources/...` as a filesystem path). Everything else is MVC + redirects.
- Auth is hand-rolled session state, **not** Spring Security:
  - session state in `@SessionScope @Component` beans (`SessionCliente`, `SessionAdmin`,
    `SessionCarrello`), surfaced to templates via `@ModelAttribute`.
  - route protection = servlet filters in `controller/filters/`, whose URL patterns are
    registered in `*ConfigFilter` (`/admin*`, `/areaPersonale*`, `/carrello`). A new protected
    route must be added there, otherwise it is public.
- Errors: controllers call `response.sendError(status, "msg")`; `CustomErrorController` renders
  `/error` (whitelabel disabled). `messages.properties` holds `typeMismatch.*` messages.

## Conventions worth keeping

- MockMvc tests assert `result.getResponse().getErrorMessage()` **verbatim** — changing
  user-facing error strings breaks tests in `src/test/.../{accessoTest,ordineTest,catalogoTest,...}`.
- Javadoc/comments are in Italian; public classes and methods carry Javadoc. 2-space indent,
  Lombok (`@Data`, `@AllArgsConstructor`, ...) on entities and forms (Lombok is registered as an
  annotation processor in `pom.xml`).
- Several tests use the bare `assert` keyword (alongside JUnit assertions); it is only active
  with `-ea`, which Surefire enables by default. Running those tests from a bare JUnit launcher
  without `-ea` makes them pass vacuously.
- `docs/` contains committed Javadoc + JaCoCo HTML that no build plugin regenerates; `projectDocs/`
  has process docs. Don't regenerate or tidy them as part of a normal change.
- README run steps are stale (typo `./mwnw`, jar name `GreenBottle-1.0.jar`, en-dash JVM args) —
  trust `pom.xml` and this file.
