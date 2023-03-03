# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.22.1)

project("giflib")

add_library(
        giflib
        SHARED

        dgif_lib.c
        gifalloc.c
        openbsd-reallocarray.c

        giflibs.cpp)

find_library( # Sets the name of the path variable.
        jnigraphics
        log-lib

        log)

target_link_libraries( # Specifies the target library.
        giflib
        jnigraphics
        log
        )