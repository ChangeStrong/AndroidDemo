

cmake_minimum_required(VERSION 3.4.1)


# 将生成的.so库 和此库绑定那些cpp文件
add_library( # Sets the name of the library.
             native-lib

             # Sets the library as a shared library.
             SHARED

             # Provides a relative path to your source file(s).
             src/main/cpp/native-lib.cpp
              src/main/cpp/audio_encoder.c)



find_library( # Sets the name of the path variable.
              log-lib

              # Specifies the name of the NDK library that
              # you want CMake to locate.
              log )



#声明导入文件更目录变量ARM_DIR ,这使用了相对与系统的更目录，因为使用相对路径似乎不起作用
#  /Users/wangyijin/Desktop/demo/FFmpegForAndroidDemo/app/src/main/jniLibs
set(ARM_DIR /Users/luoluo/AndroidStudioProjects/Github/AudioPlayer/app/src/main/jniLibs)


#导入头文件
include_directories(src/main/jniLibs/include)

#添加动态库
add_library(avcodec-57
             SHARED
             IMPORTED)
#设置动态库路径
set_target_properties(avcodec-57
                      PROPERTIES IMPORTED_LOCATION
                      ../../../../src/main/jniLibs/armeabi-v7a/libavcodec-57.so
                        )

add_library(avdevice-57
            SHARED
            IMPORTED)
set_target_properties(avdevice-57
                      PROPERTIES IMPORTED_LOCATION
                      ../../../../src/main/jniLibs/armeabi-v7a/libavdevice-57.so)
add_library(avformat-57
            SHARED
            IMPORTED)
set_target_properties(avformat-57
                      PROPERTIES IMPORTED_LOCATION
                      ../../../../src/main/jniLibs/armeabi-v7a/libavformat-57.so)
add_library(avutil-55
            SHARED
            IMPORTED)
set_target_properties(avutil-55
                      PROPERTIES IMPORTED_LOCATION
                      ../../../../src/main/jniLibs/armeabi-v7a/libavutil-55.so)

add_library(swresample-2
             SHARED
             IMPORTED)
set_target_properties(swresample-2
                       PROPERTIES IMPORTED_LOCATION
                       ../../../../src/main/jniLibs/armeabi-v7a/libswresample-2.so)
add_library(swscale-4
              SHARED
              IMPORTED)
set_target_properties(swscale-4
                        PROPERTIES IMPORTED_LOCATION
                        ../../../../src/main/jniLibs/armeabi-v7a/libswscale-4.so)
add_library(avfilter-6
              SHARED
              IMPORTED)
set_target_properties(avfilter-6
                        PROPERTIES IMPORTED_LOCATION
                        ../../../../src/main/jniLibs/armeabi-v7a/libavfilter-6.so)


# 提前编译需要使用的.so库和系统库
target_link_libraries( # Specifies the target library.
                       native-lib
                       avcodec-57
                       avdevice-57
                       avformat-57
                       avfilter-6
                       avutil-55
                       swresample-2
                       swscale-4
                        android
                        atomic
                        log
                        OpenSLES
                       # Links the target library to the log library
                       # included in the NDK.
                       ${log-lib} )