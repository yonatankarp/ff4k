# Flipping Strategies

A `FlippingStrategy` decides whether an *enabled* feature is active for a given evaluation context:

```kotlin
fun interface FlippingStrategy {
    suspend fun evaluate(feature: Feature, context: Map<String, Any>): Boolean
}
```

The context is whatever the caller passes to `ff4k.check(id, context)`. Strategies read the keys they need from it.

## Built-in strategies

| Strategy | JSON `type` | Active when |
|----------|-------------|-------------|
| `ContextFilterStrategy(key, allowed)` | `contextFilter` | `context[key]` is one of `allowed` |
| `PercentageStrategy(percentage)` | `percentage` | a random `percentage` of evaluations |
| `UserPercentageStrategy(percentage, key = "userId")` | `userPercentage` | a stable `percentage` of users, bucketed by `context[key]` and the feature id, so each feature picks its users independently |
| `ReleaseDateStrategy(releaseDate)` | `releaseDate` | now ≥ `releaseDate` |
| `DateRangeStrategy(start, end)` | `dateRange` | `start` ≤ now < `end` |
| `DailyHoursStrategy(startHour, endHour, timezone)` | `dailyHours` | the local hour is in `startHour until endHour` |
| `WeekdayStrategy(days, timezone)` | `weekday` | the local day of week is in `days` |

A missing context key evaluates to false.

!!! warning "Upgrading `userPercentage` rollouts"
    Bucketing changed after 0.4.0: every user moves to a new bucket, so existing rollouts select different users after the upgrade. During a rolling deploy, old and new nodes can give the same user different answers. Renaming a feature also reshuffles its users, because its id is part of the bucket.

```kotlin
Feature("eu-only", enabled = true, strategy = ContextFilterStrategy("region", setOf("eu")))
Feature("rollout", enabled = true, strategy = UserPercentageStrategy(25))
Feature("office-hours", enabled = true, strategy = DailyHoursStrategy(9, 17, TimeZone.of("Europe/Berlin")))

ff4k.check("eu-only", mapOf("region" to "eu"))
ff4k.check("rollout", mapOf("userId" to user.id))
```

## Combining strategies

`and`, `or` and `!` build `AndStrategy`, `OrStrategy` and `NotStrategy`, all serializable:

```kotlin
val strategy = ContextFilterStrategy("region", setOf("eu")) and
    !ContextFilterStrategy("userId", blockedUsers) and
    (UserPercentageStrategy(10) or ContextFilterStrategy("role", setOf("beta")))
```

A deny list is the negation of a filter: `!ContextFilterStrategy("userId", setOf("alice"))`.

In JSON:

```json
{ "type": "and", "strategies": [
  { "type": "contextFilter", "key": "region", "allowed": ["eu"] },
  { "type": "not", "strategy": { "type": "contextFilter", "key": "userId", "allowed": ["alice"] } }
] }
```

## Custom strategies

Any lambda works at runtime:

```kotlin
Feature("f", enabled = true, strategy = FlippingStrategy { _, ctx -> ctx["tier"] == "gold" })
```

To persist a strategy, make it a `@Serializable` data class and register it; see [Custom Flipping Strategy](../customization/custom-strategy.md).
