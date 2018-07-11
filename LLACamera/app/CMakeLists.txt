

cmake_minimum_required(VERSION 3.4.1)

add_library( # Sets the name of the library.
             native-lib

             # Sets the library as a shared library.
             SHARED

             # Provides a relative path to your source file(s).
             src/main/cpp/native-lib.cpp )


find_library( # Sets the name of the path variable.
              log-lib

              # Specifies the name of the NDK library that
              # you want CMake to locate.
              log )


#导入头文件
include_directories(src/main/jniLibs/include)

#添加动态库
add_library(ffmpeg
             SHARED
             IMPORTED)
#设置动态库路径
set_target_properties(ffmpeg
                      PROPERTIES IMPORTED_LOCATION
                      ../../../../src/main/jniLibs/armeabi-v7a/libffmpegjni.so
                        )

target_link_libraries( # Specifies the target library.
                       native-lib
                        ffmpeg
                       # Links the target library to the log library
                       # included in the NDK.
                       ${log-lib} )