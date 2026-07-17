# SpiceDB / Authorization — entity relations TODO

Status of the ReBAC model as mirrored into SpiceDB. The **write side** (populating
relationships from domain events) is mostly done; permissions and the check side are
deliberately deferred.

## Current schema (relations only, no permissions)

```
user {}
organization { admin: user; member: user }
team   { organization: organization; creator: user; admin: user; member: user }
office { organization: organization; creator: user; occupant: user }
```

## Populated today (write-side listeners)

- [x] `organization#admin@founder` + `organization#member@founder` — on org registration
- [x] `organization#member@user` — on user creation (`AddUser`)
- [x] `team#organization`, `team#creator` — on team create
- [x] `team#member` — on add member (deleted on remove member)
- [x] whole `team:{id}` wiped — on team delete
- [x] `office#organization`, `office#creator` — on office create
- [x] `office#occupant` — on assign (deleted on unassign / re-assign move)
- [x] whole `office:{id}` wiped — on office delete
- [x] every tuple with `user:{id}` as subject deleted — on user delete (`UserDeleted`,
      subject-filter delete per resource type). DB side: memberships/assignments removed and
      `team`/`office` `creator` orphaned (nulled, not cascade-deleted) by synchronous listeners.

---

## Missing / open

### Model completeness

- [ ] **`team#admin` is defined but never written.** Either implement "assign/promote team
      admin" (command + event + listener) or drop the relation from the schema.
- [ ] **`office` has no `admin` relation** while `team` does. Decide whether offices need admins
      (asymmetry is currently intentional-by-default).
- [ ] **No "leave organization" / remove-org-member path.** `organization#member` is now removed
      on user deletion (subject-filter delete), but there is still no way to *leave*/move orgs while
      keeping the account. Add if that becomes a requirement.
- [ ] **Organization deletion cleanup.** No delete-org flow exists today; if added, it must wipe
      the org's tuples *and* cascade to all teams/offices/members under it.

### Deferred by choice (parked, not bugs)

- [ ] **Permissions not defined.** Schema is relations-only; `view`/`manage` permission
      definitions were removed on purpose. Re-add when ready to enforce.
- [ ] **Check side not wired.** No `checkPermission` / `AuthorizationManager`; endpoints are
      just `.authenticated()`. Reinstate a SpiceDB `AuthorizationManager` + `.access(...)` later.

### Nice-to-have / consistency

- [ ] **`OfficeId` not in a named interface.** Office events carry the office id as a raw `UUID`
      (because `OfficeId` lives in `office.domain`, not an `api` package). Optionally move it to
      `office/api` to make the events typed, mirroring `team.api.TeamId`.
