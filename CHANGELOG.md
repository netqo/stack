# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added

- Self-contained HMAC-SHA256 implementation under `cpp_engine/include/Sha256.h`,
  following FIPS 180-4 (SHA-256) and RFC 2104 (HMAC). Header-only, no external
  dependencies.
- CMake + NDK integration in the Android module that compiles `cpp_engine` into
  `libcasino-engine.so` for the four target ABIs (`arm64-v8a`, `armeabi-v7a`,
  `x86`, `x86_64`).
- `NativeGameEngine` Kotlin facade in
  `com.plainstudio.stackcasino.engine` exposing the five provably fair game
  algorithms (Coinflip, Roulette, Crash, Mines, Blackjack deck) via JNI.
- JNI smoke test in `MainActivity` that exercises the bridge end to end.
- Android module scaffold: Empty Activity with Jetpack Compose, `minSdk` 24,
  `targetSdk` 36, Kotlin DSL build configuration.
- Provably fair C++ core engine (`ProvablyFairCore`) with the five game
  algorithms, JNI bridge, and CMake build for desktop testing.
- Repository governance under `.github/`: CODEOWNERS, pull request template,
  bug and feature issue templates, contributing guide, security policy.
- `CHANGELOG.md` following Keep a Changelog.

### Changed

- `ProvablyFairCore.cpp` no longer depends on OpenSSL. The HMAC-SHA256 helper
  delegates to the self-contained implementation in `Sha256.h`.
- JNI export symbols renamed from `com.casino.tpo.engine` to
  `com.plainstudio.stackcasino.engine` so they match the Android application
  package.

### Removed

- OpenSSL dependency from the C++ engine and its CMake configuration. The
  desktop test executable (`casino_test`) builds without `libssl` or
  `libcrypto`.

[Unreleased]: https://github.com/nullnullnullnullnullnullnullnullnullnul/stack/compare/main...dev
