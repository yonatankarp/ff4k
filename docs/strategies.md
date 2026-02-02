# Flipping Strategies

Flipping strategies allow you to control when a feature is enabled based on dynamic conditions,
rather than just a simple on/off toggle.

## Overview

A `FlippingStrategy` is evaluated each time a feature is checked. This enables use cases like:
- Gradual rollouts (percentage-based)
- User-specific targeting
- A/B testing
- Time-based feature availability

## Constant Strategies

### AlwaysTrueFlippingStrategy

Always returns `true`. Useful as a base case in composite strategies.

### AlwaysFalseFlippingStrategy

Always returns `false`. Useful as a base case in composite strategies.

## Percentage-Based Strategies

### PonderationStrategy

Enables a feature based on a random percentage. Each evaluation is independent - the same user
may get different results on subsequent calls.

```kotlin
feature("gradual-rollout") {
    ponderationStrategy(0.25) // 25% chance of being enabled
}

// Or using integer percentage
feature("gradual-rollout") {
    ponderationStrategy(25) // 25% chance
}
```

### UserPonderationStrategy

Enables a feature for a consistent percentage of users. The same user always gets the same
result (sticky sessions), making it ideal for A/B testing and gradual rollouts.

Requires `ContextKeys.USER_ID` in the execution context.

```kotlin
feature("beta-feature") {
    userPonderationStrategy(0.10) // 10% of users
}

// Or using integer percentage
feature("beta-feature") {
    userPonderationStrategy(10) // 10% of users
}

// Checking with user context
val context = FlippingExecutionContext(ContextKeys.USER_ID to currentUserId)
ff4k.check("beta-feature", context)
```

## User-List Strategies

### AllowListStrategy

Enables a feature only for specific users. All other users will have the feature disabled.

Requires `ContextKeys.USER_ID` in the execution context.

```kotlin
feature("vip-feature") {
    allowListStrategy {
        +"user-123"
        +"user-456"
        add("user-789")
    }
}
```

### DenyListStrategy

Disables a feature for specific users. All other users will have the feature enabled.

Requires `ContextKeys.USER_ID` in the execution context.

```kotlin
feature("new-ui") {
    denyListStrategy {
        +"problematic-user-1"
        +"problematic-user-2"
    }
}
```

## Time-Based Strategies

### ReleaseDateStrategy

Enables a feature after a specified release date. The feature is disabled before the release
date and enabled once the current time reaches or passes it.

Useful for scheduling feature launches without requiring a deployment.

```kotlin
// Using ISO-8601 string
feature("new-feature") {
    releaseDateStrategy("2025-06-01T00:00:00Z")
}

// Using Instant
feature("new-feature") {
    releaseDateStrategy(Instant.parse("2025-06-01T00:00:00Z"))
}

// Using LocalDateTime with timezone
feature("new-feature") {
    releaseDateStrategy(
        dateTime = LocalDateTime(2025, 6, 1, 9, 0),
        timezone = TimeZone.of("America/New_York")
    )
}
```

### DateRangeStrategy

Enables a feature only within a specified time range. The feature is enabled when the current
time is at or after the start date and before the end date (i.e., `[startDate, endDate)`).

Useful for limited-time promotions, scheduled maintenance windows, or time-boxed experiments.

```kotlin
// Using ISO-8601 strings
feature("holiday-sale") {
    dateRangeStrategy(
        startDate = "2025-12-20T00:00:00Z",
        endDate = "2025-12-26T00:00:00Z"
    )
}

// Using Instant values
feature("maintenance-mode") {
    dateRangeStrategy(
        startDate = maintenanceStart,
        endDate = maintenanceEnd
    )
}

// Using LocalDateTime with timezone
feature("summer-promo") {
    dateRangeStrategy(
        startDate = LocalDateTime(2025, 6, 1, 0, 0),
        endDate = LocalDateTime(2025, 8, 31, 23, 59),
        timezone = TimeZone.of("Europe/London")
    )
}
```

## Composite Strategies

Combine multiple strategies using logical operators.

### AndStrategy

All strategies must evaluate to `true`.

```kotlin
val strategy = strategyA and strategyB // Both must be true
```

### OrStrategy

At least one strategy must evaluate to `true`.

```kotlin
val strategy = strategyA or strategyB // Either can be true
```

### NotStrategy

Inverts the result of a strategy.

```kotlin
val strategy = !strategyA // Inverts the result
```

## Combining Strategies

You can chain multiple operators to create complex conditions:

```kotlin
feature("complex-feature") {
    flippingStrategy = (strategyA and strategyB) or !strategyC
}
```

For creating custom strategies, see [Extending FF4K](extending.md#implementing-a-flipping-strategy).
