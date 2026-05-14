# Contributing to Stack Casino

Thanks for taking the time to contribute. This guide documents the
project's branching model, commit conventions, and how to build and run
the application locally.

## Branching model

We follow a lightweight GitFlow:

- `main` is the production-ready branch. It only receives changes via
  pull requests from `dev` (release promotion PRs).
- `dev` is the integration branch. Feature work lands here.
- Feature branches branch off `dev` and merge back into `dev`.

### Branch naming

| Prefix       | Use case                                              |
| ------------ | ----------------------------------------------------- |
| `feat/`      | New user-visible feature.                             |
| `fix/`       | Bug fix.                                              |
| `refactor/`  | Code change that does not alter behavior.             |
| `perf/`      | Performance improvement.                              |
| `chore/`     | Maintenance: build, deps, config, repo housekeeping.  |
| `docs/`      | Documentation only.                                   |
| `test/`      | Adding or fixing tests.                               |
| `ci/`        | Continuous integration configuration.                 |
| `release/`   | Promoting `dev` to `main`.                            |
| `hotfix/`    | Urgent fix straight onto `main`.                      |

Examples: `feat/jni-bridge`, `fix/wallet-balance-sync`, `chore/update-deps`,
`docs/casos-de-uso-perfil`.

## Commit convention

We use [Conventional Commits](https://www.conventionalcommits.org/en/v1.0.0/).

### Format

```
<type>(<scope>): <subject>

<body>

<footer>
```

### Types

| Type       | Meaning                                                            |
| ---------- | ------------------------------------------------------------------ |
| `feat`     | New user-visible feature.                                          |
| `fix`      | Bug fix.                                                           |
| `refactor` | Code change that does not alter behavior.                          |
| `perf`     | Performance improvement.                                           |
| `style`    | Whitespace, formatting, lint fixes (no logic changes).             |
| `test`     | Adding or fixing tests.                                            |
| `docs`     | Documentation only.                                                |
| `chore`    | Maintenance: build, deps, config, repo housekeeping.               |
| `build`    | Changes to the build system or external dependencies.              |
| `ci`       | Continuous integration configuration.                              |
| `revert`   | Reverting a previous commit.                                       |

### Scopes (suggested)

- `cpp-engine`: Native C++ engine (provably fair core, JNI bridge).
- `android`: Android module (Kotlin, Compose, Gradle).
- `entrega`: TPO entrega documentation.
- `ci`: GitHub Actions workflows.
- Add new scopes as needed.

### Subject

- Imperative mood, lowercase, no trailing period.
- Under 70 characters.

### Body

- Explain the why and any non-obvious how.
- Wrap at ~72 characters per line.
- Separate from subject with a blank line.

### Footer

- Reference related issues: `Refs #123`, `Closes #456`.
- For breaking changes, start the footer with `BREAKING CHANGE: ...`.

### Examples

```
feat(android): add NativeGameEngine Kotlin wrapper and JNI smoke test

Introduces NativeGameEngine, the single Kotlin class that crosses the
JNI boundary. It exposes the five external fun entry points wired to
the corresponding C++ exports and loads libcasino-engine.so once via
a companion-object init block.
```

```
refactor(cpp-engine): replace OpenSSL with self-contained HMAC-SHA256

Adds Sha256.h, a header-only implementation of SHA-256 (FIPS 180-4)
and HMAC-SHA256 (RFC 2104) under the casino:: namespace.
```

## Pull request workflow

1. Branch off the latest `dev`:

   ```bash
   git checkout dev
   git pull origin dev
   git checkout -b feat/<name>
   ```

2. Make atomic commits following Conventional Commits.

3. Push the branch and open a PR targeting `dev`:

   ```bash
   git push -u origin feat/<name>
   gh pr create --base dev --fill
   ```

4. Use the PR template (it appears automatically) to describe the change.

5. Merge with the default merge method. Squashing and rebasing are
   disabled to preserve granular commit history. The branch is
   auto-deleted after merge.

To promote `dev` to `main`, open a `release/<version>` branch with no
new commits (only the integration merge) and PR it to `main`.

## Style conventions

### Writing

- ASCII-style prose. Single hyphens, not em-dashes. No emojis.
- Use Spanish for entrega documentation. Use English for code,
  commits, PR descriptions, and `.github/` assets.

### Kotlin

- Follow the project's `.editorconfig` and the default Android Studio
  Kotlin formatter. Run `Code -> Reformat Code` before committing.
- Compose composables use PascalCase. State is hoisted unless trivial.
- Public APIs include KDoc.

### C++

- Follow `cpp_engine/.clang-format` (Google base, two-space indent,
  100-column lines).
- Public methods include Doxygen comments.
- No dynamic allocation in the hot path of the engine.

## Local development

### Android module

```bash
# From the repository root.
./gradlew assembleDebug          # Build the debug APK (includes native).
./gradlew installDebug           # Install on the first connected device.
./gradlew testDebugUnitTest      # Run JVM unit tests (when present).
./gradlew connectedDebugAndroidTest  # Run instrumentation tests on device.
```

### C++ engine (desktop tests)

```bash
cd cpp_engine
cmake -B build
cmake --build build
./build/casino_test
```

The desktop build is OpenSSL-free; it uses the self-contained
HMAC-SHA256 implementation in `cpp_engine/include/Sha256.h`.

### Native build for Android

The Android module compiles the C++ engine via CMake. See
`app/src/main/cpp/CMakeLists.txt`. The resulting `libcasino-engine.so`
covers `arm64-v8a`, `armeabi-v7a`, `x86`, and `x86_64`. No external
libraries are pulled in.

## Reporting issues

Use the issue templates under `.github/ISSUE_TEMPLATE/`. Include logs,
device information, and a minimal reproduction whenever possible.
