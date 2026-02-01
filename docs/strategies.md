# Flipping Strategies

Flipping strategies allow you to control when a feature is enabled based on dynamic conditions,
rather than just a simple on/off toggle.

## Overview

A `FlippingStrategy` is evaluated each time a feature is checked. This enables use cases like:
- Gradual rollouts (percentage-based)
- User-specific targeting
- A/B testing
- Time-based feature availability

## Built-in Strategies

### Constant Strategies

#### AlwaysTrueFlippingStrategy

Always returns `true`. Useful as a base case in composite strategies.

#### AlwaysFalseFlippingStrategy

Always returns `false`. Useful as a base case in composite strategies.

### Ponderation Strategies

#### PonderationStrategy

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

#### UserPonderationStrategy

Enables a feature for a consistent percentage of users. The same user always gets the same
result (sticky sessions), making it ideal for A/B testing and gradual rollouts.

Requires `userId` in the execution context.

```kotlin
feature("beta-feature") {
    userPonderationStrategy(0.10) // 10% of users
}

// Or using integer percentage
feature("beta-feature") {
    userPonderationStrategy(10) // 10% of users
}

// Checking with user context
val context = FlippingExecutionContext("userId" to currentUserId)
ff4k.check("beta-feature", context)
```

### Composite Strategies

Combine multiple strategies using logical operators.

#### AndStrategy

All strategies must evaluate to `true`.

```kotlin
val strategy = strategyA and strategyB // Both must be true
```

#### OrStrategy

At least one strategy must evaluate to `true`.

```kotlin
val strategy = strategyA or strategyB // Either can be true
```

#### NotStrategy

Inverts the result of a strategy.

```kotlin
val strategy = !strategyA // Inverts the result
```

### Combining Strategies

You can chain multiple operators to create complex conditions:

```kotlin
feature("complex-feature") {
    flippingStrategy = (strategyA and strategyB) or !strategyC
}
```

For creating custom strategies, see [Extending FF4K](extending.md#implementing-a-flipping-strategy).
