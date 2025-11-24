cmake_minimum_required(VERSION 3.22.1)

project("cvgl")

# OpenCV Configuration
# The OpenCV_DIR is passed from build.gradle via -DOpenCV_DIR
find_package(OpenCV REQUIRED)

add_library(
        cvgl
        SHARED
        native-lib.cpp)

target_link_libraries(
        cvgl
        ${OpenCV_LIBS}
        android
        log
        GLESv2
        EGL)
