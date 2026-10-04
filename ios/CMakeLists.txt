cmake_minimum_required(VERSION 3.25)
project(AlgeriumIOS LANGUAGES CXX)
set(CMAKE_CXX_STANDARD 20)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
add_library(algerium_ios_engine STATIC
  ../engine/src/dom.cpp
  ../engine/src/css.cpp
  ../engine/src/layout.cpp
  ../engine/src/html_parser.cpp
  ../engine/src/engine.cpp
  ../engine/src/security.cpp
  ../engine/src/events.cpp
  ../engine/src/navigation.cpp
  ../engine/src/render.cpp
  ../engine/src/resources.cpp
)
target_include_directories(algerium_ios_engine PUBLIC ../engine/include)
