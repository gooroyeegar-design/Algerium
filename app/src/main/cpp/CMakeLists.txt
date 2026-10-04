cmake_minimum_required(VERSION 3.22.1)
project(algerium_native)
set(CMAKE_CXX_STANDARD 20)
add_library(algerium_engine STATIC
    ../../../../engine/src/dom.cpp
    ../../../../engine/src/css.cpp
    ../../../../engine/src/layout.cpp
    ../../../../engine/src/html_parser.cpp
    ../../../../engine/src/engine.cpp
)
target_include_directories(algerium_engine PUBLIC ../../../../engine/include)
add_library(algerium_native SHARED algerium_jni.cpp)
target_include_directories(algerium_native PRIVATE ../../../../engine/include)
find_library(log-lib log)
target_link_libraries(algerium_native PRIVATE algerium_engine ${log-lib})
