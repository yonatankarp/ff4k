# Contributing to FF4K

Thank you for your interest in contributing to FF4K! This document provides guidelines and information to help you contribute effectively.

## Getting Started

### Building Locally

The build targets the JVM only for now; every module is Kotlin Multiplatform so more targets can be added later. You need a JDK 17 or higher.

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

| Type       | Description                                             | In release notes      |
|------------|---------------------------------------------------------|-----------------------|
| `feat`     | A new feature                                           | ✨ New features       |
| `change`   | An improvement to existing behaviour                    | 🔧 Improvements       |
| `fix`      | A bug fix                                               | 🐞 Bug fixes          |
| `docs`     | Documentation-only changes                              | No                    |
| `refactor` | Code change that neither fixes a bug nor adds a feature | No                    |
| `test`     | Adding or updating tests                                | No                    |
| `chore`    | Maintenance tasks; `chore(deps)` for dependency upgrades | Only `chore(deps)`, under 📦 Dependencies |
| `ci`       | CI/CD configuration changes                             | No                    |

Add `!` after the type or scope for a breaking change, e.g. `feat!: remove the DSL`.

Dependency upgrades use `chore(deps)`, which Dependabot does on its own, and are listed under **📦 Dependencies** in the release notes. GitHub Actions upgrades use `ci(deps)` and are left out.

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

A CI check rejects any other title, because the release notes are built from it: `feat`, `change` and `fix` pull requests are listed under their heading, the rest are left out. The entry is the title, or the first paragraph under a `## Release note` heading in the description when there is one; write `none` there to leave a pull request out.

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
- Unit tests on the JVM
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
