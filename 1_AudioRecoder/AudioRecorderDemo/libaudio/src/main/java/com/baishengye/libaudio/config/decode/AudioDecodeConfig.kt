package com.baishengye.libaudio.config.decode

import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack

open class AudioDecodeConfig(
    /**
     * 采样率 赫兹
     * - 44100Hz 所有设备均可用
     * - 22050Hz  16000Hz  11025Hz
     */
    var sampleRateInHz: Int = 44100,
    /**
     * 解码格式
     * - [AudioDecodingFormat]
     * */
    var audioDecodingFormat: AudioDecodingFormat = AudioDecodingFormat.PCM_16BIT_STEREO,

    /**
     * 音频流格式（一般使用music）
     */
    var streamType: Int = AudioManager.STREAM_MUSIC,
    /**
     * 播放模式（一般使用流模式）
     */
    val mod: Int = AudioTrack.MODE_STREAM,

    /**
     * 帧数:每采160次样本通知将数据编码设置的音频格式*/
    var simplePreNotify: Int = 160,
) {
    /**
     * 每次采样占用多少字节
     * 计算方式: 音频编码(每个声道占用字节数1或2Byte)*声道数*/
    open fun bytesPerSample(): Byte {
        return audioDecodingFormat.bytesPreSimple
    }

    /**
     * 音频编码对应的bit数,可以理解成每个音道所占bit数*/
    open fun bitsPreChannel(): Byte {
        return if (audioDecodingFormat.audioFormat == AudioFormat.ENCODING_PCM_8BIT) {
            8
        } else {
            16
        }
    }

    /**
     * 声道数*/
    open fun channels(): Int {
        return if (audioDecodingFormat.channelConfig == AudioFormat.CHANNEL_OUT_MONO) 1 else 2
    }
}