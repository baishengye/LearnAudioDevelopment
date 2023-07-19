package com.baishengye.libaudio.config.encode

import android.media.AudioFormat
import android.media.MediaRecorder

open class AudioEncodeConfig(

    /**
     * 音频源，详见
     * - [MediaRecorder.AudioSource]
     */
    var audioSource: Int = MediaRecorder.AudioSource.MIC,

    /**
     * 采样率 赫兹
     * - 44100Hz 所有设备均可用
     * - 22050Hz  16000Hz  11025Hz
     */
    var sampleRateInHz: Int = 44100,

    /**
     * 编码格式
     * - [AudioEncodingFormat]
     * */
    var audioEncodingFormat: AudioEncodingFormat = AudioEncodingFormat.PCM_16BIT_STEREO,


    /**
     * 帧数:每采160次样本通知将数据编码设置的音频格式*/
    var simplePreNotify: Int = 160,
) {
    /**
     * 每次采样占用多少字节
     * 计算方式: 音频编码(每个声道占用字节数1或2Byte)*声道数*/
    open fun bytesPerSample(): Byte {
        return audioEncodingFormat.bytesPreSimple
    }

    /**
     * 音频编码对应的bit数,可以理解成每个声道所占bit数*/
    open fun bitsPreChannel(): Byte {
        return if (audioEncodingFormat.audioFormat == AudioFormat.ENCODING_PCM_8BIT) {
            8
        } else {
            16
        }
    }

    /**
     * 声道数*/
    open fun channels(): Int {
        return if (audioEncodingFormat.channelConfig == AudioFormat.CHANNEL_IN_MONO) 1 else 2
    }
}