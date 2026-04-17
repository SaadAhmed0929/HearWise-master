cmake_minimum_required(VERSION 3.22.1)
project(whisper_android)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_C_STANDARD 11)

# Optimisation flags for mobile
set(CMAKE_C_FLAGS   "${CMAKE_C_FLAGS}   -O3 -ffast-math")
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -O3 -ffast-math")

# Disable GGML features that are not available / needed on Android CPU
add_definitions(
    -DGGML_USE_CPU
    -DWHISPER_NO_DECODER_FALLBACK
    -D_GNU_SOURCE
)

add_library(
    whisper-hearwise
    SHARED

    jni-bridge.cpp
    whisper.cpp
    ggml.c
    ggml-alloc.c
    ggml-backend.c
    ggml-quants.c
)

find_library(log-lib log)
find_library(android-lib android)

target_include_directories(whisper-hearwise PRIVATE ${CMAKE_CURRENT_SOURCE_DIR})

target_link_libraries(
    whisper-hearwise
    ${log-lib}
    ${android-lib}
)
