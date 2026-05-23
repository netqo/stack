# stack

[![CI](https://github.com/nullnullnullnullnullnullnullnullnullnul/stack/actions/workflows/ci.yml/badge.svg?branch=main)](https://github.com/nullnullnullnullnullnullnullnullnullnul/stack/actions/workflows/ci.yml)
![Android](https://img.shields.io/badge/Platform-Android-3DDC84?logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Language-Kotlin-7F52FF?logo=kotlin&logoColor=white)
![C++17](https://img.shields.io/badge/Native-C%2B%2B17-00599C?logo=cplusplus&logoColor=white)
![Min SDK](https://img.shields.io/badge/Min%20SDK-24-blue)
![Target SDK](https://img.shields.io/badge/Target%20SDK-36-blue)
![Provably Fair](https://img.shields.io/badge/Provably%20Fair-HMAC--SHA256-7B61FF)
![License](https://img.shields.io/badge/License-MIT-lightgrey)
[![Conventional Commits](https://img.shields.io/badge/Conventional%20Commits-1.0.0-FE5196?logo=conventionalcommits&logoColor=white)](https://www.conventionalcommits.org/en/v1.0.0/)
[![Keep a Changelog](https://img.shields.io/badge/Keep%20a%20Changelog-1.1.0-E05735?logo=keepachangelog&logoColor=white)](https://keepachangelog.com/en/1.1.0/)
[![Contributor Covenant](https://img.shields.io/badge/Contributor%20Covenant-2.1-4baaaa)](CODE_OF_CONDUCT.md)

Provably-fair game-outcome engine in C++17 with Android JNI bindings. The engine and its bridge ship today; the consumer surface (game UIs, wallet, on-chain payments) is the planned product, tracked in the [Roadmap](#roadmap) below.

## Status

| Component                                         | State                                                                                                  |
| ------------------------------------------------- | ------------------------------------------------------------------------------------------------------ |
| `ProvablyFairCore` engine (coinflip, roulette, crash, mines, blackjack deck) | **implemented** (`cpp_engine/src/ProvablyFairCore.cpp`)                                                |
| Self-contained HMAC-SHA256 (no OpenSSL)           | **implemented** (`cpp_engine/include/Sha256.h`, FIPS 180-4 + RFC 2104)                                 |
| Desktop test harness                              | **implemented** (`cpp_engine/src/main.cpp`)                                                            |
| Android NDK build to `libcasino-engine.so` (arm64-v8a, armeabi-v7a, x86, x86_64) | **implemented**                                                                                        |
| JNI bridge + Kotlin `NativeGameEngine` facade     | **implemented** (`cpp_engine/src/native-lib.cpp`, `app/.../engine/NativeGameEngine.kt`)                |
| `MainActivity` JNI smoke test                     | **implemented**                                                                                        |
| Roulette / Blackjack / Crash / Mines game screens | **not implemented yet**                                                                                |
| Wallet, USDC deposits/withdrawals on Polygon      | **not implemented yet**                                                                                |
| Firebase Auth / Firestore / Cloud Functions       | **not implemented yet**                                                                                |
| Gemini-powered in-app assistant                   | **not implemented yet**                                                                                |
| NewsAPI feed, Room caching                        | **not implemented yet**                                                                                |
| Biometric-gated key storage                       | **not implemented yet**                                                                                |

What you can run today: the desktop test binary exercises every game algorithm and the input-validation paths; the Android app installs and the JNI smoke test confirms the native bridge round-trips strings through the engine.

## What the engine does

`ProvablyFairCore` is a pure C++ class that derives deterministic, verifiable game outcomes from a `(serverSeed, clientSeed, nonce)` triple using HMAC-SHA256. Each public method returns `"result|hash"` so the caller can both consume the result and persist the hash for later third-party verification.

- **Rejection sampling for unbiased outcomes.** Roulette (`[0, 37)`), Mines and Blackjack (Fisher-Yates with shrinking ranges) all reject hash chunks that would introduce modulo bias and rehash when the current hash is exhausted.
- **Exhaustion is treated as an error, not silently biased.** If rejection sampling fails to find a valid chunk within the safety bound (an astronomically unlikely event that indicates a broken entropy path), the engine throws instead of falling back to a fixed value.
- **No external crypto dependency.** `Sha256.h` is a header-only, FIPS 180-4 conformant SHA-256 plus an RFC 2104 HMAC wrapper, so the engine builds identically on desktop CMake and the Android NDK with zero third-party libraries.

## Architecture

```
UI (Jetpack Compose)                  -- planned
        downward via StateFlow
ViewModel + Repository layer          -- planned
        |
        +-- Room (single source of truth)             -- planned
        +-- Retrofit / Firebase / Polygon RPC         -- planned

Game outcomes
    Kotlin NativeGameEngine ──JNI──► ProvablyFairCore (C++)   -- IMPLEMENTED
                                       ├── HMAC-SHA256 (Sha256.h)
                                       ├── Coinflip
                                       ├── Roulette  (rejection sampling)
                                       ├── Crash     (1% house edge)
                                       ├── Mines     (Fisher-Yates partial shuffle)
                                       └── Blackjack (Fisher-Yates full shuffle)
```

The lower half of the diagram is what compiles and runs today. The upper half (UI, repository, persistence, network) is the planned product surface described in the [Roadmap](#roadmap).

## Build

### Desktop (engine + test harness)

```bash
cmake -S cpp_engine -B cpp_engine/build -DCMAKE_BUILD_TYPE=Release
cmake --build cpp_engine/build --parallel
./cpp_engine/build/casino_test
```

The test binary prints 10 rounds for each game plus input-validation pass/fail. It is the same binary CI runs on every push.

### Android (app + native library)

```bash
./gradlew assembleDebug
```

This compiles `libcasino-engine.so` via the Android NDK for the four target ABIs (`arm64-v8a`, `armeabi-v7a`, `x86`, `x86_64`) and bundles it into the debug APK. The app launches into a JNI smoke screen that calls `evaluateCoinflip` and `evaluateCrashPoint` once and renders the results.

Prerequisites: Android Studio (or the equivalent SDK + NDK + CMake bundle) and JDK 17.

## Provably fair verification

For each round the engine derives the outcome from `HMAC-SHA256(serverSeed, clientSeed:nonce)` and returns the full hash alongside the result. Once a round closes you can verify any past outcome independently:

1. Take `serverSeed`, `clientSeed`, `nonce` from the round.
2. Compute `HMAC-SHA256(serverSeed, clientSeed + ":" + nonce)` with any conformant implementation.
3. Apply the documented derivation for the game (e.g. for coinflip: parity of the first byte; for roulette: the first non-rejected 16-bit chunk modulo 37).
4. Compare against the recorded result.

If the engine ever returns a different value than the verifier computes, the round is invalid.

## Conventions

- **Commits**: [Conventional Commits](https://www.conventionalcommits.org/) (`feat`, `fix`, `refactor`, `docs`, `chore`, `ci`, `test`, ...).
- **Branching**: feature branches off `dev`, merged via PR into `dev`, batched into `main` per release.
- **PRs**: CI must be green. See [`.github/CONTRIBUTING.md`](.github/CONTRIBUTING.md) for the full process.
- **Changelog**: [Keep a Changelog](https://keepachangelog.com/en/1.1.0/) format in [`CHANGELOG.md`](CHANGELOG.md). Unreleased changes accumulate there until tagged.
- **Code of conduct**: [Contributor Covenant 2.1](CODE_OF_CONDUCT.md).
- **C++ style**: enforced via `.clang-format` in `cpp_engine/`.

## Roadmap

The eventual product target is a self-contained Android casino simulator backed by a real Polygon wallet, useful as a sandbox for testing game logic and crypto flows end-to-end without putting real money at risk (the operator controls both the house wallet and the player wallets they fund). The work below is ordered by intended implementation:

1. Game UIs: Roulette, Blackjack, Crash and Mines screens that consume `NativeGameEngine` and render results in Jetpack Compose.
2. App architecture: MVVM, Hilt for DI, Room as a single source of truth, Retrofit for any remote calls.
3. Wallet: house-wallet generation/import via Android Keystore + `EncryptedSharedPreferences`, gated by `BiometricPrompt`.
4. On-chain integration: USDC deposits/withdrawals on Polygon, Alchemy webhook for deposit detection, Firebase Cloud Function for withdrawal signing.
5. Firebase: Auth (Google Sign-In), Firestore sync for users/rounds/bets, Cloud Messaging for deposit/withdrawal/security notifications.
6. Auxiliary: Gemini-powered in-app assistant scoped to game rules, NewsAPI feed with offline-first caching.

Component-level progress is tracked in [`CHANGELOG.md`](CHANGELOG.md) (`[Unreleased]` block) and surfaced in the Status table above.

## License

MIT. See [LICENSE](LICENSE).
