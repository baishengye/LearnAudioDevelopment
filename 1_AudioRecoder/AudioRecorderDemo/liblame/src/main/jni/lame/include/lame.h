/*
 *	Interface to MP3 LAME encoding engine
 *
 *	Copyright (c) 1999 Mark Taylor
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 */

/* $Id: lame.h,v 1.192 2017/08/31 14:14:46 robert Exp $ */

#ifndef LAME_LAME_H
#define LAME_LAME_H

/* for size_t typedef */
#include <stddef.h>
/* for va_list typedef */
#include <stdarg.h>
/* for FILE typedef, TODO: remove when removing lame_mp3_tags_fid */
#include <stdio.h>

#if defined(__cplusplus)
extern "C" {
#endif

typedef void (*lame_report_function)(const char *format, va_list ap);

#if defined(WIN32) || defined(_WIN32)
#undef CDECL
#define CDECL __cdecl
#else
#define CDECL
#endif

#define DEPRECATED_OR_OBSOLETE_CODE_REMOVED 1


/*在LAME中，vbr_mode_e 是一个枚举类型，
 * 用于表示MP3音频编码中的可变比特率（Variable Bit Rate，VBR）模式。
 * VBR模式允许在编码过程中根据音频信号的复杂性和特性动态地调整比特率，
 * 从而在保持高音质的同时获得更好的压缩效率。
 * 主要涉及到三种编码模式:
 * CBR：固定比特率，音质一致，文件大小固定。
 * VBR：可变比特率，音质优先，文件大小会根据音频内容动态调整。
 * ABR：平均比特率，平衡音质和文件大小的一种模式。
 * 一般情况下都会选择ABR*/
typedef enum vbr_mode_e {
    vbr_off = 0,//关闭可变比特率，即使用固定比特率（CBR）模式编码
    vbr_mt,     //该常量已经过时（obsolete），是 vbr_mtrh 的同义词（same as vbr_mtrh）          /* obsolete, same as vbr_mtrh */
    vbr_rh,    //表示使用VBR模式，并采用相对高质量（Relative High）的设置
    vbr_abr,   //表示使用平均比特率（Average Bit Rate）模式，VBR模式下的一种变种
    vbr_mtrh,  // 表示使用VBR模式，并采用音质和比特率的平衡设置（Middle quality, Tune to BitRate-NoiseRatio curve）
    vbr_max_indicator, //用于进行一些健全性检查（sanity checks），不建议在实际使用中直接使用它   /* Don't use this! It's used for sanity checks.       */
    vbr_default = vbr_mtrh  //将默认的VBR模式设置为 vbr_mtrh，意味着默认情况下使用音质和比特率平衡的VBR模式  /* change this to change the default VBR mode of LAME */
} vbr_mode;


/* MPEG modes */
/*在LAME中，MPEG_mode_e 是一个枚举类型，
 * 用于表示MP3音频编码中的不同声道模式（Channel Mode）。
 * 声道模式定义了音频编码时使用的声道布局，即音频数据在输出文件中的声道设置。*/
typedef enum MPEG_mode_e {
    STEREO = 0, //表示立体声（Stereo）模式，音频被编码为左右两个声道
    JOINT_STEREO, //表示联合立体声（Joint Stereo）模式，是一种高级立体声模式，可以在一些情况下提供更好的压缩效率
    DUAL_CHANNEL, //双声道（Dual Channel）模式。注释中提到LAME并不支持这个模式  /* LAME doesn't supports this! */
    MONO, //单声道（Mono）模式，音频只有一个声道
    NOT_SET, //未设置模式，可能在某些情况下用于标识一个未知的或者尚未指定的模式
    MAX_INDICATOR //用于进行一些健全性检查（sanity checks），不建议在实际使用中直接使用它  /* Don't use this! It's used for sanity checks. */
} MPEG_mode;

/* Padding types */
/*在LAME中，Padding_type_e 参数用于控制MP3音频编码中的填充（Padding）策略。
 * 填充是一种在音频帧中添加额外的字节，以确保每个帧的大小满足特定的要求。
 * 这是因为在MP3编码中，音频帧的大小通常是固定的，而实际的音频数据可能无法完全填充满一个固定大小的帧。
 * */
typedef enum Padding_type_e {
    PAD_NO = 0, //表示没有填充（No Padding），在音频编码中不进行填充操作
    PAD_ALL,   //表示对所有的帧进行填充（Padding），即所有帧都增加填充字节
    PAD_ADJUST, //表示根据需要自动调整填充（Padding），可能只对一部分帧进行填充以优化编码结果
    PAD_MAX_INDICATOR  //这个常量用于进行一些健全性检查（sanity checks），不建议在实际使用中直接使用它  /* Don't use this! It's used for sanity checks. */
} Padding_type;


/*presets*/
/*
 * LAME音频编码器中的预设模式（Preset Mode）*/
typedef enum preset_mode_e {
    /*values from 8 to 320 should be reserved for abr bitrates*/
    /*for abr I'd suggest to directly use the targeted bitrate as a value*/
    ABR_8 = 8,//平均比特率（ABR）预设模式，目标平均比特率为 8 kbps
    ABR_320 = 320,//平均比特率（ABR）预设模式，目标平均比特率为 320 kbps

    /*LAME选择了在枚举常量中使用 Vx 和 VBR_xx 的双重命名方式。
     * 例如，V9 和 VBR_10 是同一个预设模式，
     * 都表示目标平均比特率为 410 kbps。
     * 这样的设计让LAME的预设模式名称即可以与自身的命名约定匹配，
     * 也可以与FhG的命名约定对应
     * FhG的MP3编码器中，使用类似 VBR_xx 的命名方式来表示VBR预设模式
     * LAME中，通常使用类似 Vx 的命名方式来表示VBR（可变比特率）预设模式，
     * 其中的 x 代表目标平均比特率，单位为 kbps。例如，V0 表示目标平均比特率为 0 kbps*/
    V9 = 410,
    VBR_10 = 410,
    V8 = 420,
    VBR_20 = 420,
    V7 = 430,
    VBR_30 = 430,
    V6 = 440,
    VBR_40 = 440,
    V5 = 450,
    VBR_50 = 450,
    V4 = 460,
    VBR_60 = 460,
    V3 = 470,
    VBR_70 = 470,
    V2 = 480,
    VBR_80 = 480,
    V1 = 490,
    VBR_90 = 490,
    V0 = 500,
    VBR_100 = 500,


    /*still there for compatibility*/
    /*保留用于兼容性，表示较早版本的预设模式*/
    R3MIX = 1000,//特定的音频编码参数设置
    STANDARD = 1001,//一组适中的音频编码参数设置，可以在保持一定音质的同时获得较小的文件大小。适用于一般情况下的音频编码
    EXTREME = 1002,//较高的音频编码质量设置，会在保持更高音质的前提下生成较大的文件大小。适用于对音质要求较高的场景
    INSANE = 1003,//最高的音频编码质量设置，会生成最高质量的音频文件，但文件大小可能会非常大。适用于对音质要求极高的特殊场景
    STANDARD_FAST = 1004,//在相对较短时间内编码完成的设置。这个模式会在保持较好音质的同时，尽可能地减少编码时间
    EXTREME_FAST = 1005,//在更短时间内编码完成的设置。这个模式会在保持较高音质的同时，进一步减少编码时间
    MEDIUM = 1006,//介于标准质量和高质量之间的设置，适用于对音质和文件大小都有一定要求的场景
    MEDIUM_FAST = 1007//在相对较短时间内编码完成的设置，同时相对于MEDIUM模式会略微降低音质以获得更快的编码速度
} preset_mode;


/*asm optimizations*/
/*音频编码器使用的特定的汇编优化选项。
 * 这些选项用于利用特定的处理器指令集来优化编码器的性能，
 * 从而在执行音频编码时提高速度和效率*/
typedef enum asm_optimizations_e {
    MMX = 1,//编码器使用MMX（MultiMedia eXtension）指令集进行优化。MMX是Intel处理器的一种SIMD（Single Instruction, Multiple Data）指令集，用于进行多媒体数据处理加速
    AMD_3DNOW = 2,//编码器使用AMD 3DNow!（三维现在）指令集进行优化。3DNow!是AMD处理器的一种SIMD指令集，类似于Intel的MMX，用于进行多媒体数据处理加速
    SSE = 3//编码器使用SSE（Streaming SIMD Extensions）指令集进行优化。SSE是Intel处理器的一种SIMD指令集，其后续版本分别称为SSE2、SSE3等，用于进行更高级的多媒体数据处理加速。
} asm_optimizations;


/* psychoacoustic model */
/*音频编码器使用的心理声学模型（Psychoacoustic Model）。
 * 心理声学模型在音频编码中起到重要作用，
 * 它模拟人类听觉的特性，以便在编码过程中根据人耳对声音的感知特性，
 * 优化对音频信号的压缩*/
typedef enum Psy_model_e {
    PSY_GPSYCHO = 1,//使用GPSYCHO心理声学模型。GPSYCHO是LAME中的一种心理声学模型，它根据人耳的听觉特性，对音频信号进行分析和优化。该模型可以在保持较高音质的前提下，实现更高的压缩率。
    PSY_NSPSYTUNE = 2//使用NSPSYTUNE心理声学模型。NSPSYTUNE是LAME中的另一种心理声学模型，它也是根据人耳听觉特性进行优化的模型。该模型可能会在某些特定情况下提供不同的音频编码效果。
} Psy_model;


/* buffer considerations */
/*音频编码器输出缓冲区的约束条件（Buffer Constraint）。
 * 输出缓冲区约束条件是为了确保编码器输出的比特流在解码时能够正确地被播放器解码和播放。*/
typedef enum buffer_constraint_e {
    MDB_DEFAULT = 0,//默认的输出缓冲区约束条件。在这种模式下，编码器将根据默认设置来确定输出缓冲区的约束条件，以保证通常的解码器都能正确地解码和播放音频数据
    MDB_STRICT_ISO = 1,//严格的ISO输出缓冲区约束条件。在这种模式下，编码器将遵循ISO标准定义的严格约束条件，以确保输出的比特流在ISO标准下能够正确解码和播放
    MDB_MAXIMUM = 2//最大化输出缓冲区约束条件。在这种模式下，编码器将尽可能地放宽输出缓冲区的约束条件，以最大化编码器的性能和效率。这可能会导致输出比特流稍微超出ISO标准的约束，但通常兼容大多数播放器。
} buffer_constraint;


struct lame_global_struct;//用于存储LAME音频编码器的全局参数和状态信息
typedef struct lame_global_struct lame_global_flags;//将 struct lame_global_struct 结构体类型重命名为 lame_global_flags。通过这样的方式，以后在代码中可以使用 lame_global_flags 来代替 struct lame_global_struct，使代码更加简洁和易读。
typedef lame_global_flags *lame_t;// lame_global_flags指针的别名




/***********************************************************************
 *
 *  The LAME API
 *  These functions should be called, in this order, for each
 *  MP3 file to be encoded.  See the file "API" for more documentation
 *
 ***********************************************************************/


/*
 * REQUIRED:
 * initialize the encoder.  sets default for all encoder parameters,
 * returns NULL if some malloc()'s failed
 * otherwise returns pointer to structure needed for all future
 * API calls.
 */
/*用于初始化LAME音频编码器*/
lame_global_flags *CDECL lame_init(void);
#if DEPRECATED_OR_OBSOLETE_CODE_REMOVED
#else
/* obsolete version */
int CDECL lame_init_old(lame_global_flags *);
#endif

/*
 * OPTIONAL:
 * set as needed to override defaults
 */

/********************************************************************
 *  input stream description
 ***********************************************************************/
/* number of samples.  default = 2^32-1   */
/*设置lame的样本数,比如:44.1kHz的采样率表示在一秒钟内采集了44100个样本,而可以设置
 * Lame只获取44100个样本中的100个样本*/
int CDECL lame_set_num_samples(lame_global_flags *, unsigned long);

/*获取样本数*/
unsigned long CDECL lame_get_num_samples(const lame_global_flags *);

/* input sample rate in Hz.  default = 44100hz */
/*设置lame的采样率:44.1kHz的采样率表示在一秒钟内采集了44100个样本*/
int CDECL lame_set_in_samplerate(lame_global_flags *, int);

/*获取采样率*/
int CDECL lame_get_in_samplerate(const lame_global_flags *);

/* number of channels in input stream. default=2  */
/*设置lame转码的声道数*/
int CDECL lame_set_num_channels(lame_global_flags *, int);

/*获取lame的声道数*/
int CDECL lame_get_num_channels(const lame_global_flags *);

/*
  scale the input by this amount before encoding.  default=1
  (not used by decoding routines)
*/
/*在进行音频编码之前对输入数据进行的缩放操作,缩放音频的震幅：具体体现为缩放声量，参数是缩放比例*/
int CDECL lame_set_scale(lame_global_flags *, float);

/*获取缩放比例*/
float CDECL lame_get_scale(const lame_global_flags *);

/*
  scale the channel 0 (left) input by this amount before encoding.  default=1
  (not used by decoding routines)
*/
/*在进行音频编码之前对输入数据进行的缩放操作,缩放音频的震幅：具体体现为缩放声量，参数是缩放比例，只缩放左声道*/
int CDECL lame_set_scale_left(lame_global_flags *, float);
float CDECL lame_get_scale_left(const lame_global_flags *);

/*
  scale the channel 1 (right) input by this amount before encoding.  default=1
  (not used by decoding routines)
*/
/*在进行音频编码之前对输入数据进行的缩放操作,缩放音频的震幅：具体体现为缩放声量，参数是缩放比例，只缩放右声道*/
int CDECL lame_set_scale_right(lame_global_flags *, float);
float CDECL lame_get_scale_right(const lame_global_flags *);

/*
  output sample rate in Hz.  default = 0, which means LAME picks best value
  based on the amount of compression.  MPEG only allows:
  MPEG1    32, 44.1,   48khz
  MPEG2    16, 22.05,  24
  MPEG2.5   8, 11.025, 12
  (not used by decoding routines)
*/
/*设置输出音频的采样率，不同的采样率会使Lame采用不同的编码器*/
int CDECL lame_set_out_samplerate(lame_global_flags *, int);

/*获取输出音频的采样率*/
int CDECL lame_get_out_samplerate(const lame_global_flags *);


/********************************************************************
 *  general control parameters
 ***********************************************************************/
/* 1=cause LAME to collect data for an MP3 frame analyzer. default=0 */
/*是否开启MP3帧分析器数据收集 0就是不开,1就是开启*/
int CDECL lame_set_analysis(lame_global_flags *, int);
int CDECL lame_get_analysis(const lame_global_flags *);

/*
  1 = write a Xing VBR header frame.
  default = 1
  this variable must have been added by a Hungarian notation Windows programmer :-)
*/
/*在LAME编码器中，可以设置是否在输出的MP3文件中写入VBR标签，以标记该文件是采用了VBR编码方式
 * 1是采用,0是不采用*/
int CDECL lame_set_bWriteVbrTag(lame_global_flags *, int);
int CDECL lame_get_bWriteVbrTag(const lame_global_flags *);

/* 1=decode only.  use lame/mpglib to convert mp3/ogg to wav.  default=0 */
/*设置是否仅进行解码操作的函数
 * 当设置为仅进行解码操作时，LAME（或MPGLib）将会用来将MP3或OGG格式的音频文件解码为WAV格式*/
int CDECL lame_set_decode_only(lame_global_flags *, int);
int CDECL lame_get_decode_only(const lame_global_flags *);

#if DEPRECATED_OR_OBSOLETE_CODE_REMOVED
#else
/* 1=encode a Vorbis .ogg file.  default=0 */
/* DEPRECATED */
int CDECL lame_set_ogg(lame_global_flags *, int);
int CDECL lame_get_ogg(const lame_global_flags *);
#endif

/*
  internal algorithm selection.  True quality is determined by the bitrate
  but this variable will effect quality by selecting expensive or cheap algorithms.
  quality=0..9.  0=best (very slow).  9=worst.
  recommended:  2     near-best quality, not too slow
                5     good quality, fast
                7     ok quality, really fast
*/
/*算法的质量选项。质量级别的范围为 0 到 9，
 * 其中 0 表示最佳质量但速度较慢，9 表示较低质量但速度较快*/
int CDECL lame_set_quality(lame_global_flags *, int);
int CDECL lame_get_quality(const lame_global_flags *);

/*
  mode = 0,1,2,3 = stereo, jstereo, dual channel (not supported), mono
  default: lame picks based on compression ration and input channels
*/
/*声道模式用于决定音频编码的声道布局，即音频数据中的通道数。在LAME编码器中，声道模式的取值可以是 0、1、2、3，分别对应以下含义：

0：立体声（Stereo）模式，音频数据包含左声道和右声道。
1：联合立体声（Joint Stereo）模式，根据编码器的压缩率和输入声道数选择立体声或双声道。
2：双声道（Dual Channel）模式，不支持，已废弃。
3：单声道（Mono）模式，音频数据只包含一个声道。*/
int CDECL lame_set_mode(lame_global_flags *, MPEG_mode);
MPEG_mode CDECL lame_get_mode(const lame_global_flags *);

#if DEPRECATED_OR_OBSOLETE_CODE_REMOVED
#else
/*
  mode_automs.  Use a M/S mode with a switching threshold based on
  compression ratio
  DEPRECATED
*/
int CDECL lame_set_mode_automs(lame_global_flags *, int);
int CDECL lame_get_mode_automs(const lame_global_flags *);
#endif

/*
  force_ms.  Force M/S for all frames.  For testing only.
  default = 0 (disabled)
*/
/*M/S编码是一种音频编码技术，用于对音频信号进行编码和解码。
 * 在M/S编码中，音频信号被分为两个部分：中间（Middle）部分和侧边（Side）部分。
 * 中间部分包含两个声道的相同信号，而侧边部分包含两个声道的不同信号。
 * 通过这种方式，M/S编码可以在一定程度上减少编码后的数据量，提高压缩效率。

默认情况下，LAME编码器不会强制使用M/S编码，对应的返回值为 0。
 如果需要强制在所有帧中使用M/S编码，可以调用 lame_set_force_ms 函数并传入参数 1。
 使用 lame_get_force_ms 函数可以获取当前是否强制使用M/S编码。

请注意，强制使用M/S编码仅用于测试和特定场景，一般情况下不建议开启此选项。
 在实际应用中，LAME编码器会根据音频数据的特性自动选择合适的编码方式。*/
int CDECL lame_set_force_ms(lame_global_flags *, int);
int CDECL lame_get_force_ms(const lame_global_flags *);

/* use free_format?  default = 0 (disabled) */
/*自由格式（Free Format）是一种MP3文件编码的格式，
 * 它允许在编码的过程中不受限制地使用不同的帧大小。
 * 通常情况下，MP3文件采用固定的帧大小，这有助于解码器在解码时准确地定位每个帧。
 * 但自由格式允许帧大小的变化，这样可以在一定程度上提高编码效率和灵活性。

默认情况下，LAME编码器不使用自由格式，对应的返回值为 0。如果需要使用自由格式的MP3编码，
 可以调用 lame_set_free_format 函数并传入参数 1。
 使用 lame_get_free_format 函数可以获取当前是否使用自由格式。

需要注意的是，使用自由格式的MP3文件可能不被所有的MP3解码器所支持，
 因此在实际应用中需要谨慎使用。
 大多数情况下，采用标准的MP3格式会更为通用和兼容。*/
int CDECL lame_set_free_format(lame_global_flags *, int);
int CDECL lame_get_free_format(const lame_global_flags *);

/* perform ReplayGain analysis?  default = 0 (disabled) */
/*ReplayGain是一种用于平衡音频音量的算法。
 * 通过ReplayGain分析，可以计算出音频文件的音量增益值，
 * 然后在播放时自动调整音量，使得不同音频文件的音量听起来更为统一，
 * 避免音量差异带来的不便。
 * 1是开启,0是不开启*/
int CDECL lame_set_findReplayGain(lame_global_flags *, int);
int CDECL lame_get_findReplayGain(const lame_global_flags *);

/* decode on the fly. Search for the peak sample. If the ReplayGain
 * analysis is enabled then perform the analysis on the decoded data
 * stream. default = 0 (disabled)
 * NOTE: if this option is set the build-in decoder should not be used */
/*“On the fly”操作意味着在解码过程中实时执行某些操作。
 * 具体到这里的函数，lame_set_decode_on_the_fly 和 lame_get_decode_on_the_fly
 * 是用于控制解码器在解码音频数据时是否执行以下两项操作：
在解码过程中搜索峰值样本（Search for the peak sample）。
如果启用了ReplayGain分析，则在解码的数据流上执行ReplayGain分析。
 1是开启,0是不开启*/
int CDECL lame_set_decode_on_the_fly(lame_global_flags *, int);
int CDECL lame_get_decode_on_the_fly(const lame_global_flags *);

#if DEPRECATED_OR_OBSOLETE_CODE_REMOVED
#else
/* DEPRECATED: now does the same as lame_set_findReplayGain()
   default = 0 (disabled) */
int CDECL lame_set_ReplayGain_input(lame_global_flags *, int);
int CDECL lame_get_ReplayGain_input(const lame_global_flags *);

/* DEPRECATED: now does the same as
   lame_set_decode_on_the_fly() && lame_set_findReplayGain()
   default = 0 (disabled) */
int CDECL lame_set_ReplayGain_decode(lame_global_flags *, int);
int CDECL lame_get_ReplayGain_decode(const lame_global_flags *);

/* DEPRECATED: now does the same as lame_set_decode_on_the_fly()
   default = 0 (disabled) */
int CDECL lame_set_findPeakSample(lame_global_flags *, int);
int CDECL lame_get_findPeakSample(const lame_global_flags *);
#endif

/* counters for gapless encoding */
/*在无缝编码中，如果有多个音频文件需要合并成一个连续的音频流，
 * 需要确保相邻文件之间没有间隔（Gap），从而实现无缝播放。
 * 为了实现这一点，LAME编码器允许用户指定无缝编码计数器的值。

无缝编码计数器的作用是为了确保在合并音频文件时正确处理相邻文件之间的样本，
 以消除可能存在的间隔。每个音频文件都应该有一个唯一的无缝编码计数器值，
 这样LAME编码器在合并文件时就能够正确地进行无缝编码。*/
int CDECL lame_set_nogap_total(lame_global_flags *, int);
int CDECL lame_get_nogap_total(const lame_global_flags *);

/*无缝编码索引计数器用于指定当前音频数据的索引，
 * 从而在合并音频数据时确保正确处理相邻文件之间的样本，
 * 从而实现无缝播放。每个音频文件都应该有一个唯一的无缝编码索引计数器值，
 * 以便在合并文件时能够正确地进行无缝编码。
 * 无缝编码通常用于特定的应用场景，
 * 例如将多个音轨合并为一个音频文件，以实现无间隙的连续播放。
 * 在一般情况下，不需要手动设置无缝编码索引计数器，
 * 因为大多数音频播放器和编码器会自动处理这一问题。
 * 这些函数主要用于控制和处理无缝编码的特殊需求*/
int CDECL lame_set_nogap_currentindex(lame_global_flags *, int);
int CDECL lame_get_nogap_currentindex(const lame_global_flags *);


/*
 * OPTIONAL:
 * Set printf like error/debug/message reporting functions.
 * The second argument has to be a pointer to a function which looks like
 *   void my_debugf(const char *format, va_list ap)
 *   {
 *       (void) vfprintf(stdout, format, ap);
 *   }
 * If you use NULL as the value of the pointer in the set function, the
 * lame buildin function will be used (prints to stderr).
 * To quiet any output you have to replace the body of the example function
 * with just "return;" and use it in the set function.
 */
/*log工具函数:lame_report_function指的是log时要做的事情的函数指针*/
int CDECL lame_set_errorf(lame_global_flags *, lame_report_function);
int CDECL lame_set_debugf(lame_global_flags *, lame_report_function);
int CDECL lame_set_msgf(lame_global_flags *, lame_report_function);


/* set one of brate compression ratio.  default is compression ratio of 11.  */
/*设置比特率
 * 在音频编码中，比特率是用于表示音频文件的每秒数据量，通常以kbps（千比特每秒）为单位。
 * 较高的比特率可以提供更高的音频质量，但会产生更大的文件大小。*/
int CDECL lame_set_brate(lame_global_flags *, int);
int CDECL lame_get_brate(const lame_global_flags *);

/*设置压缩比
 * 压缩比是比特率和音频数据的压缩之间的关系。较高的压缩比意味着更高的压缩率，
 * 可以减小文件大小，但可能会损失一些音频质量。*/
int CDECL lame_set_compression_ratio(lame_global_flags *, float);
float CDECL lame_get_compression_ratio(const lame_global_flags *);

/*设置编码器的预设，预设是一组预定义的参数集合，它们根据不同的音频质量要求和压缩目标进行了优化*/
int CDECL lame_set_preset(lame_global_flags *gfp, int);

/*设置汇编优化，汇编优化是一种编译器和硬件层面的优化手段，旨在提高编码器的性能和效率。
 * 第二个参数是 asm_optimizations
 * 第三个参数是 0：关闭优化，1开启优化*/
int CDECL lame_set_asm_optimizations(lame_global_flags *gfp, int, int);



/********************************************************************
 *  frame params
 ***********************************************************************/
/* mark as copyright.  default=0 */
/*设置音频数据是否有版权受保护，1：受保护,0：不受保护*/
int CDECL lame_set_copyright(lame_global_flags *, int);
int CDECL lame_get_copyright(const lame_global_flags *);

/* mark as original.  default=1 */
/*设置音频数据是否原创，1：原创,0：非原创*/
int CDECL lame_set_original(lame_global_flags *, int);
int CDECL lame_get_original(const lame_global_flags *);

/* error_protection.  Use 2 bytes from each frame for CRC checksum. default=0 */
/*错误保护是一种技术，用于在音频数据传输或存储过程中检测和纠正潜在的错误。
 * 0：不开启,1：开启*/
int CDECL lame_set_error_protection(lame_global_flags *, int);
int CDECL lame_get_error_protection(const lame_global_flags *);

#if DEPRECATED_OR_OBSOLETE_CODE_REMOVED
#else
/* padding_type. 0=pad no frames  1=pad all frames 2=adjust padding(default) */
int CDECL lame_set_padding_type(lame_global_flags *, Padding_type);
Padding_type CDECL lame_get_padding_type(const lame_global_flags *);
#endif

/* MP3 'private extension' bit  Meaningless.  default=0 */
/*设置和获取MP3编码中的“private extension”位标记。
 * 这个位标记在MP3文件的帧头中的一位，它是保留位，目前并没有具体的定义和使用。
 * 仅当需要特定的MP3格式兼容性测试或调试时，可能会使用这个函数来设置或获取“private extension”位标记的值。
 * 对于一般的MP3编码和解码任务，可以忽略这个标记。*/
int CDECL lame_set_extension(lame_global_flags *, int);
int CDECL lame_get_extension(const lame_global_flags *);

/* enforce strict ISO compliance.  default=0 */
/*通过设置严格的ISO兼容性标记，
 * 可以强制LAME遵守ISO对MP3编码的规定，
 * 以确保生成的MP3文件与ISO标准的要求完全一致。
 * 0：不开启*/
int CDECL lame_set_strict_ISO(lame_global_flags *, int);
int CDECL lame_get_strict_ISO(const lame_global_flags *);


/********************************************************************
 * quantization/noise shaping
 ***********************************************************************/

/* disable the bit reservoir. For testing only. default=0 */
/*启用或禁用MP3编码中的比特储备功能。
 * 比特储备是一种技术，允许在一个MP3帧中储存多于该帧实际所需的比特数，
 * 以便在后续帧中使用这些多余的比特，从而提高整体的编码效率。
 * 这种技术可以改善编码质量，尤其是在低比特率的情况下
 * 禁用比特储备功能可能会导致编码效率和质量的降低，特别是在低比特率的情况下。因此，
 * 禁用比特储备通常仅在特定的测试和调试需求下使用，并不推荐在实际应用中禁用比特储备功能。
 * 1：禁用比特存储功能*/
int CDECL lame_set_disable_reservoir(lame_global_flags *, int);
int CDECL lame_get_disable_reservoir(const lame_global_flags *);

/* select a different "best quantization" function. default=0  */
/*选择不同的“最佳量化”函数用于整个块和短块的编码过程*/
int CDECL lame_set_quant_comp(lame_global_flags *, int);
int CDECL lame_get_quant_comp(const lame_global_flags *);

/*设置不同的值来选择不同的量化函数*/
int CDECL lame_set_quant_comp_short(lame_global_flags *, int);
int CDECL lame_get_quant_comp_short(const lame_global_flags *);

/*启用或禁用实验性功能X，启用或禁用实验性功能X*/
int CDECL lame_set_experimentalX(lame_global_flags *, int); /* compatibility*/
int CDECL lame_get_experimentalX(const lame_global_flags *);

/* another experimental option.  for testing only */
/*启用或禁用实验性功能Y，启用或禁用实验性功能Y*/
int CDECL lame_set_experimentalY(lame_global_flags *, int);
int CDECL lame_get_experimentalY(const lame_global_flags *);

/* another experimental option.  for testing only */
/*启用或禁用实验性功能Z，启用或禁用实验性功能Z*/
int CDECL lame_set_experimentalZ(lame_global_flags *, int);
int CDECL lame_get_experimentalZ(const lame_global_flags *);

/* Naoki's psycho acoustic model.  default=0 */
/*exp_nspsytune标记是LAME编码器中用于启用或禁用特定的Psychoacoustic Model的实验性版本。
 * Psychoacoustic Model是MP3编码中用于感知音频信号的特性，并根据人耳听觉模型的特点，
 * 将编码的比特数分配给不同的频率区域以达到较好的音质和压缩效率的一种技术。
 * 实验性版本的Psychoacoustic Model可能未经充分测试，或者在未来的版本中可能会有所改变，
 * 一般情况下，不建议在生产环境中使用这些功能*/
int CDECL lame_set_exp_nspsytune(lame_global_flags *, int);
int CDECL lame_get_exp_nspsytune(const lame_global_flags *);

/*MS处理修复值（msfix）是用于修复MS（Mid/Side）编码中的问题的一个参数。
 * MS编码是一种用于降低立体声音频的比特率的技术。
 * 在MS编码中，立体声音频被编码为左声道（L）和差异信号（S），
 * 其中差异信号表示了两个声道之间的差异信息。
 * 然后，这些信号被编码为一个立体声音频流。
 * MS编码可能会导致一些问题，特别是在低比特率下。
 * lame_set_msfix 函数允许设置MS处理修复值，
 * 用于调整修复MS编码中的问题。
 * 通过调整这个参数，可以尝试改善MS编码的质量和效率。
 * MS处理修复值是一个浮点数，用户可以根据具体的音频内容和编码需求进行适当的调整和优化。
 * 通常情况下，不需要手动设置这个参数，因为LAME编码器会在默认设置下自动处理MS编码的问题。
 * 仅在特定的音频内容和编码需求下，可能需要手动调整这个参数*/
void CDECL lame_set_msfix(lame_global_flags *, double);
float CDECL lame_get_msfix(const lame_global_flags *);


/********************************************************************
 * VBR control
 ***********************************************************************/
/* Types of VBR.  default = vbr_off = CBR */
/*配置VBR模式*/
int CDECL lame_set_VBR(lame_global_flags *, vbr_mode);
vbr_mode CDECL lame_get_VBR(const lame_global_flags *);

/* VBR quality level.  0=highest  9=lowest  */
/*配置输出音频质量:int设置*/
int CDECL lame_set_VBR_q(lame_global_flags *, int);
int CDECL lame_get_VBR_q(const lame_global_flags *);

/* VBR quality level.  0=highest  9=lowest, Range [0,...,10[  */
/*配置输出音频质量:float设置,精度更高*/
int CDECL lame_set_VBR_quality(lame_global_flags *, float);
float CDECL lame_get_VBR_quality(const lame_global_flags *);

/* Ignored except for VBR=vbr_abr (ABR mode) */
/*用于设置VBR模式下的平均比特率*/
int CDECL lame_set_VBR_mean_bitrate_kbps(lame_global_flags *, int);
int CDECL lame_get_VBR_mean_bitrate_kbps(const lame_global_flags *);

/*设置VBR模式下的最小比特率*/
int CDECL lame_set_VBR_min_bitrate_kbps(lame_global_flags *, int);
int CDECL lame_get_VBR_min_bitrate_kbps(const lame_global_flags *);

/*设置VBR模式下的最大比特率*/
int CDECL lame_set_VBR_max_bitrate_kbps(lame_global_flags *, int);
int CDECL lame_get_VBR_max_bitrate_kbps(const lame_global_flags *);

/*
  1=strictly enforce VBR_min_bitrate.  Normally it will be violated for
  analog silence
*/
/*设置VBR模式下的硬最小比特率*/
int CDECL lame_set_VBR_hard_min(lame_global_flags *, int);
int CDECL lame_get_VBR_hard_min(const lame_global_flags *);

/* for preset */
#if DEPRECATED_OR_OBSOLETE_CODE_REMOVED
#else
int CDECL lame_set_preset_expopts(lame_global_flags *, int);
#endif

/********************************************************************
 * Filtering control
 ***********************************************************************/
/* freq in Hz to apply lowpass. Default = 0 = lame chooses.  -1 = disabled */
/*设置低通滤波器的频率,0：Lame自己适应,-1：不开启低通滤波*/
int CDECL lame_set_lowpassfreq(lame_global_flags *, int);
int CDECL lame_get_lowpassfreq(const lame_global_flags *);
/* width of transition band, in Hz.  Default = one polyphase filter band */
/*设置低通滤波器的过渡带宽度，
 * 在LAME音频编码器中，低通滤波器的过渡带宽度用于控制过渡带的宽度，
 * 从而影响音频信号中通带和阻带之间的过渡。过渡带宽度越大，
 * 过渡区域越宽，可能会导致更多的高频信息被保留，但也会增加文件的大小*/
int CDECL lame_set_lowpasswidth(lame_global_flags *, int);
int CDECL lame_get_lowpasswidth(const lame_global_flags *);

/* freq in Hz to apply highpass. Default = 0 = lame chooses.  -1 = disabled */
/*设置高通滤波器的频率,0：Lame自己适应,-1：不开启低通滤波*/
int CDECL lame_set_highpassfreq(lame_global_flags *, int);
int CDECL lame_get_highpassfreq(const lame_global_flags *);
/* width of transition band, in Hz.  Default = one polyphase filter band */
/*设置高通滤波器的过渡带宽度，
 * 在LAME音频编码器中，低通滤波器的过渡带宽度用于控制过渡带的宽度，
 * 从而影响音频信号中通带和阻带之间的过渡。过渡带宽度越大，
 * 过渡区域越宽，可能会导致更多的高频信息被保留，但也会增加文件的大小*/
int CDECL lame_set_highpasswidth(lame_global_flags *, int);

int CDECL lame_get_highpasswidth(const lame_global_flags *);


/********************************************************************
 * psycho acoustics and other arguments which you should not change
 * unless you know what you are doing
 ***********************************************************************/

/* only use ATH for masking */
/*掩蔽效应是指当较强的音频信号出现时，
 * 它可能会掩盖掉较弱的音频信号，
 * 导致较弱的信号在听觉上变得不可感知。
 * ATH是一种心理声学模型，
 * 用于计算相对于听觉阈值的音频信号的感知强度，
 * 从而进行掩蔽效应的计算。*/
/*参数为1时，表示只使用ATH进行掩蔽效应计算。
 * 这意味着编码器将仅考虑ATH对于信号的掩蔽效应，
 * 而不使用其他算法或模型*/
/*不清楚如何设置这个选项，建议保持默认值或使用LAME预设模式*/
int CDECL lame_set_ATHonly(lame_global_flags *, int);

int CDECL lame_get_ATHonly(const lame_global_flags *);

/* only use ATH for short blocks */
/*短块是音频信号在编码过程中进行频谱分析的一种方式。
 * 在某些情况下，编码器可能会选择使用短块进行编码，
 * 而不是使用长块（long blocks）。
 * 使用ATH进行掩蔽效应计算意味着在分析短块时，
 * 编码器将仅考虑ATH对于信号的掩蔽效应，而不使用其他算法或模型*/
/*设置只在短块中使用ATH进行掩蔽效应计算*/
int CDECL lame_set_ATHshort(lame_global_flags *, int);

int CDECL lame_get_ATHshort(const lame_global_flags *);

/* disable ATH */
/*关闭ATH处理,1:关闭*/
int CDECL lame_set_noATH(lame_global_flags *, int);

int CDECL lame_get_noATH(const lame_global_flags *);

/* select ATH formula */
/*计算掩蔽效应的ATH（Absolute Threshold of Hearing）公式类型有以下几种选项：

ATH_TYPE_APPROXIMATE: 近似型（默认值）
这是默认的ATH公式类型，用于计算掩蔽效应。
它基于一种近似的ATH模型，计算相对于听觉阈值的音频信号的感知强度。

ATH_TYPE_EXPERIMENTAL: 实验型
这是一种实验性的ATH公式类型，可能是某些研究或实验中使用的。
它可能基于更精确的心理声学模型，用于计算掩蔽效应。

ATH_TYPE_MMD: 感知模型依赖型
这是一种基于感知模型的ATH公式类型。
它可能基于更复杂的感知模型，用于计算相对于听觉阈值的音频信号的感知强度。
 非专业用户在使用LAME音频编码器时保持默认的ATH公式类型,lame中没有给出对应的说明和整型*/
int CDECL lame_set_ATHtype(lame_global_flags *, int);

int CDECL lame_get_ATHtype(const lame_global_flags *);

/* lower ATH by this many db */
/*降低ATH的幅度可以理解为降低绝对听阈的水平。绝对听阈是指在没有其他声音干扰的情况下，
 * 人耳能够感知的最低音频信号强度。通过降低ATH，编码器可以增加对较弱音频信号的敏感性，
 * 从而提高对低音量信号的编码质量*/
/*设置降低ATH的幅度*/
int CDECL lame_set_ATHlower(lame_global_flags *, float);

float CDECL lame_get_ATHlower(const lame_global_flags *);

/* select ATH adaptive adjustment type */
/*设置ATH自适应调整类型
 * 建议保持默认值或使用LAME预设模式*/
int CDECL lame_set_athaa_type(lame_global_flags *, int);

int CDECL lame_get_athaa_type(const lame_global_flags *);

#if DEPRECATED_OR_OBSOLETE_CODE_REMOVED
#else
/* select the loudness approximation used by the ATH adaptive auto-leveling  */
int CDECL lame_set_athaa_loudapprox( lame_global_flags *, int);
int CDECL lame_get_athaa_loudapprox( const lame_global_flags *);
#endif

/* adjust (in dB) the point below which adaptive ATH level adjustment occurs */
/*自适应ATH水平调整是一种功能，它根据音频信号的特性动态调整ATH的水平。
 * athaa_sensitivity 参数用于调整自适应ATH调整的触发点，
 * 即在多大程度上认为音频信号比绝对听阈更弱，从而触发ATH水平的动态调整
 * 增加灵敏度可能会导致编码器更积极地调整ATH水平，
 * 而降低灵敏度可能会使编码器更保守地进行调整。
 * 设置自适应ATH水平调整的灵敏度，单位通常是分贝（dB）*/
int CDECL lame_set_athaa_sensitivity(lame_global_flags *, float);
float CDECL lame_get_athaa_sensitivity(const lame_global_flags *);

#if DEPRECATED_OR_OBSOLETE_CODE_REMOVED
#else
/* OBSOLETE: predictability limit (ISO tonality formula) */
int CDECL lame_set_cwlimit(lame_global_flags *, int);
int CDECL lame_get_cwlimit(const lame_global_flags *);
#endif

/*
  allow blocktypes to differ between channels?
  default: 0 for jstereo, 1 for stereo
*/
/*短块类型是指将音频分成多个较短的时间块进行编码的一种策略。
 * 在立体声（stereo）模式下，LAME可以使用两种短块类型：
 * 正常短块（Short Block）和开始短块（Start Short Block）。
 * 而在联合立体声（jstereo）模式下，允许不同通道之间使用不同的短块类型，以进一步提高编码效率*/
/*用于设置允许不同通道之间使用不同的短块类型*/
int CDECL lame_set_allow_diff_short(lame_global_flags *, int);

int CDECL lame_get_allow_diff_short(const lame_global_flags *);

/* use temporal masking effect (default = 1) */
/*时域遮罩效应是一种心理声学现象，用于解释在音频信号中较强的声音掩盖或抑制较弱的声音。
 * LAME编码器可以利用这种效应来提高编码的效率和音频质量*/
/*设置使用时域遮罩效应的选项*/
int CDECL lame_set_useTemporal(lame_global_flags *, int);

int CDECL lame_get_useTemporal(const lame_global_flags *);

/* use temporal masking effect (default = 1) */
/*声道间比率用于控制编码器在立体声模式下对左声道和右声道之间的掩盖效应进行调整。
 * 较高的声道间比率会导致编码器更加注重掩盖效应，可能会导致对某些频段进行更强的掩盖，
 * 而较低的声道间比率则可能导致对声道之间的掩盖效应进行较少的调整。*/
/*用于设置声道间比率*/
int CDECL lame_set_interChRatio(lame_global_flags *, float);

float CDECL lame_get_interChRatio(const lame_global_flags *);

/* disable short blocks */
/*短块是将音频分成多个较短的时间块进行编码的一种策略。
 * 禁用短块意味着编码器将完全采用常规块（regular blocks）进行编码，而不使用短块*/
/*设置是否禁用短块*/
int CDECL lame_set_no_short_blocks(lame_global_flags *, int);

int CDECL lame_get_no_short_blocks(const lame_global_flags *);

/* force short blocks */
/*设置强制使用短块*/
int CDECL lame_set_force_short_blocks(lame_global_flags *, int);

int CDECL lame_get_force_short_blocks(const lame_global_flags *);

/* Input PCM is emphased PCM (for instance from one of the rarely
   emphased CDs), it is STRONGLY not recommended to use this, because
   psycho does not take it into account, and last but not least many decoders
   ignore these bits */
/*强调PCM音频是一种早期的音频处理技术，在现代音频编码中很少使用。
 * 通常情况下，不建议使用这个选项，除非您处理的音频确实是强调PCM音频。
 * 如果不确定是否需要启用强调PCM音频，建议保持默认值或使用 LAME 预设模式。
 * 设置强调PCM音频*/
int CDECL lame_set_emphasis(lame_global_flags *, int);
int CDECL lame_get_emphasis(const lame_global_flags *);



/************************************************************************/
/* internal variables, cannot be set...                                 */
/* provided because they may be of use to calling application           */
/************************************************************************/
/* version  0=MPEG-2  1=MPEG-1  (2=MPEG-2.5)     */
/* 获取使用的LAME 编码器版本*/
int CDECL lame_get_version(const lame_global_flags *);

/* encoder delay   */
/*编码器延迟是指编码器处理音频数据时引入的延迟量。
 * 在音频编码过程中，编码器可能需要对音频数据进行处理和分析，
 * 导致输出数据的产生比输入数据晚一些。这种延迟可能会影响音频的同步性和播放顺序。*/
/*获取编码器延迟信息*/
int CDECL lame_get_encoder_delay(const lame_global_flags *);

/*
  padding appended to the input to make sure decoder can fully decode
  all input.  Note that this value can only be calculated during the
  call to lame_encoder_flush().  Before lame_encoder_flush() has
  been called, the value of encoder_padding = 0.
*/
/*编码器填充是指在编码过程中为了确保解码器能够完全解码所有输入而追加到输入数据的额外字节。
 * 这种填充通常用于确保解码器不会因为缺少一些信息而无法正确解码音频。
 * 函数 lame_get_encoder_padding 在调用 lame_encoder_flush() 函数之后才能正确计算并获取填充值。
 * 在 lame_encoder_flush() 调用之前，填充值将为0
 * 获取编码器填充信息*/
int CDECL lame_get_encoder_padding(const lame_global_flags *);

/* size of MPEG frame */
/*MPEG 帧是音频数据在 MPEG 音频编码中的基本单元。它包含了一定的音频采样数，
 * 并在编码过程中被分割成不同的子帧和帧头*/
/*获取 MPEG 帧大小*/
int CDECL lame_get_framesize(const lame_global_flags *);

/* number of PCM samples buffered, but not yet encoded to mp3 data. */
/*PCM 样本是未经过编码的原始音频样本。LAME 编码器在进行音频编码时，
 * 通常会先将一部分 PCM 样本进行缓冲，然后再进行编码处理。
 * 该函数允许您查询当前缓冲中尚未被编码的 PCM 样本的数量
 * 获取当前缓冲但尚未编码为 MP3 数据的 PCM 样本数量*/
int CDECL lame_get_mf_samples_to_encode(const lame_global_flags *gfp);

/*
  size (bytes) of mp3 data buffered, but not yet encoded.
  this is the number of bytes which would be output by a call to
  lame_encode_flush_nogap.  NOTE: lame_encode_flush() will return
  more bytes than this because it will encode the reamining buffered
  PCM samples before flushing the mp3 buffers.
*/
/*MP3 缓冲大小是指当前已经在缓冲中等待编码的 MP3 数据的大小。
 * 在进行音频编码时，LAME 编码器通常会先将一部分 PCM 样本进行缓冲，
 * 然后再进行编码处理。该函数允许您查询当前缓冲中尚未被编码的 MP3 数据的大小
 * 获取当前缓冲但尚未编码为 MP3 数据的大小*/
int CDECL lame_get_size_mp3buffer(const lame_global_flags *gfp);

/* number of frames encoded so far */
/*获取当前已编码的帧数*/
int CDECL lame_get_frameNum(const lame_global_flags *);

/*
  lame's estimate of the total number of frames to be encoded
   only valid if calling program set num_samples
*/
/*在进行音频编码时，LAME 编码器需要先知道要编码的音频的总采样数
 * ，或者由调用程序通过 lame_set_num_samples 函数设置了总采样数。
 * 根据总采样数，LAME 可以估计要编码的总帧数。
 * 获取 LAME 对要编码的总帧数的估计值*/
int CDECL lame_get_totalframes(const lame_global_flags *);

/* RadioGain value. Multiplied by 10 and rounded to the nearest. */
/*RadioGain 是一种音频增益值，用于描述音频的响度。在 LAME 编码器中，
 * RadioGain 值被乘以 10 并四舍五入，得到最近的整数值，以分贝为单位来表示音频的增益*/
int CDECL lame_get_RadioGain(const lame_global_flags *);

/* AudiophileGain value. Multipled by 10 and rounded to the nearest. */
/*AudiophileGain 是一种音频增益值，用于描述高保真（Audiophile）音频的响度。
 * 在 LAME 编码器中，AudiophileGain 值被乘以 10 并四舍五入，
 * 得到最近的整数值，以分贝为单位来表示音频的增益*/
int CDECL lame_get_AudiophileGain(const lame_global_flags *);

/* the peak sample */
/*用于获取音频数据的峰值采样值*/
/*峰值采样值是音频数据中的最大采样值，用于表示音频数据的最大振幅*/
float CDECL lame_get_PeakSample(const lame_global_flags *);

/* Gain change required for preventing clipping. The value is correct only if
   peak sample searching was enabled. If negative then the waveform
   already does not clip. The value is multiplied by 10 and rounded up. */
/*防止剪切是指为了避免音频数据出现裁剪（Clipping），
 * 即音频数据的振幅超过了所支持的范围。
 * LAME 编码器在进行编码时会检查音频数据的峰值振幅，
 * 并根据需要计算防止剪切所需的增益变化值。*/
/*获取防止剪切所需的增益变化值*/
int CDECL lame_get_noclipGainChange(const lame_global_flags *);

/* user-specified scale factor required for preventing clipping. Value is
   correct only if peak sample searching was enabled and no user-specified
   scaling was performed. If negative then either the waveform already does
   not clip or the value cannot be determined */
/*获取防止剪切所需的用户指定缩放因子*/
float CDECL lame_get_noclipScale(const lame_global_flags *);

/* returns the limit of PCM samples, which one can pass in an encode call
   under the constrain of a provided buffer of size buffer_size */
/*LAME 编码器在进行编码时，需要将 PCM 样本转换为 MP3 数据并存储在缓冲区中。
 * 这个函数可以帮助您确定在给定的编码缓冲区大小下，
 * 能够处理的最大 PCM 样本数，以避免缓冲区溢出。*/
/*获取最大 PCM 样本数*/
int CDECL lame_get_maximum_number_of_samples(lame_t gfp, size_t buffer_size);


/*
 * REQUIRED:
 * sets more internal configuration based on data provided above.
 * returns -1 if something failed.
 */
/*初始化lame*/
int CDECL lame_init_params(lame_global_flags *);


/*
 * OPTIONAL:
 * get the version number, in a string. of the form:
 * "3.63 (beta)" or just "3.63".
 */
/*获取 LAME 编码器版本号*/
const char *CDECL get_lame_version(void);

/*获取 LAME 编码器的简短版本号*/
const char *CDECL get_lame_short_version(void);

/*获取 LAME 编码器的非常简短版本号*/
const char *CDECL get_lame_very_short_version(void);

/*获取 LAME 编码器中心心声音频压缩算法的版本号*/
const char *CDECL get_psy_version(void);

/*获取 LAME 编码器的官方网址*/
const char *CDECL get_lame_url(void);

/*获取 LAME 编码器的操作系统位数*/
const char *CDECL get_lame_os_bitness(void);

/*
 * OPTIONAL:
 * get the version numbers in numerical form.
 */
/*Lame版本结构体*/
typedef struct {
    /* generic LAME version */
    int major;
    int minor;
    int alpha;               /* 0 if not an alpha version                  */
    int beta;                /* 0 if not a beta version                    */

    /* version of the psy model */
    int psy_major;
    int psy_minor;
    int psy_alpha;           /* 0 if not an alpha version                  */
    int psy_beta;            /* 0 if not a beta version                    */

    /* compile time features */
    const char *features;    /* Don't make assumptions about the contents! */
} lame_version_t;
void CDECL get_lame_version_numerical(lame_version_t *);


/*
 * OPTIONAL:
 * print internal lame configuration to message handler
 */
/*将 LAME 编码器的内部配置信息打印到消息处理器*/
void CDECL lame_print_config(const lame_global_flags *gfp);

/*将 LAME 编码器的内部信息打印到消息处理器*/
void CDECL lame_print_internals(const lame_global_flags *gfp);


/*
 * input pcm data, output (maybe) mp3 frames.
 * This routine handles all buffering, resampling and filtering for you.
 *
 * return code     number of bytes output in mp3buf. Can be 0
 *                 -1:  mp3buf was too small
 *                 -2:  malloc() problem
 *                 -3:  lame_init_params() not called
 *                 -4:  psycho acoustic problems
 *
 * The required mp3buf_size can be computed from num_samples,
 * samplerate and encoding rate, but here is a worst case estimate:
 *
 * mp3buf_size in bytes = 1.25*num_samples + 7200
 *
 * I think a tighter bound could be:  (mt, March 2000)
 * MPEG1:
 *    num_samples*(bitrate/8)/samplerate + 4*1152*(bitrate/8)/samplerate + 512
 * MPEG2:
 *    num_samples*(bitrate/8)/samplerate + 4*576*(bitrate/8)/samplerate + 256
 *
 * but test first if you use that!
 *
 * set mp3buf_size = 0 and LAME will not check if mp3buf_size is
 * large enough.
 *
 * NOTE:
 * if gfp->num_channels=2, but gfp->mode = 3 (mono), the L & R channels
 * will be averaged into the L channel before encoding only the L channel
 * This will overwrite the data in buffer_l[] and buffer_r[].
 *
*/
/*将输入的 PCM 数据编码成 MP3 帧
 * gfp: 一个指向 lame_global_flags 结构体的指针，表示 LAME 编码器的全局上下文句柄，用于配置编码器的参数和选项。
buffer_l: 一个指向 short int 数组的指针，表示左声道的 PCM 数据缓冲区。
buffer_r: 一个指向 short int 数组的指针，表示右声道的 PCM 数据缓冲区。如果是单声道音频，可以将该参数设置为 NULL。
nsamples: 一个整数，表示每个声道的 PCM 样本数。
mp3buf: 一个指向 unsigned char 数组的指针，表示用于存储编码后的 MP3 数据的缓冲区。
mp3buf_size: 一个整数，表示输出缓冲区的大小，即能容纳编码后的 MP3 数据的最大字节数。

 返回值：
 大于 0: 表示编码后的 MP3 数据的字节数，即成功将 PCM 数据编码为 MP3 帧。
-1: 表示传入的输出缓冲区 mp3buf 太小，无法容纳编码后的 MP3 数据。
-2: 表示内存分配问题，可能是由于 malloc() 调用失败导致的。
-3: 表示 lame_init_params() 函数未被调用，即初始化参数的函数未执行。
-4: 表示心理声学处理出现问题，可能是由于心理声学模型计算时出现异常。*/
int CDECL lame_encode_buffer(
        lame_global_flags *gfp,           /* global context handle         */
        const short int buffer_l[],   /* PCM data for left channel     */
        const short int buffer_r[],   /* PCM data for right channel    */
        const int nsamples,      /* number of samples per channel */
        unsigned char *mp3buf,        /* pointer to encoded MP3 stream */
        const int mp3buf_size); /* number of valid octets in this
                                              stream                        */

/*
 * as above, but input has L & R channel data interleaved.
 * NOTE:
 * num_samples = number of samples in the L (or R)
 * channel, not the total number of samples in pcm[]
 */
/*将输入的交错式的 PCM 数据（左右声道交替排列）编码成 MP3 帧
 * gfp: 一个指向 lame_global_flags 结构体的指针，表示 LAME 编码器的全局上下文句柄，用于配置编码器的参数和选项。
pcm: 一个指向 short int 数组的指针，表示左右声道交替排列的 PCM 数据缓冲区。
num_samples: 一个整数，表示每个声道的 PCM 样本数，而不是 pcm[] 中所有 PCM 样本的总数。
mp3buf: 一个指向 unsigned char 数组的指针，表示用于存储编码后的 MP3 数据的缓冲区。
mp3buf_size: 一个整数，表示输出缓冲区的大小，即能容纳编码后的 MP3 数据的最大字节数。*/
int CDECL lame_encode_buffer_interleaved(
        lame_global_flags *gfp,           /* global context handlei        */
        short int pcm[],         /* PCM data for left and right
                                              channel, interleaved          */
        int num_samples,   /* number of samples per channel,
                                              _not_ number of samples in
                                              pcm[]                         */
        unsigned char *mp3buf,        /* pointer to encoded MP3 stream */
        int mp3buf_size); /* number of valid octets in this
                                              stream                        */


/* as lame_encode_buffer, but for 'float's.
 * !! NOTE: !! data must still be scaled to be in the same range as
 * short int, +/- 32768
 */
/*将输入的 PCM 数据编码成 MP3 帧
 * 输入的数据是float精度类型
 * 输入的浮点型 PCM 数据应该在与 short int 数据相同的范围内进行缩放，即在 +/- 32768 的范围内。*/
int CDECL lame_encode_buffer_float(
        lame_global_flags *gfp,           /* global context handle         */
        const float pcm_l[],      /* PCM data for left channel     */
        const float pcm_r[],      /* PCM data for right channel    */
        const int nsamples,      /* number of samples per channel */
        unsigned char *mp3buf,        /* pointer to encoded MP3 stream */
        const int mp3buf_size); /* number of valid octets in this
                                              stream                        */

/* as lame_encode_buffer, but for 'float's.
 * !! NOTE: !! data must be scaled to +/- 1 full scale
 */
/*将输入的PCM 数据编码成 MP3 帧
 * 输入的数据是ieee_float精度类型
 * 输入的浮点型 PCM 数据应该在与 short int 数据相同的范围内进行缩放，即在 +/- 32768 的范围内。*/
int CDECL lame_encode_buffer_ieee_float(
        lame_t gfp,
        const float pcm_l[],          /* PCM data for left channel     */
        const float pcm_r[],          /* PCM data for right channel    */
        const int nsamples,
        unsigned char *mp3buf,
        const int mp3buf_size);

/*将输入的 (左右声道混合)PCM 数据编码成 MP3 帧
 * 输入的数据是ieee_float精度类型
 * 输入的浮点型 PCM 数据应该在与 short int 数据相同的范围内进行缩放，即在 +/- 32768 的范围内。*/
int CDECL lame_encode_buffer_interleaved_ieee_float(
        lame_t gfp,
        const float pcm[],             /* PCM data for left and right
                                              channel, interleaved          */
        const int nsamples,
        unsigned char *mp3buf,
        const int mp3buf_size);

/* as lame_encode_buffer, but for 'double's.
 * !! NOTE: !! data must be scaled to +/- 1 full scale
 */
/*将输入的PCM 数据编码成 MP3 帧
 * 输入的数据是ieee_double精度类型
 * 输入的浮点型 PCM 数据应该在在  +/- 1 的范围内进行缩放。*/
int CDECL lame_encode_buffer_ieee_double(
        lame_t gfp,
        const double pcm_l[],          /* PCM data for left channel     */
        const double pcm_r[],          /* PCM data for right channel    */
        const int nsamples,
        unsigned char *mp3buf,
        const int mp3buf_size);

/*将输入的 (左右声道混合)PCM 数据编码成 MP3 帧
 * 输入的数据是ieee_double精度类型
 * 输入的浮点型 PCM 数据应该在在  +/- 1 的范围内进行缩放。*/
int CDECL lame_encode_buffer_interleaved_ieee_double(
        lame_t gfp,
        const double pcm[],             /* PCM data for left and right
                                              channel, interleaved          */
        const int nsamples,
        unsigned char *mp3buf,
        const int mp3buf_size);

/* as lame_encode_buffer, but for long's
 * !! NOTE: !! data must still be scaled to be in the same range as
 * short int, +/- 32768
 *
 * This scaling was a mistake (doesn't allow one to exploit full
 * precision of type 'long'.  Use lame_encode_buffer_long2() instead.
 *
 */
/*将输入的PCM 数据编码成 MP3 帧
 * 输入的数据是long精度类型
 * 输入的浮点型 PCM 数据应该在与 short int 数据相同的范围内进行缩放，即在 +/- 32768 的范围内。*/
int CDECL lame_encode_buffer_long(
        lame_global_flags *gfp,           /* global context handle         */
        const long buffer_l[],       /* PCM data for left channel     */
        const long buffer_r[],       /* PCM data for right channel    */
        const int nsamples,      /* number of samples per channel */
        unsigned char *mp3buf,        /* pointer to encoded MP3 stream */
        const int mp3buf_size); /* number of valid octets in this
                                              stream                        */

/* Same as lame_encode_buffer_long(), but with correct scaling.
 * !! NOTE: !! data must still be scaled to be in the same range as
 * type 'long'.   Data should be in the range:  +/- 2^(8*size(long)-1)
 *
 */
/*将输入的PCM 数据编码成 MP3 帧
 * 输入的数据是long精度类型
 * 输入的浮点型 PCM 数据应该即在+/- 2^(8*size(long)-1)的范围内在进行缩放。*/
int CDECL lame_encode_buffer_long2(
        lame_global_flags *gfp,           /* global context handle         */
        const long buffer_l[],       /* PCM data for left channel     */
        const long buffer_r[],       /* PCM data for right channel    */
        const int nsamples,      /* number of samples per channel */
        unsigned char *mp3buf,        /* pointer to encoded MP3 stream */
        const int mp3buf_size); /* number of valid octets in this
                                              stream                        */

/* as lame_encode_buffer, but for int's
 * !! NOTE: !! input should be scaled to the maximum range of 'int'
 * If int is 4 bytes, then the values should range from
 * +/- 2147483648.
 *
 * This routine does not (and cannot, without loosing precision) use
 * the same scaling as the rest of the lame_encode_buffer() routines.
 *
 */
/*将输入的PCM 数据编码成 MP3 帧
 * 输入的数据是int精度类型
 * 输入的浮点型 PCM 数据应该即在+/- 2147483648的范围内在进行缩放。*/
int CDECL lame_encode_buffer_int(
        lame_global_flags *gfp,           /* global context handle         */
        const int buffer_l[],       /* PCM data for left channel     */
        const int buffer_r[],       /* PCM data for right channel    */
        const int nsamples,      /* number of samples per channel */
        unsigned char *mp3buf,        /* pointer to encoded MP3 stream */
        const int mp3buf_size); /* number of valid octets in this
                                              stream                        */

/*
 * as above, but for interleaved data.
 * !! NOTE: !! data must still be scaled to be in the same range as
 * type 'int32_t'.   Data should be in the range:  +/- 2^(8*size(int32_t)-1)
 * NOTE:
 * num_samples = number of samples in the L (or R)
 * channel, not the total number of samples in pcm[]
 */
/*将输入的（左右声道混合）PCM 数据编码成 MP3 帧
 * 输入的数据是int精度类型
 * 输入的浮点型 PCM 数据应该即在+/- 2^(8*size(int32_t)-1)的范围内在进行缩放。*/
int
lame_encode_buffer_interleaved_int(
        lame_t gfp,
        const int pcm[],            /* PCM data for left and right
                                              channel, interleaved          */
        const int nsamples,          /* number of samples per channel,
                                              _not_ number of samples in
                                              pcm[]                         */
        unsigned char *mp3buf,            /* pointer to encoded MP3 stream */
        const int mp3buf_size);     /* number of valid octets in this
                                              stream                        */



/*
 * REQUIRED:
 * lame_encode_flush will flush the intenal PCM buffers, padding with
 * 0's to make sure the final frame is complete, and then flush
 * the internal MP3 buffers, and thus may return a
 * final few mp3 frames.  'mp3buf' should be at least 7200 bytes long
 * to hold all possible emitted data.
 *
 * will also write id3v1 tags (if any) into the bitstream
 *
 * return code = number of bytes output to mp3buf. Can be 0
 */
/*在编码结束时将剩余的 PCM 数据和内部 MP3 缓冲区刷新并输出为 MP3 帧*/
int CDECL lame_encode_flush(
        lame_global_flags *gfp,    /* global context handle                 */
        unsigned char *mp3buf, /* pointer to encoded MP3 stream         */
        int size);  /* number of valid octets in this stream */

/*
 * OPTIONAL:
 * lame_encode_flush_nogap will flush the internal mp3 buffers and pad
 * the last frame with ancillary data so it is a complete mp3 frame.
 *
 * 'mp3buf' should be at least 7200 bytes long
 * to hold all possible emitted data.
 *
 * After a call to this routine, the outputed mp3 data is complete, but
 * you may continue to encode new PCM samples and write future mp3 data
 * to a different file.  The two mp3 files will play back with no gaps
 * if they are concatenated together.
 *
 * This routine will NOT write id3v1 tags into the bitstream.
 *
 * return code = number of bytes output to mp3buf. Can be 0
 */
/*用于在编码结束时将内部 MP3 缓冲区刷新，并在最后一帧中填充附加数据，以确保它是一个完整的 MP3 帧*/
int CDECL lame_encode_flush_nogap(
        lame_global_flags *gfp,    /* global context handle                 */
        unsigned char *mp3buf, /* pointer to encoded MP3 stream         */
        int size);  /* number of valid octets in this stream */

/*
 * OPTIONAL:
 * Normally, this is called by lame_init_params().  It writes id3v2 and
 * Xing headers into the front of the bitstream, and sets frame counters
 * and bitrate histogram data to 0.  You can also call this after
 * lame_encode_flush_nogap().
 */
/*初始化比特流并写入 ID3v2 和 Xing 头部到输出的 MP3 数据流的前部。该函数还会将帧计数器和比特率直方图数据设置为 0*/
int CDECL lame_init_bitstream(
        lame_global_flags *gfp);    /* global context handle                 */



/*
 * OPTIONAL:    some simple statistics
 * a bitrate histogram to visualize the distribution of used frame sizes
 * a stereo mode histogram to visualize the distribution of used stereo
 *   modes, useful in joint-stereo mode only
 *   0: LR    left-right encoded
 *   1: LR-I  left-right and intensity encoded (currently not supported)
 *   2: MS    mid-side encoded
 *   3: MS-I  mid-side and intensity encoded (currently not supported)
 *
 * attention: don't call them after lame_encode_finish
 * suggested: lame_encode_flush -> lame_*_hist -> lame_close
 */
/*统计不同比特率的帧大小分布*/
void CDECL lame_bitrate_hist(
        const lame_global_flags *gfp,
        int bitrate_count[14]);//保存不同比特率范围内的帧数量统计结果。比特率范围通常是从 8 kbps 到 320 kbps。
/*统计不同比特率的帧大小分布，并将比特率以 kbps 为单位保存到数组中*/
void CDECL lame_bitrate_kbps(
        const lame_global_flags *gfp,
        int bitrate_kbps[14]);//保存不同比特率范围内的帧数量统计结果，以 kbps 为单位。比特率范围通常是从 8 kbps 到 320 kbps。
/*统计不同立体声编码模式的帧数量分布
 * stereo_mode_count[4]: 一个长度为 4 的整数数组，用于保存不同立体声编码模式的帧数量统计结果。
0: LR - 左声道和右声道编码
1: LR-I - 左声道和右声道编码（目前不支持）
2: MS - 中侧编码
3: MS-I - 中侧编码（目前不支持）*/
void CDECL lame_stereo_mode_hist(
        const lame_global_flags *gfp,
        int stereo_mode_count[4]);

/*统计不同比特率和立体声编码模式的帧数量分布*/
void CDECL lame_bitrate_stereo_mode_hist(
        const lame_global_flags *gfp,
        int bitrate_stmode_count[14][4]);//保存不同比特率和立体声编码模式的帧数量统计结果。
/*统计不同块类型的帧数量分布*/
void CDECL lame_block_type_hist(
        const lame_global_flags *gfp,
        int btype_count[6]);//保存不同块类型的帧数量统计结果。
/*统计不同比特率和块类型的帧数量分布
 * bitrate_btype_count[14][6]: 一个 14x6 的二维整数数组，
 * 用于保存不同比特率和块类型的帧数量统计结果。
 * 数组的第一维表示比特率范围，
 * 通常从 8 kbps 到 320 kbps；第二维表示不同块类型。

块类型的取值范围为 0 到 5，表示不同的块类型：
0: 短块，Short Blocks
1: 开始长块，Start Long Blocks
2: 结束长块，End Long Blocks
3: 短块+开始长块，Short Blocks + Start Long Blocks
4: 短块+结束长块，Short Blocks + End Long Blocks
5: 短块+开始长块+结束长块，Short Blocks + Start Long Blocks + End Long Blocks*/
void CDECL lame_bitrate_block_type_hist(
        const lame_global_flags *gfp,
        int bitrate_btype_count[14][6]);

#if (DEPRECATED_OR_OBSOLETE_CODE_REMOVED && 0)
#else
/*
 * OPTIONAL:
 * lame_mp3_tags_fid will rewrite a Xing VBR tag to the mp3 file with file
 * pointer fid.  These calls perform forward and backwards seeks, so make
 * sure fid is a real file.  Make sure lame_encode_flush has been called,
 * and all mp3 data has been written to the file before calling this
 * function.
 * NOTE:
 * if VBR  tags are turned off by the user, or turned off by LAME because
 * the output is not a regular file, this call does nothing
 * NOTE:
 * LAME wants to read from the file to skip an optional ID3v2 tag, so
 * make sure you opened the file for writing and reading.
 * NOTE:
 * You can call lame_get_lametag_frame instead, if you want to insert
 * the lametag yourself.
*/
void CDECL lame_mp3_tags_fid(lame_global_flags *, FILE *fid);
#endif

/*
 * OPTIONAL:
 * lame_get_lametag_frame copies the final LAME-tag into 'buffer'.
 * The function returns the number of bytes copied into buffer, or
 * the required buffer size, if the provided buffer is too small.
 * Function failed, if the return value is larger than 'size'!
 * Make sure lame_encode flush has been called before calling this function.
 * NOTE:
 * if VBR  tags are turned off by the user, or turned off by LAME,
 * this call does nothing and returns 0.
 * NOTE:
 * LAME inserted an empty frame in the beginning of mp3 audio data,
 * which you have to replace by the final LAME-tag frame after encoding.
 * In case there is no ID3v2 tag, usually this frame will be the very first
 * data in your mp3 file. If you put some other leading data into your
 * file, you'll have to do some bookkeeping about where to write this buffer.
 */
/*
 * 获取 LAME 标签帧（LAME-tag frame），并将其复制到指定的缓冲区中。
 * LAME 标签帧是包含关于 LAME 编码器设置和统计信息的帧，
 * 它可以用于标识和描述 MP3 文件的编码参数和特性。

这个函数接受三个参数：

gfp: 一个指向 lame_global_flags 结构体的指针，表示 LAME 编码器的全局上下文句柄。

buffer: 一个指向用于存储 LAME 标签帧数据的缓冲区。

size: 缓冲区的大小，即可存储 LAME 标签帧数据的最大字节数。

函数的返回值为 size_t 类型，表示实际复制到缓冲区中的字节数，
 或者如果提供的缓冲区大小不足以容纳整个 LAME 标签帧，则返回所需的缓冲区大小。
 如果返回值大于 size，则表示函数执行失败，因为提供的缓冲区太小。

需要注意的是，如果用户关闭了 VBR 标签，
 或者 LAME 自己禁用了 VBR 标签，调用此函数将不会执行任何操作并返回 0。

在调用此函数之前，必须确保已经调用了 lame_encode_flush 来完成编码过程，
 以确保 LAME-tag frame 已经生成。
 另外，LAME 在 MP3 音频数据的开头插入了一个空帧，
 你需要在编码后将其替换为最终的 LAME 标签帧。
 如果 MP3 文件中没有 ID3v2 标签，通常这个帧将是文件中的第一个数据。
 如果你在文件开头添加了其他数据，你需要对写入缓冲区的位置进行一些记录。*/
size_t CDECL lame_get_lametag_frame(
        const lame_global_flags *, unsigned char *buffer, size_t size);

/*
 * REQUIRED:
 * final call to free all remaining buffers
 */
/*使用 LAME 编码器后的最后一次调用，用于释放所有剩余的缓冲区和资源，完成对 LAME 编码器的清理工作*/
int  CDECL lame_close(lame_global_flags *);

#if DEPRECATED_OR_OBSOLETE_CODE_REMOVED
#else
/*
 * OBSOLETE:
 * lame_encode_finish combines lame_encode_flush() and lame_close() in
 * one call.  However, once this call is made, the statistics routines
 * will no longer work because the data will have been cleared, and
 * lame_mp3_tags_fid() cannot be called to add data to the VBR header
 */
int CDECL lame_encode_finish(
        lame_global_flags*  gfp,
        unsigned char*      mp3buf,
        int                 size );
#endif


/*********************************************************************
 *
 * decoding
 *
 * a simple interface to mpglib, part of mpg123, is also included if
 * libmp3lame is compiled with HAVE_MPGLIB
 *
 *********************************************************************/
/*以下的涉及解码Mp3的操作函数*/

/*解码器的结构体*/
struct hip_global_struct;
typedef struct hip_global_struct hip_global_flags;
typedef hip_global_flags *hip_t;


/*
 * 用于存储解码后的MP3音频文件的一些信息。

结构体成员的含义如下：

header_parsed: 一个标志，表示MP3文件头是否已经解析并计算了后续的数据。
stereo: 表示音频的声道数，即双声道或单声道。
samplerate: 表示音频的采样率，即每秒采集的样本数。
bitrate: 表示音频的比特率，即每秒传输的比特数。
mode: 表示MP3帧的类型，如立体声、联合立体声、双声道等。
mode_ext: 表示MP3帧类型的扩展信息。
framesize: 表示每个MP3帧中的样本数。
以下数据是仅在mpglib检测到Xing VBR（可变比特率）头时计算的：

nsamp: 表示MP3文件中的样本总数。
totalframes: 表示MP3文件中的总帧数。
注意：最后一个成员framenum目前不是由mpglib例程计算的，它可能是用户自行维护的用于跟踪解码的帧数。*/
typedef struct {
    int header_parsed;   /* 1 if header was parsed and following data was
                          computed                                       */
    int stereo;          /* number of channels                             */
    int samplerate;      /* sample rate                                    */
    int bitrate;         /* bitrate                                        */
    int mode;            /* mp3 frame type                                 */
    int mode_ext;        /* mp3 frame type                                 */
    int framesize;       /* number of samples per mp3 frame                */

    /* this data is only computed if mpglib detects a Xing VBR header */
    unsigned long nsamp; /* number of samples in mp3 file.                 */
    int totalframes;     /* total number of frames in mp3 file             */

    /* this data is not currently computed by the mpglib routines */
    int framenum;        /* frames decoded counter                         */
} mp3data_struct;

/* required call to initialize decoder */
/*初始化一个Lame解码器*/
hip_t CDECL hip_decode_init(void);

/* cleanup call to exit decoder  */
/*在解码完成后释放解码器的资源和内存*/
int CDECL hip_decode_exit(hip_t gfp);

/* HIP reporting functions */
/*log函数*/
void CDECL hip_set_errorf(hip_t gfp, lame_report_function f);
void CDECL hip_set_debugf(hip_t gfp, lame_report_function f);
void CDECL hip_set_msgf(hip_t gfp, lame_report_function f);

/*********************************************************************
 * input 1 mp3 frame, output (maybe) pcm data.
 *
 *  nout = hip_decode(hip, mp3buf,len,pcm_l,pcm_r);
 *
 * input:
 *    len          :  number of bytes of mp3 data in mp3buf
 *    mp3buf[len]  :  mp3 data to be decoded
 *
 * output:
 *    nout:  -1    : decoding error
 *            0    : need more data before we can complete the decode
 *           >0    : returned 'nout' samples worth of data in pcm_l,pcm_r
 *    pcm_l[nout]  : left channel data
 *    pcm_r[nout]  : right channel data
 *
 *********************************************************************/
/*解码函数
 * mp3buf: 输入音频
 * len： 输入音频数组长度
 * pcm_l: 输出音频左声道
 * pcm_r: 输出音频右声道*/
int CDECL hip_decode(hip_t gfp, unsigned char *mp3buf, size_t len, short pcm_l[], short pcm_r[]
);

/* same as hip_decode, and also returns mp3 header data */
/*解码的同时获取到MP3音频的元数据*/
int CDECL
hip_decode_headers(hip_t gfp, unsigned char *mp3buf, size_t len, short pcm_l[], short pcm_r[],
                   mp3data_struct *mp3data
);

/* same as hip_decode, but returns at most one frame */
/*只返回一帧的pcm数据*/
int CDECL hip_decode1(hip_t gfp, unsigned char *mp3buf, size_t len, short pcm_l[], short pcm_r[]
);

/* same as hip_decode1, but returns at most one frame and mp3 header data */
/*只返回一帧的pcm数据同时获取到MP3音频的元数据*/
int CDECL
hip_decode1_headers(hip_t gfp, unsigned char *mp3buf, size_t len, short pcm_l[], short pcm_r[],
                    mp3data_struct *mp3data
);

/* same as hip_decode1_headers, but also returns enc_delay and enc_padding
   from VBR Info tag, (-1 if no info tag was found) */
/*解码的同时获取到MP3音频的元数据,还返回VBR信息标签中的enc_delay和enc_padding值
 * */
int CDECL
hip_decode1_headersB(hip_t gfp, unsigned char *mp3buf, size_t len, short pcm_l[], short pcm_r[],
                     mp3data_struct *mp3data, int *enc_delay, int *enc_padding
);



/* OBSOLETE:
 * lame_decode... functions are there to keep old code working
 * but it is strongly recommended to replace calls by hip_decode...
 * function calls, see above.
 */
#if DEPRECATED_OR_OBSOLETE_CODE_REMOVED
#else
int CDECL lame_decode_init(void);
int CDECL lame_decode(
        unsigned char *  mp3buf,
        int              len,
        short            pcm_l[],
        short            pcm_r[] );
int CDECL lame_decode_headers(
        unsigned char*   mp3buf,
        int              len,
        short            pcm_l[],
        short            pcm_r[],
        mp3data_struct*  mp3data );
int CDECL lame_decode1(
        unsigned char*  mp3buf,
        int             len,
        short           pcm_l[],
        short           pcm_r[] );
int CDECL lame_decode1_headers(
        unsigned char*   mp3buf,
        int              len,
        short            pcm_l[],
        short            pcm_r[],
        mp3data_struct*  mp3data );
int CDECL lame_decode1_headersB(
        unsigned char*   mp3buf,
        int              len,
        short            pcm_l[],
        short            pcm_r[],
        mp3data_struct*  mp3data,
        int              *enc_delay,
        int              *enc_padding );
int CDECL lame_decode_exit(void);

#endif /* obsolete lame_decode API calls */


/*********************************************************************
 *
 * id3tag stuff
 *
 *********************************************************************/

/*
 * id3tag.h -- Interface to write ID3 version 1 and 2 tags.
 *
 * Copyright (C) 2000 Don Melton.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307, USA.
 */

/* utility to obtain alphabetically sorted list of genre names with numbers */
/*获取按字母顺序排序的ID3标签流派（genre）名称及其对应的数字。*/
/*handler: 函数指针，用于处理每个genre名称及其对应的数字。
 * 该函数的原型为：void handler(int, const char *, void *)。
 * 参数 int 是genre的数字代码，const char * 是genre的名称，void * 是用户传递的cookie参数。
cookie: 传递给handler函数的用户自定义数据指针。
 可以用于在handler函数中访问其他数据。
该函数用于获取LAME库中内置的ID3标签流派列表，
 例如"Rock"、"Pop"、"Jazz"等，以及与每个流派对应的数字代码，例如"0"、"1"、"2"等。
 通过遍历该列表，用户可以获取完整的genre名称及其对应的数字代码。*/
void CDECL id3tag_genre_list(
        void (*handler)(int, const char *, void *),
        void *cookie);

/*用于初始化ID3标签。ID3标签是一种在MP3文件中存储元数据（例如歌曲标题、艺术家、专辑信息等）的标准格式。*/
void CDECL id3tag_init(lame_t gfp);

/* force addition of version 2 tag */
/*通过调用id3tag_add_v2函数，你可以确保MP3文件中包含版本2的ID3标签。*/
void CDECL id3tag_add_v2(lame_t gfp);

/* add only a version 1 tag */
/*调用id3tag_v1_only函数，你可以指定LAME编码器仅添加版本1的ID3标签。*/
void CDECL id3tag_v1_only(lame_t gfp);

/* add only a version 2 tag */
/*调用id3tag_v2_only函数，你可以指定LAME编码器仅添加版本2的ID3标签。*/
void CDECL id3tag_v2_only(lame_t gfp);

/* pad version 1 tag with spaces instead of nulls */
/*指定LAME编码器在生成版本1的ID3标签时，用空格来填充字段，而不是使用空字符*/
void CDECL id3tag_space_v1(lame_t gfp);

/* pad version 2 tag with extra 128 bytes */
/*生成版本2的ID3标签时，向标签的末尾添加额外的128字节的填充*/
void CDECL id3tag_pad_v2(lame_t gfp);

/* pad version 2 tag with extra n bytes */
/*生成版本2的ID3标签时，向标签的末尾添加额外的n字节的填充*/
void CDECL id3tag_set_pad(lame_t gfp, size_t n);

/*
 * 在ID3标签中写入
 * 歌曲标题、艺术家、专辑信息、年份和注释*/
void CDECL id3tag_set_title(lame_t gfp, const char *title);
void CDECL id3tag_set_artist(lame_t gfp, const char *artist);
void CDECL id3tag_set_album(lame_t gfp, const char *album);
void CDECL id3tag_set_year(lame_t gfp, const char *year);
void CDECL id3tag_set_comment(lame_t gfp, const char *comment);

/* return -1 result if track number is out of ID3v1 range
                    and ignored for ID3v1 */
/*设置MP3文件的音轨号（Track Number）信息。音轨号通常用于标识音频CD上的不同音轨*/
int CDECL id3tag_set_track(lame_t gfp, const char *track);

/* return non-zero result if genre name or number is invalid
  result 0: OK
  result -1: genre number out of range
  result -2: no valid ID3v1 genre name, mapped to ID3v1 'Other'
             but taken as-is for ID3v2 genre tag */
/*设置MP3文件的音乐流派（Genre）信息。音乐流派通常用于标识音频的类型、风格或类别，例如流行、摇滚、古典*/
int CDECL id3tag_set_genre(lame_t gfp, const char *genre);

/* return non-zero result if field name is invalid */
/*设置ID3v2标签中的自定义字段（Field）值
 * 函数返回值非零的情况是当字段名称无效时*/
int CDECL id3tag_set_fieldvalue(lame_t gfp, const char *fieldvalue);

/* return non-zero result if image type is invalid */
/*设置ID3v2标签中的专辑封面图像
 * 返回值为0表示设置图像数据成功*/
int CDECL id3tag_set_albumart(lame_t gfp, const char *image, size_t size);

/* lame_get_id3v1_tag copies ID3v1 tag into buffer.
 * Function returns number of bytes copied into buffer, or number
 * of bytes rquired if buffer 'size' is too small.
 * Function fails, if returned value is larger than 'size'.
 * NOTE:
 * This functions does nothing, if user/LAME disabled ID3v1 tag.
 */
/*将ID3v1标签复制到缓冲区*/
size_t CDECL lame_get_id3v1_tag(lame_t gfp, unsigned char *buffer, size_t size);

/* lame_get_id3v2_tag copies ID3v2 tag into buffer.
 * Function returns number of bytes copied into buffer, or number
 * of bytes rquired if buffer 'size' is too small.
 * Function fails, if returned value is larger than 'size'.
 * NOTE:
 * This functions does nothing, if user/LAME disabled ID3v2 tag.
 */
/*将ID3v2标签复制到缓冲区*/
size_t CDECL lame_get_id3v2_tag(lame_t gfp, unsigned char *buffer, size_t size);

/* normaly lame_init_param writes ID3v2 tags into the audio stream
 * Call lame_set_write_id3tag_automatic(gfp, 0) before lame_init_param
 * to turn off this behaviour and get ID3v2 tag with above function
 * write it yourself into your file.
 */
/*控制是否自动写入ID3v2标签到音频流*/
void CDECL lame_set_write_id3tag_automatic(lame_global_flags *gfp, int);
int CDECL lame_get_write_id3tag_automatic(lame_global_flags const *gfp);

/* experimental */
int CDECL id3tag_set_textinfo_latin1(lame_t gfp, char const *id, char const *text);

/* experimental */
int CDECL
id3tag_set_comment_latin1(lame_t gfp, char const *lang, char const *desc, char const *text);

#if DEPRECATED_OR_OBSOLETE_CODE_REMOVED
#else
/* experimental */
int CDECL id3tag_set_textinfo_ucs2(lame_t gfp, char const *id, unsigned short const *text);

/* experimental */
int CDECL id3tag_set_comment_ucs2(lame_t gfp, char const *lang,
                                  unsigned short const *desc, unsigned short const *text);

/* experimental */
int CDECL id3tag_set_fieldvalue_ucs2(lame_t gfp, const unsigned short *fieldvalue);
#endif

/* experimental */
int CDECL id3tag_set_fieldvalue_utf16(lame_t gfp, const unsigned short *fieldvalue);

/* experimental */
int CDECL id3tag_set_textinfo_utf16(lame_t gfp, char const *id, unsigned short const *text);

/* experimental */
int CDECL id3tag_set_comment_utf16(lame_t gfp, char const *lang, unsigned short const *desc,
                                   unsigned short const *text);


/***********************************************************************
*
*  list of valid bitrates [kbps] & sample frequencies [Hz].
*  first index: 0: MPEG-2   values  (sample frequencies 16...24 kHz)
*               1: MPEG-1   values  (sample frequencies 32...48 kHz)
*               2: MPEG-2.5 values  (sample frequencies  8...12 kHz)
***********************************************************************/

extern const int bitrate_table[3][16];
extern const int samplerate_table[3][4];

/* access functions for use in DLL, global vars are not exported */
/*返回指定MPEG版本和比特率表索引对应的比特率值（以kbps为单位）*/
int CDECL lame_get_bitrate(int mpeg_version, int table_index);

/*返回指定MPEG版本和采样率表索引对应的采样率值（以Hz为单位）*/
int CDECL lame_get_samplerate(int mpeg_version, int table_index);


/* maximum size of albumart image (128KB), which affects LAME_MAXMP3BUFFER
   as well since lame_encode_buffer() also returns ID3v2 tag data */
#define LAME_MAXALBUMART    (128 * 1024)//ID3中写入的专辑图像不能大于128KB

/* maximum size of mp3buffer needed if you encode at most 1152 samples for
   each call to lame_encode_buffer.  see lame_encode_buffer() below  
   (LAME_MAXMP3BUFFER is now obsolete)  */
#define LAME_MAXMP3BUFFER   (16384 + LAME_MAXALBUMART)

/*
 * LAME_OKAY 和 LAME_NOERROR: 表示没有错误，操作成功完成。
LAME_GENERICERROR: 表示一般性错误，未明确定义的错误。
LAME_NOMEM: 表示内存不足错误，无法分配所需的内存。
LAME_BADBITRATE: 表示比特率设置错误，指定的比特率不符合规范。
LAME_BADSAMPFREQ: 表示采样率设置错误，指定的采样率不符合规范。
LAME_INTERNALERROR: 表示内部错误，编码库发生了未知的内部错误。
此外，还有一些以 FRONTEND_ 开头的错误代码，
 这些错误代码通常用于前端应用程序与 LAME 编码库之间的通信错误，
 比如读取文件错误、写入文件错误、文件过大等*/
typedef enum {
    LAME_OKAY = 0,
    LAME_NOERROR = 0,
    LAME_GENERICERROR = -1,
    LAME_NOMEM = -10,
    LAME_BADBITRATE = -11,
    LAME_BADSAMPFREQ = -12,
    LAME_INTERNALERROR = -13,

    FRONTEND_READERROR = -80,
    FRONTEND_WRITEERROR = -81,
    FRONTEND_FILETOOLARGE = -82

} lame_errorcodes_t;

#if defined(__cplusplus)
}
#endif
#endif /* LAME_LAME_H */

