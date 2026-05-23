package com.plainstudio.stackcasino.engine

/**
 * Kotlin facade over the native (C++) provably fair game engine.
 *
 * The engine lives in `cpp_engine/` at the repository root. It is compiled
 * into `libcasino-engine.so` by the Android NDK using the CMake script at
 * `app/src/main/cpp/CMakeLists.txt`. All five game algorithms run on the
 * native side; this class only marshals strings and longs across JNI.
 *
 * Each native method returns a string of the form `"result|hash"` where:
 *   - `result` is the game-specific outcome (a number, CSV, etc.).
 *   - `hash`   is the 64-char lowercase hex of HMAC-SHA256(serverSeed,
 *     "clientSeed:nonce") used to derive the result.
 *
 * Callers are expected to split on '|' once and forward the parts to the
 * domain layer. Throws [IllegalArgumentException] for invalid seeds or nonce
 * (mirrors `std::invalid_argument` from the core engine) and
 * [RuntimeException] for any other native failure.
 */
class NativeGameEngine {
    external fun evaluateCoinflip(
        serverSeed: String,
        clientSeed: String,
        nonce: Long,
    ): String

    external fun evaluateRoulette(
        serverSeed: String,
        clientSeed: String,
        nonce: Long,
    ): String

    external fun evaluateCrashPoint(
        serverSeed: String,
        clientSeed: String,
        nonce: Long,
    ): String

    external fun evaluateMines(
        serverSeed: String,
        clientSeed: String,
        nonce: Long,
        numMines: Int,
    ): String

    external fun evaluateBlackjackDeck(
        serverSeed: String,
        clientSeed: String,
        nonce: Long,
    ): String

    companion object {
        init {
            System.loadLibrary("casino-engine")
        }
    }
}
