cmake_minimum_required(VERSION 3.10)
project(CasinoEngine CXX)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_EXPORT_COMPILE_COMMANDS ON)

include_directories(include)

# Source files for the provably fair core engine.
# HMAC-SHA256 is implemented in Sha256.h (header-only, FIPS 180-4 + RFC 2104),
# so the engine no longer depends on OpenSSL or any external crypto library
# and compiles identically on desktop (this CMakeLists) and on Android (NDK).
set(SOURCES
    src/ProvablyFairCore.cpp
)

# Local desktop test executable.
add_executable(casino_test src/main.cpp ${SOURCES})
