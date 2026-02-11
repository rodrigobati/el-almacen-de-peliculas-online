# Catalog ↔ Sales Synchronization (Event-Driven Projection)

## 1. Brief summary of the proposed approach

- Catalog is the source of truth for movies and emits domain events on create, update, and retire.
- Sales consumes those events and maintains a local read model with only the fields it needs.
- Each purchase stores immutable snapshots of movie data at the time of purchase.
- Deletion is modeled as logical retirement rather than a hard delete.

## 2. Strengths of the approach

- **Loose coupling at runtime**: Sales does not depend on Catalog availability for cart/checkout.
- **Resiliency**: Temporary outages in Catalog do not block Sales operations.
- **Performance and autonomy**: Sales reads its local projection without network hops.
- **Auditability**: Snapshots preserve historical accuracy even if Catalog data changes later.
- **Business alignment**: “Retire” is a clearer business concept than physical deletion.

## 3. Weaknesses or risks

- **Eventual consistency**: Sales may serve slightly stale movie data until events are processed.
- **Operational complexity**: Requires a message broker and reliable event handling.
- **Data drift risk**: Missed events or processing failures can desync projections.
- **Versioning challenges**: Event schema evolution needs coordination between services.

## 4. Possible alternatives

- **Synchronous REST on demand**: Sales queries Catalog in real time for each cart operation.
  - Simpler to implement but tightly couples availability and increases latency.
- **Hybrid read-through**: Sales uses its projection but falls back to Catalog on cache miss.
  - Reduces staleness risk but reintroduces some runtime coupling.
- **Shared event log replay**: Sales can rebuild its projection by replaying events if it drifts.
  - More robust but adds tooling and storage overhead.

## 5. Final recommendation and rationale

The proposed event-driven projection with immutable purchase snapshots is the best fit for this project.
It balances clear domain ownership (Catalog as source of truth) with the autonomy and resiliency
expected in microservices. The approach is realistic for a university project: it demonstrates
DDD-aligned eventing, supports historical accuracy, and avoids shared databases or tight coupling.
To mitigate risks, keep the event set small, version events carefully, and include a simple replay
or backfill procedure to fix projection drift.
