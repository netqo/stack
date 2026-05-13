# CMakeLists.txt - Android NDK build for the Stack Casino native engine.
#
# Builds libcasino-engine.so by combining:
#   - The provably fair core (cpp_engine/src/ProvablyFairCore.cpp) which uses
#     the self-contained HMAC-SHA256 implementation in Sha256.h.
#   - The JNI bridge (cpp_engine/src/native-lib.cpp) that exposes the core to
#     the Kotlin com.plainstudio.stackcasino.engine.NativeGameEngine class.
#
# No external dependencies are required (no OpenSSL, no prefab packages).

cmake_minimum_required(VERSION 3.22.1)
project(stackcasino_native CXX)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# Path to the engine sources at the repository root.
# CMakeLists.txt lives at app/src/main/cpp/, so we go up four levels.
set(ENGINE_DIR ${CMAKE_CURRENT_SOURCE_DIR}/../../../../cpp_engine)

# Make the engine headers visible (ProvablyFairCore.h, Sha256.h).
include_directories(${ENGINE_DIR}/include)

# Output: libcasino-engine.so loaded by System.loadLibrary("casino-engine").
add_library(casino-engine SHARED
    ${ENGINE_DIR}/src/ProvablyFairCore.cpp
    ${ENGINE_DIR}/src/native-lib.cpp
)

# JNI ships with the NDK; link the Android log library for completeness even if
# the engine does not currently use it (useful for debugging from C++).
find_library(log-lib log)
target_link_libraries(casino-engine ${log-lib})
