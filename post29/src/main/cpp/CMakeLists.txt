cmake_minimum_required(VERSION 3.4.1)

add_library(basic SHARED snippet.cpp)

target_link_libraries(basic android log)