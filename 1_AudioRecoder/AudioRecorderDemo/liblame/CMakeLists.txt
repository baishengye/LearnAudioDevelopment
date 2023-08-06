cmake_minimum_required(VERSION 3.10)

#add_library(LameLoader SHARED
#        src/main/jni/lame/cpp/LameLoader.cpp
#        src/main/jni/lame/cpp/com_baishengye_liblame_LameLoader.h
#        src/main/jni/lame/include/lame.h
#        src/main/jni/lame/include/lame.def
#        src/main/jni/lame/libmp3lame/i386/nasm.h
#        src/main/jni/lame/libmp3lame/vector/lame_intrin.h
#        src/main/jni/lame/libmp3lame/vector/xmm_quantize_sub.c
#        src/main/jni/lame/libmp3lame/bitstream.c
#        src/main/jni/lame/libmp3lame/bitstream.h
#        src/main/jni/lame/libmp3lame/encoder.c
#        src/main/jni/lame/libmp3lame/encoder.h
#        src/main/jni/lame/libmp3lame/fft.c
#        src/main/jni/lame/libmp3lame/fft.h
#        src/main/jni/lame/libmp3lame/gain_analysis.c
#        src/main/jni/lame/libmp3lame/gain_analysis.h
#        src/main/jni/lame/libmp3lame/id3tag.c
#        src/main/jni/lame/libmp3lame/id3tag.h
#        src/main/jni/lame/libmp3lame/id3tag.c
#        src/main/jni/lame/libmp3lame/id3tag.h
#        src/main/jni/lame/libmp3lame/lame.c
#        src/main/jni/lame/libmp3lame/lame-analysis.h
#        src/main/jni/lame/libmp3lame/lame_global_flags.h
#        src/main/jni/lame/libmp3lame/lameerror.h
#        src/main/jni/lame/libmp3lame/machine.h
#        src/main/jni/lame/libmp3lame/mpglib_interface.c
#        src/main/jni/lame/libmp3lame/newmdct.c
#        src/main/jni/lame/libmp3lame/newmdct.h
#        src/main/jni/lame/libmp3lame/presets.c
#        src/main/jni/lame/libmp3lame/psymodel.c
#        src/main/jni/lame/libmp3lame/psymodel.h
#        src/main/jni/lame/libmp3lame/quantize.c
#        src/main/jni/lame/libmp3lame/quantize.h
#        src/main/jni/lame/libmp3lame/quantize_pvt.c
#        src/main/jni/lame/libmp3lame/quantize_pvt.h
#        src/main/jni/lame/libmp3lame/reservoir.c
#        src/main/jni/lame/libmp3lame/reservoir.h
#        src/main/jni/lame/libmp3lame/set_get.c
#        src/main/jni/lame/libmp3lame/set_get.h
#        src/main/jni/lame/libmp3lame/tables.c
#        src/main/jni/lame/libmp3lame/tables.h
#        src/main/jni/lame/libmp3lame/takehiro.c
#        src/main/jni/lame/libmp3lame/util.c
#        src/main/jni/lame/libmp3lame/util.h
#        src/main/jni/lame/libmp3lame/vbrquantize.c
#        src/main/jni/lame/libmp3lame/vbrquantize.h
#        src/main/jni/lame/libmp3lame/VbrTag.c
#        src/main/jni/lame/libmp3lame/VbrTag.h
#        src/main/jni/lame/libmp3lame/version.c
#        src/main/jni/lame/libmp3lame/version.h
#        src/main/jni/lame/mpglib/common.c
#        src/main/jni/lame/mpglib/common.h
#        src/main/jni/lame/mpglib/dct64_i386.c
#        src/main/jni/lame/mpglib/dct64_i386.h
#        src/main/jni/lame/mpglib/decode_i386.c
#        src/main/jni/lame/mpglib/decode_i386.h
#        src/main/jni/lame/mpglib/huffman.h
#        src/main/jni/lame/mpglib/interface.c
#        src/main/jni/lame/mpglib/interface.h
#        src/main/jni/lame/mpglib/l2tables.h
#        src/main/jni/lame/mpglib/layer1.c
#        src/main/jni/lame/mpglib/layer1.h
#        src/main/jni/lame/mpglib/layer2.c
#        src/main/jni/lame/mpglib/layer2.h
#        src/main/jni/lame/mpglib/layer3.c
#        src/main/jni/lame/mpglib/layer3.h
#        src/main/jni/lame/mpglib/mpg123.h
#        src/main/jni/lame/mpglib/mpglib.h
#        src/main/jni/lame/mpglib/tabinit.c
#        src/main/jni/lame/mpglib/tabinit.h)

aux_source_directory(src/main/jni/lame/libmp3lame LIB_MP3_LAME)
aux_source_directory(src/main/jni/lame/mpglib MPG_LIB)
aux_source_directory(src/main/jni/lame/libmp3lame/i386 I386)
aux_source_directory(src/main/jni/lame/libmp3lame/vector VECTOR)
add_library(LameLoader SHARED
        src/main/jni/lame/cpp/LameLoader.cpp
        src/main/jni/lame/cpp/LameLoader.h
        ${LIB_MP3_LAME}
        ${MPG_LIB}
        ${I386}
        ${VECTOR})

find_library(log-lib log)

target_link_libraries(LameLoader ${log-lib})