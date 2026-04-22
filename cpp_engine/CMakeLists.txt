cmake_minimum_required(VERSION 3.10)
project(CasinoEngine CXX)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_EXPORT_COMPILE_COMMANDS ON)

# Buscar OpenSSL en el sistema para testing local
find_package(OpenSSL REQUIRED)

include_directories(${OPENSSL_INCLUDE_DIR})
include_directories(include)

# Archivos fuente del motor
set(SOURCES
    src/ProvablyFairCore.cpp
)

# Compilar un ejecutable para test local
add_executable(casino_test src/main.cpp ${SOURCES})

# Linkear OpenSSL
target_link_libraries(casino_test OpenSSL::SSL OpenSSL::Crypto)
