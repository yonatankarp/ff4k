# Monitoring and Reporting

FF4K provides built-in tools to inspect the state of your feature flags at runtime. This is useful for debugging, logging, and building administrative dashboards.

## Feature Statistics

The `stats()` method returns a snapshot of your feature store, providing aggregated counts of features by state.

```kotlin
val stats = ff4k.stats()

println("Total features: ${stats.total}")
println("Enabled: ${stats.enabled}")
println("Disabled: ${stats.disabled}")
println("With Permissions: ${stats.withPermissions}")
println("With Strategy: ${stats.withStrategy}")
println("Total Groups: ${stats.groups}")
```

## Text Report

For a quick human-readable overview, use the `report()` method. It generates a formatted string listing all features with their status, group, permissions, and strategies.

```kotlin
println(ff4k.report())
```

**Output Example:**
```text
FF4K Feature Report
===================
Total: 5 | Enabled: 3 | Disabled: 2
With Permissions: 2 | With Strategy: 1 | Groups: 2

Features:
  [ON]  dark-mode (group: ui)
  [OFF] beta-feature (group: ui) [STRATEGY: PercentageStrategy]
  [ON]  premium (group: billing) [PERMISSIONS: ADMIN, PREMIUM]
  [OFF] legacy
  [ON]  new-checkout [STRATEGY: RegionStrategy] [PERMISSIONS: BETA]
```

## Filtering Features

You can also query specific subsets of features:

- `ff4k.enabledFeatures()`: List of all enabled features.
- `ff4k.disabledFeatures()`: List of all disabled features.
- `ff4k.featuresWithPermission("ADMIN")`: Features requiring a specific permission.
- `ff4k.featuresWithStrategy()`: Features that have a flipping strategy attached.
