cmake_minimum_required(VERSION 3.20)
project(algerium_engine LANGUAGES CXX)
set(CMAKE_CXX_STANDARD 20)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
add_library(algerium_engine STATIC
    src/dom.cpp
    src/css.cpp
    src/layout.cpp
    src/html_parser.cpp
    src/engine.cpp
    src/security.cpp
    src/events.cpp
    src/navigation.cpp
    src/render.cpp
    src/resources.cpp
)
target_include_directories(algerium_engine PUBLIC include)
if(MSVC)
  target_compile_options(algerium_engine PRIVATE /W4)
else()
  target_compile_options(algerium_engine PRIVATE -Wall -Wextra -Wpedantic)
endif()
option(ALGERIUM_ENGINE_TESTS "Build engine tests" ON)
if(ALGERIUM_ENGINE_TESTS)
  enable_testing()
  add_executable(algerium_engine_tests tests/engine_tests.cpp)
  target_link_libraries(algerium_engine_tests PRIVATE algerium_engine)
  add_test(NAME algerium_engine_tests COMMAND algerium_engine_tests)
endif()
