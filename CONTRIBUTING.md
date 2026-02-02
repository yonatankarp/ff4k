# Contributing to FF4K

Thank you for your interest in contributing to FF4K! This document provides guidelines and information to help you contribute effectively.

## Getting Started

### Building Locally

This project uses an **adaptive build system** that automatically enables or disables platform targets based on the tools available in your environment.

#### 1. Default Behavior
*   **JVM:** Always built.
*   **Android:** Built only if the Android SDK is detected.
*   **Apple (iOS):** Built only if running on macOS with Xcode Command Line Tools installed.

#### 2. Prerequisites

To build all targets, including Android and iOS, ensure you have the following:

*   **Java Development Kit (JDK):**
    *   **Version 17 or higher** is required for all builds.
*   **Android SDK:**
    *   Set the `ANDROID_HOME` or `ANDROID_SDK_ROOT` environment variable.
    *   **OR** create a `local.properties` file in the project root with `sdk.dir=/path/to/android/sdk`.
*   **Apple (macOS only):**
    *   Xcode or Command Line Tools (`xcode-select --install`).

#### 3. Customizing the Build

You can manually control build options by creating a `local.properties` file in the project root:

```properties
# Path to Android SDK (automatically added by Android Studio)
sdk.dir=/Users/username/Library/Android/sdk

# Explicitly disable Apple targets (even on macOS) to speed up the build
ff4k.include.apple=false
```

### Building the Project

```bash
./gradlew build
```

### Running Tests

```bash
# Run all tests
./gradlew check

# Run JVM tests only
./gradlew jvmTest

# Run Android tests only
./gradlew testDebugUnitTest

# Run iOS tests (requires macOS)
./gradlew iosSimulatorArm64Test
```

### Checking Code Formatting

```bash
# Check formatting
./gradlew spotlessCheck

# Auto-fix formatting
./gradlew spotlessApply
```

## Making Changes

### Branch Naming

Use descriptive branch names with the following prefixes:

| Prefix      | Purpose               | Example                      |
|-------------|-----------------------|------------------------------|
| `feature/`  | New features          | `feature/add-redis-store`    |
| `fix/`      | Bug fixes             | `fix/property-serialization` |
| `docs/`     | Documentation changes | `docs/update-readme`         |
| `chore/`    | Maintenance tasks     | `chore/update-dependencies`  |
| `refactor/` | Code refactoring      | `refactor/simplify-dsl`      |

### Commit Messages

We follow [Conventional Commits](https://www.conventionalcommits.org/) for clear and consistent commit history.

**Format:**
```
<type>(<scope>): <description>

[optional body]

[optional footer(s)]
```

**Types:**

| Type       | Description                                             |
|------------|---------------------------------------------------------|
| `feat`     | A new feature                                           |
| `fix`      | A bug fix                                               |
| `docs`     | Documentation-only changes                              |
| `style`    | Code style changes (formatting, etc.)                   |
| `refactor` | Code change that neither fixes a bug nor adds a feature |
| `perf`     | Performance improvement                                 |
| `test`     | Adding or updating tests                                |
| `chore`    | Maintenance tasks, dependency updates                   |
| `ci`       | CI/CD configuration changes                             |

**Examples:**
```
feat(dsl): add support for feature groups

fix(store): resolve race condition in InMemoryFeatureStore

docs: update installation instructions in README

chore(deps): bump kotlin to 2.1.0
```

### Pull Requests

#### PR Title

PR titles should follow the same format as commit messages:
```
<type>(<scope>): <description>
```

This ensures clear release notes when your PR is merged.

#### PR Labels

Add appropriate labels to your PR. These labels are used to categorize changes in release notes:

| Label                           | Use When                                       |
|---------------------------------|------------------------------------------------|
| `enhancement` or `feature`      | Adding new functionality                       |
| `bug` or `fix`                  | Fixing a bug                                   |
| `documentation` or `docs`       | Documentation changes                          |
| `dependencies`                  | Dependency updates                             |
| `breaking` or `breaking-change` | Introducing breaking changes                   |
| `skip-changelog`                | Changes that shouldn't appear in release notes |

#### PR Description

Please include:
- **What**: A brief description of the changes
- **Why**: The motivation or issue being addressed
- **How**: Any notable implementation details
- **Testing**: How you tested the changes

### Code Quality

Before submitting a PR, ensure:

1. **Tests pass**: `./gradlew check`
2. **Code compiles**: `./gradlew build`
3. **Formatting is correct**: `./gradlew spotlessCheck`

The CI pipeline will automatically run:
- Code formatting check via Spotless (ktlint)
- Unit tests on JVM and Android (Linux)
- Unit tests on iOS Simulator (macOS)
- Code coverage merged and reported to SonarCloud

### Testing

We use [Kotest](https://kotest.io/) for testing. Tests should be written using Kotest's DSL:

```kotlin
class MyFeatureTest : FunSpec({
    test("should do something") {
        // Given
        val input = "test"

        // When
        val result = myFunction(input)

        // Then
        result shouldBe "expected"
    }
})
```

### Breaking Changes

If your change introduces breaking changes:

1. Add the `breaking` label to your PR
2. Document the breaking change in the PR description
3. Explain migration steps for users

## Questions?

If you have questions or need help, feel free to open a [GitHub Issue](https://github.com/yonatankarp/ff4k/issues).

## License

By contributing to FF4K, you agree that your contributions will be licensed under the [Apache License 2.0](LICENSE).
