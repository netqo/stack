# stack

[![CI](https://github.com/netqo/stack/actions/workflows/ci.yml/badge.svg?branch=main)](https://github.com/netqo/stack/actions/workflows/ci.yml)
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
| ktlint + detekt lint gating in CI                 | **implemented** (`config/detekt/detekt.yml`, dedicated `lint` job in `.github/workflows/ci.yml`)       |
| Dependency catalog (Hilt, Navigation, Retrofit, Room, Firebase, Gemini, Glide, MockK, ...) | **implemented** (`gradle/libs.versions.toml`)                                                          |
| Hilt DI scaffold (`StackCasinoApp`, `AppModule`, `@AndroidEntryPoint`) | **implemented** (`app/.../StackCasinoApp.kt`, `app/.../di/AppModule.kt`)                               |
| Stack Casino dark design system (palette + MD3 type scale) | **implemented** (`app/.../ui/theme/`)                                                                  |
| Compose Navigation graph (16 routes + bottom bar) | **implemented** (`app/.../navigation/`, `app/.../ui/components/StackBottomBar.kt`)                     |
| Splash Screen API + auth-state gating             | **implemented** (`Theme.Stackcasino.Splash` + `SplashViewModel` resolves Firebase session into Login/Lobby start destination) |
| Firebase wiring (BoM, Auth, Firestore, Analytics) | **implemented** (Hilt providers in `app/.../di/AppModule.kt`)                                          |
| Google Sign-In via Credential Manager             | **implemented** (`AuthRepository` + `LoginScreen` exchange a Google ID token with Firebase Auth)       |
| Per-screen UI (Lobby, Wallet, History, Profile, games, KYC, News, Assistant) | **not implemented yet**                                                                                |
| NewsAPI consumption + Room caching                | **not implemented yet**                                                                                |
| Glide image loading                               | **not implemented yet**                                                                                |
| Gemini-powered in-app assistant                   | **not implemented yet**                                                                                |
| Biometric-gated key storage                       | **not implemented yet**                                                                                |
| On-chain integration (USDC, Polygon, Alchemy)     | **not implemented yet**                                                                                |

What you can run today: the desktop test binary exercises every game algorithm and the input-validation paths; the Android app installs, shows the AndroidX SplashScreen with the brand icon on the SurfaceBase background, then `SplashViewModel` reads the cached Firebase Auth state and routes to either the Login screen (no session) or the Lobby placeholder (session persisted). The Login screen runs the real Google Sign-In flow via Credential Manager + Firebase Auth; once authenticated the user lands in the Lobby placeholder, and the Compose `NavHost` exposes the remaining 15 destinations as labeled placeholders driven by the five-tab bottom bar.

## What the engine does

`ProvablyFairCore` is a pure C++ class that derives deterministic, verifiable game outcomes from a `(serverSeed, clientSeed, nonce)` triple using HMAC-SHA256. Each public method returns `"result|hash"` so the caller can both consume the result and persist the hash for later third-party verification.

- **Rejection sampling for unbiased outcomes.** Roulette (`[0, 37)`), Mines and Blackjack (Fisher-Yates with shrinking ranges) all reject hash chunks that would introduce modulo bias and rehash when the current hash is exhausted.
- **Exhaustion is treated as an error, not silently biased.** If rejection sampling fails to find a valid chunk within the safety bound (an astronomically unlikely event that indicates a broken entropy path), the engine throws instead of falling back to a fixed value.
- **No external crypto dependency.** `Sha256.h` is a header-only, FIPS 180-4 conformant SHA-256 plus an RFC 2104 HMAC wrapper, so the engine builds identically on desktop CMake and the Android NDK with zero third-party libraries.

## Architecture

```
UI surface
    Jetpack Compose + Material 3 (dark theme)                 -- IMPLEMENTED
        StackApp -> Scaffold + StackBottomBar + StackNavHost  -- IMPLEMENTED (placeholders)
        per-screen composables (Lobby, Wallet, ...)           -- planned

App architecture
    MVVM via Hilt (@HiltAndroidApp + AppModule)               -- IMPLEMENTED (scaffold only)
    ViewModels + Repositories                                 -- planned
        |
        +-- Room (single source of truth)                     -- planned
        +-- Retrofit / Firebase / Polygon RPC                 -- planned

Game outcomes
    Kotlin NativeGameEngine ──JNI──► ProvablyFairCore (C++)   -- IMPLEMENTED
                                       ├── HMAC-SHA256 (Sha256.h)
                                       ├── Coinflip
                                       ├── Roulette  (rejection sampling)
                                       ├── Crash     (1% house edge)
                                       ├── Mines     (Fisher-Yates partial shuffle)
                                       └── Blackjack (Fisher-Yates full shuffle)
```

What runs today is the UI scaffolding plus the engine; the per-screen composables, ViewModels, repositories and remote data sources are the planned product surface described in the [Roadmap](#roadmap).

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
./gradlew ktlintCheck detekt
./gradlew :app:testDebugUnitTest
```

`assembleDebug` compiles `libcasino-engine.so` via the Android NDK for the four target ABIs (`arm64-v8a`, `armeabi-v7a`, `x86`, `x86_64`) and bundles it into the debug APK. The app launches into the AndroidX SplashScreen, `SplashViewModel` resolves the cached Firebase session, then routes to Login or Lobby; the five primary tabs (Lobby, Wallet, History, News, Profile) drive the bottom bar.

`ktlintCheck detekt` enforces style and static-analysis gating; the same tasks run as the `lint` job in CI and gate the `android` job. `:app:testDebugUnitTest` runs the JVM unit tests (`StackCasinoAppTest`, `StackcasinoThemeTest`, `RouteTest`, `StartDestinationTest`, `SplashViewModelTest`, `LoginViewModelTest`, `FirebaseUserMapperTest`, `AuthRepositoryImplTest`).

Prerequisites: Android Studio (or the equivalent SDK + NDK + CMake bundle) and JDK 17.

### Firebase setup

`app/google-services.json` is committed at the repo root so CI can build the debug APK end-to-end. The keys it contains are not secret in the traditional sense: Firebase relies on Security Rules + App Check + the SHA-1 signing fingerprint to gate access, not on client-side secrecy.

If you fork the repo, replace it with one from your own Firebase project:

1. Open https://console.firebase.google.com and create a project (Analytics optional).
2. Add an Android app with package name `com.plainstudio.stackcasino`.
3. Pin the debug build's SHA-1 so Google Sign-In can authenticate it: `keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android`. Copy the `SHA1:` line into the Firebase app settings under "Add fingerprint".
4. Download the generated `google-services.json` and drop it in `app/`.
5. `./gradlew :app:assembleDebug` should then succeed; the Firebase SDK self-initializes via `FirebaseInitProvider` (no manual `FirebaseApp.initializeApp` call required).

### Google Sign-In setup

To exercise the Login screen end-to-end (real account chooser, real Firebase session) the consumer also needs to enable the Google provider in Firebase Auth and expose its Web client ID to the app:

1. Firebase Console -> `Build` -> `Authentication` -> `Get started`.
2. `Sign-in method` tab -> `Google` -> toggle **Enable** -> pick a `Project support email` -> `Save`.
3. Click the now-enabled `Google` provider and expand `Web SDK configuration` -> copy the **Web client ID** (not the Android one).
4. Add it to `local.properties` (gitignored):
   ```
   GOOGLE_WEB_CLIENT_ID=123456789012-xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx.apps.googleusercontent.com
   ```

The build reads the property and exposes it via `BuildConfig.GOOGLE_WEB_CLIENT_ID`; if the property is missing the field is empty and the runtime sign-in fails fast with a clear error so the UI surface still builds.

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
