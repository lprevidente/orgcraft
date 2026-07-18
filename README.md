# Orgcraft

[![Run Tests](https://github.com/lprevidente/ddd-example/actions/workflows/run-tests.yml/badge.svg)](https://github.com/lprevidente/ddd-example/actions/workflows/run-tests.yml)

A Spring Boot application exploring Domain-Driven Design (DDD) and CQRS with a toy team-management
domain. The point is
the **shape of the code**, not the feature set.

> **Note**: This is a learning project. Feedback and suggestions are welcome.

## Stack

| Layer              | Technology                                                          |
|--------------------|---------------------------------------------------------------------|
| Language           | Java 25                                                             |
| Framework          | Spring Boot 4.0.5                                                   |
| Persistence        | Spring Data JPA, Hibernate 7, PostgreSQL                            |
| Modularity         | Spring Modulith 2.0.5                                               |
| Authorization      | SpiceDB (ReBAC) over gRPC                                           |
| DDD semantics      | jMolecules (DDD + CQRS + events annotations, BOM `2025.0.2`)        |
| JPA translation    | `jmolecules-jpa` + `jmolecules-spring` via ByteBuddy (compile-time) |
| Architecture tests | `jmolecules-archunit` + ArchUnit                                    |
| Utilities          | Lombok, JSpecify, Jackson 3                                         |
| Dev DB             | PostgreSQL via Docker Compose                                       |
| Test DB            | H2 in-memory                                                        |

---

## Package Structure

The codebase is organized around **bounded contexts**. Each context is a top-level package and
follows the same internal
layout:

```
com.lprevidente.orgcraft/
├── Application.java
├── config/                          # Cross-cutting framework config (error handling)
├── common/                          # Shared kernel: identifier abstraction, authorization port + schema mirror
├── security/                        # Method security: hasPermission evaluator, @tenant SpEL bean
├── tenancy/                         # Per-request tenant context (multi-tenancy)
├── authorization/                   # SpiceDB adapter, schema bootstrap, domain-event → relationship listeners
└── <bounded-context>/
    ├── api/                         # Named interface exposed to other modules (optional)
    ├── application/
    │   ├── command/                 # Command records (write side)
    │   ├── handler/                 # Command handlers (one per command)
    │   ├── query/                   # Query services + read repositories
    │   └── projection/              # *View projection interfaces
    ├── domain/                      # Aggregates, value objects, repositories, events, exceptions
    └── infrastructure/              # Framework adapters (REST controllers, etc.)
```

### Key structural rules

- **`domain/`** is persistence-ignorant. No `@Entity`, `@Embeddable`, or `@Id` are hand-written —
  `jmolecules-jpa`
  translates `@AggregateRoot` → `@Entity`, `@ValueObject` → `@Embeddable`, `@Identity` →
  `@EmbeddedId`/`@Id` at
  compile time via ByteBuddy.
- **`application/`** owns orchestration only, split by CQRS concern: `command/` + `handler/` for the
  write side,
  `query/` (query services + read repositories) + `projection/` (`*View` interfaces) for the read
  side. No domain logic
  lives here.
- **`infrastructure/`** contains only framework adapters (`@RestController`). Controllers are
  package-private and inject
  handlers directly — no mediator or dispatcher.
- **Cross-module access** goes exclusively through a context's `api/` package (a Modulith named
  interface). Other
  contexts never reach into another's `domain/`.

---

## CQRS Write Side

Commands are plain records in `application/command/` annotated with `@Command`. Each command has
exactly one handler in
`application/handler/` annotated with `@Service`:

```
Controller  →  Handler.handle(command)  →  Domain aggregate  →  Repository
```

No mediator. Controllers inject the specific handler they need. The handler's `handle()` method is
annotated
`@CommandHandler` (jMolecules).

**Example flow — create a team:**

```
POST /api/v1/teams
  └─ TeamController.create(CreateTeam)
       └─ CreateTeamHandler.handle(CreateTeam)
            └─ new Team(name)           // domain constructor, registers TeamCreated event
                 └─ teams.save(team)    // Spring Data JPA
```

## CQRS Read Side

Reads never go through a command handler. The read side is fully separated from the write side:

- **Domain repositories** (`domain/`) have no projection methods — write operations only
- **Read repositories** (`application/query/`) extend Spring Data's `Repository<T,ID>` marker and
  expose only
  projection queries
- **Query services** (`application/query/`) return `*View` projection interfaces — no aggregates
  leak out

```
Controller  →  QueryService  →  ReadRepository.findXxx(Class<T>)  →  *View projection
```

Cross-context read enrichment: `TeamMemberQueryService` loads memberships, then calls `UserApi.findAllById(ids,
MemberView.class)` to hydrate user data from the user module.

---

## Authorization (ReBAC via SpiceDB)

Access control is **relationship-based** (ReBAC), backed by [SpiceDB](https://authzed.com/spicedb).
The domain stores *relationships* ("who relates to what"); SpiceDB *computes* permissions from them.
The application keeps no ACLs — it only ever asks SpiceDB two questions.

### The model

The schema lives in `resources/spicedb/schema.zed` and is the single source of truth.
`common/authorization/SpiceDbSchema` mirrors its type / relation / permission names in Java, so a
rename is a single-place change and a typo can't target a non-existent object.

| Resource       | Relations                                       | `view`                                    | `manage`                      |
|----------------|-------------------------------------------------|-------------------------------------------|-------------------------------|
| `organization` | `admin`, `member`                               | —                                         | `admin`                       |
| `team`         | `organization`, `creator`, `admin`, `member`    | `admin + member + organization→manage`    | `admin + organization→manage` |
| `office`       | `organization`, `creator`, `admin`, `occupant`  | `admin + occupant + organization→manage`  | `admin + organization→manage` |
| `user`         | `organization`, `office`                        | `organization→manage + office→manage`     | `organization→manage`         |

`→manage` is an *arrow*: a team/office inherits an org admin's power by following its `organization`
relation up to the org's `manage`. Promoting someone to team/office `admin` grants them `view` +
`manage` on that one resource.

The `user` resource **mirrors** the org/office edges it belongs to (`user#organization`,
`user#office`) — the reverse of `organization#member` / `office#occupant`. SpiceDB arrows only
traverse resource → subject, so those mirror edges are what let a *reverse* lookup like "which users
can this office admin see?" resolve.

### Two questions, two call sites

- **Guard one resource** — declarative, on the controller method (→ SpiceDB `CheckPermission`):
  ```java
  @PreAuthorize("hasPermission(#id, 'office', 'view')")
  ```
  Tenant-scoped creates read `@tenant.organizationId()`, a `CurrentTenant` SpEL bean:
  ```java
  @PreAuthorize("hasPermission(@tenant.organizationId(), 'organization', 'manage')")
  ```
- **Filter a list** — ask "which ids can this subject reach?", then filter the read model
  (→ SpiceDB `LookupResources`):
  ```java
  authorization.accessibleResourceIds(Type.OFFICE, Permission.VIEW, user.id())
      .map(officeQueryService::findAllByIds)   // some ids → filtered
      .orElseGet(officeQueryService::findAll);  // empty → authz disabled, return all
  ```

Both go through the `ResourceAuthorization` port (`common/authorization/`), implemented by
`SpiceDbResourceAuthorization` in the `authorization/` module. Reads run **fully consistent**
(read-your-writes) so a check reflects a tuple written by a just-processed event. The
`hasPermission(...)` SpEL is wired to the port by `ResourceAuthorizationPermissionEvaluator`
(`security/`), enabled via `@EnableMethodSecurity`.

### Relationships follow domain events

Tuples are never written inline. Aggregates raise domain events; `@ApplicationModuleListener`s in the
`authorization/` module translate them into `WriteRelationships` / `DeleteRelationships` calls:

```
AssignedUserToOffice           →  office:{id}#occupant@user:{u}  +  user:{u}#office@office:{id}
PromotedOfficeOccupantToAdmin  →  office:{id}#admin@user:{u}
UserRegistered                 →  organization:{org}#member@user:{u}  +  user:{u}#organization@organization:{org}
OrganizationDeleted            →  delete every organization:{id} tuple (+ mirror edges by subject)
```

Listeners run through Spring Modulith's event-publication registry (transactional, at-least-once).
`EventPublicationRetryConfig` resubmits failed publications and dead-letters those past the attempt
cap: a transient SpiceDB blip self-heals, a persistent failure is logged for reconciliation.

### Startup & toggling

- `SpiceDbSchemaBootstrap` (an `ApplicationRunner`) pushes `schema.zed` to SpiceDB on boot.
- `SpiceDbConfig` builds the gRPC channel; `orgcraft.spicedb.*` properties (`enabled`, `endpoint`,
  `preshared-key`, `plaintext`) configure it.
- **Everything above is `@ConditionalOnProperty("orgcraft.spicedb.enabled")`.** When disabled, a
  permit-all `ResourceAuthorization` fallback returns "everything / allowed" — this is how the H2
  test profile runs, so tests exercise handlers without a SpiceDB container.

The `http/` folder contains runnable end-to-end scenarios (`test-office-view.http`,
`test-user-view.http`, `test-team-view.http`) that walk the org-admin ▸ resource-admin ▸ member
hierarchy and assert the 200/403 outcomes — meaningful only with SpiceDB enabled.

---

## Domain Conventions

### Aggregates

- Annotated `@AggregateRoot` (jMolecules); `@Entity` added by transformer
- Protected no-arg constructor for Hibernate
- **Invariants are enforced in the constructor** — repositories and cross-module APIs are passed in
  so the aggregate can
  validate itself at creation time (e.g. checking uniqueness of a natural key, or verifying that a
  referenced entity in
  another context actually exists before establishing the relationship).

### Value Objects

- Records implementing `Identifier<UUID>` for typed IDs
- Annotated `@ValueObject` (jMolecules); `@Embeddable` added by transformer
- Compact constructors enforce invariants (e.g. format validation, strength or range rules)

### Domain Events

- Aggregates extend `AbstractAggregateRoot<T>` and register events from their constructor or domain
  methods
- Published automatically by Spring Data on `save()`

### Modulith boundaries

- `user/api/` is declared as a named interface — the only package other modules may import from
  `user`
- `common/identifier/` is a named interface shared across all modules
- `VeryModulithTest` verifies boundaries at test time via
  `ApplicationModules.of(Application.class).verify()`
- `DddRulesTest` runs `JMoleculesDddRules.all()` (ArchUnit) to verify aggregate/entity/value-object
  rules

---

## JPA Without JPA Annotations

The domain model is kept free of persistence annotations. The `byte-buddy-maven-plugin` runs
`JMoleculesPlugin` at
`process-classes` and rewrites bytecode:

| Source annotation                     | Added by transformer                  |
|---------------------------------------|---------------------------------------|
| `@AggregateRoot`                      | `@Entity`                             |
| `@ValueObject`                        | `@Embeddable`                         |
| `@Identity` on a value-object field   | `@EmbeddedId`                         |
| `@Identity` on a primitive/UUID field | `@Id`                                 |
| Non-null aggregate fields             | `@PostLoad`/`@PrePersist` null checks |
| `@Repository` (jMolecules)            | `@Repository` (Spring)                |
| `@Service` (jMolecules)               | `@Service` (Spring)                   |

Source classes only carry what the transformer can't derive: `@Table`, `@AttributeOverride`,
`@Column`.

To inspect the result: `javap -v -p target/classes/.../User.class`

---

## Running

**Prerequisites:** Java 25, Maven, Docker

```bash
./mvnw spring-boot:run          # starts app + Postgres + SpiceDB via Docker Compose
./mvnw test                     # run all tests (H2, SpiceDB disabled → permit-all)
```

Docker Compose brings up PostgreSQL, SpiceDB (`authzed/spicedb`, in-memory datastore, preshared key
`orgcraft-dev-key`) and a grpcui for inspecting SpiceDB. On boot the app writes `schema.zed` to
SpiceDB automatically. Set `orgcraft.spicedb.enabled=false` to run without it (permit-all).

---

## Learning & Contributions

If you have suggestions or see opportunities for better applying DDD concepts, feel free to open an
issue or a pull
request.
