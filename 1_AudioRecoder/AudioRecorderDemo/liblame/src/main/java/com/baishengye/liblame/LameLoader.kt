package com.baishengye.liblame

import com.baishengye.liblame.LameBuilder.VbrMode

class LameLoader {

    companion object {
        init {
            System.loadLibrary("LameLoader")
        }

        fun initialize(builder: LameBuilder) {
            initLame(
                builder.inSampleRate,
                builder.outChannel,
                builder.outSampleRate,
                builder.outBitrate,
                builder.scaleInput,
                getIntForMode(builder.mode),
                getIntForVbrMode(builder.vbrMode),
                builder.quality,
                builder.vbrQuality,
                builder.abrMeanBitrate,
                builder.lowpassFreq,
                builder.highpassFreq,
                builder.id3tagTitle,
                builder.id3tagArtist,
                builder.id3tagAlbum,
                builder.id3tagYear,
                builder.id3tagComment
            );
        }

        /**
         * 获取Lame版本号
         */
        external fun getLameVersion(): String?

        /**
         * 以默认参数初始化Lame
         */
        external fun initLameDefault(): Int

        /**
         * 配置参数初始化Lame*/
        external fun initLame(
            inSampleRate: Int,//输入采样率
            outChannel: Int,//输出声道数
            outSampleRate: Int,//输出采样率
            outBitrate: Int,//输出比特率
            scaleInput: Float,//缩放比
            mode: Int,//声道模式
            vbrMode: Int,//VBR模式
            quality: Int,//质量
            vbrQuality: Int,//VBR质量
            abrMeanBitrate: Int,//平均比特率
            lowpassFreq: Int,//低通滤波器
            highpassFreq: Int,//高通滤波器
            id3tagTitle: String?,//标题
            id3tagArtist: String?,//艺术家
            id3tagAlbum: String?,//专辑
            id3tagYear: String?,//年份
            id3tagComment: String?//备注
        ): Int

        /**
         * 清理Lame缓冲区里的PCM数据，全部转成MP3帧*/
        external fun lameEncodeFlush(mp3buf: ByteArray): Int

        /**
         * 编码->mp3*/
        external fun lameEncodeBuffer(
            buffer_l: ShortArray, buffer_r: ShortArray,
            samples: Int, mp3buf: ByteArray
        ): Int

        /**
         * （左右双通道混合）编码->mp3*/
        external fun lameEncodeBufferInterleaved(
            pcm: ShortArray, samples: Int,
            mp3buf: ByteArray
        ): Int

        external fun initLess(
            inSampleRate: Int,
            outChannel: Int,
            outSampleRate: Int,
            outBitrate: Int,
            quality: Int
        )

        /**
         * 释放Lame*/
        external fun lameClose()

        ////UTILS
        private fun getIntForMode(mode: LameBuilder.Mode): Int {
            return when (mode) {
                LameBuilder.Mode.STEREO -> 0
                LameBuilder.Mode.JSTEREO -> 1
                LameBuilder.Mode.MONO -> 3
                LameBuilder.Mode.DEFAULT -> 4
            }
        }

        private fun getIntForVbrMode(mode: VbrMode): Int {
            return when (mode) {
                VbrMode.VBR_OFF -> 0
                VbrMode.VBR_RH -> 2
                VbrMode.VBR_ABR -> 3
                VbrMode.VBR_MTRH -> 4
                VbrMode.VBR_DEFAULT -> 6
            }
        }
    }
}