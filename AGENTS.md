# AGENTS.md

Single-module Spring Boot 3.4 / Java 21 server-rendered app (Thymeleaf + Bootstrap, no SPA).
Maven is the only build tool. There is no lint/format/typecheck task:
`mvn test` is the whole verification gate. `google_checks.xml` is a stock Checkstyle file that is
**not** wired into the build — ignore it.

JMH microbenchmarks live in `src/benchmarks/java`, compiled and shaded **only** under the
`benchmarks` Maven profile (see section below); the default build and CI never see them.

## Architecture

- Package root `it.unisa.greenbottle`, sliced **by feature, not by layer**:
  - `controller/<feature>/` — `@Controller`s, plus `form/` (form-backing objects) and `util/`.
  - `storage/<feature>/{dao,entity}` — Spring Data JPA repositories and `@Entity` classes.
- Views live in `src/main/resources/templates/<Feature>View/*.html`; shared markup in
  `templates/fragments/`. Controllers return view names as strings **without a leading slash**,
  e.g. `"CatalogoView/Home"` — a leading slash yields `classpath:/templates//...`, which resolves
  on the exploded classpath but fails inside the packaged jar.
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
