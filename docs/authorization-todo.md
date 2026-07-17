# SpiceDB / Authorization — entity relations TODO

Status of the ReBAC model as mirrored into SpiceDB. The **write side** (populating
relationships from domain events) is mostly done; permissions and the check side are
deliberately deferred.

## Current schema (relations only, no permissions)

```
user {}
organization { admin: user; member: user }
team   { organization: organization; creator: user; admin: user; member: user }
office { organization: organization; creator: user; admin: user; occupant: user }
```

## Populated today (write-side listeners)

- [x] `organization#admin@founder` + `organization#member@founder` — on org registration
- [x] `organization#member@user` — on user creation (`AddUser`)
- [x] `team#organization`, `team#creator` — on team create
- [x] `team#member` — on add member (deleted on remove member)
- [x] `team#admin` — on promote member to admin (deleted on revoke / remove member); many admins
      per team allowed
- [x] whole `team:{id}` wiped — on team delete
- [x] `office#organization`, `office#creator` — on office create
- [x] `office#occupant` — on assign (deleted on unassign / re-assign move)
- [x] `office#admin` — on promote occupant to admin (deleted on revoke / unassign); many admins
      per office allowed
- [x] whole `office:{id}` wiped — on office delete
- [x] every tuple with `user:{id}` as subject deleted — on user delete (`UserDeleted`,
      subject-filter delete per resource type). DB side: memberships/assignments removed and
      `team`/`office` `creator` orphaned (nulled, not cascade-deleted) by synchronous listeners.
- [x] whole `organization:{id}` wiped — on org delete (`OrganizationDeleted`), which also cascades:
      team/office modules delete their aggregates (each raising `TeamDeleted`/`OfficeDeleted` so the
      per-resource wipes fire) and the org's users are deleted via `UserApi`.

---

## Missing / open

### Deferred by choice (parked, not bugs)

- [ ] **Who may promote/manage admins is not enforced.** Any authenticated caller can promote or
      revoke a team/office admin (and delete an organization). Gate these once the check side lands.
- [ ] **Permissions not defined.** Schema is relations-only; `view`/`manage` permission
      definitions were removed on purpose. Re-add when ready to enforce.
- [ ] **Check side not wired.** No `checkPermission` / `AuthorizationManager`; endpoints are
      just `.authenticated()`. Reinstate a SpiceDB `AuthorizationManager` + `.access(...)` later.
