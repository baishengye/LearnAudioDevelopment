package com.baishengye.libaudio.config.decode

import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.media.MediaRecorder
import com.baishengye.libaudio.config.AudioConfig

open class AudioDecodeConfig(
    audioSource: Int = MediaRecorder.AudioSource.MIC,
    sampleRateInHz: Int = 44100,
    /**
     * 解码格式
     * - [AudioDecodingFormat]
     * */
    var audioDecodingFormat: AudioDecodingFormat = AudioDecodingFormat.PCM_16BIT_STEREO,
    simplePreNotify: Int = 160,
    /**
     * 音频流格式（一般使用music）
     */
    var streamType: Int = AudioManager.STREAM_MUSIC,
    /**
     * 播放模式（一般使用流模式）
     */
    val mod: Int = AudioTrack.MODE_STREAM,
) : AudioConfig(
    audioSource, sampleRateInHz, simplePreNotify
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