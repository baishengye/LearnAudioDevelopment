package com.baishengye.libaudio.config.decode

import android.media.AudioFormat

/**
 * 解码和编码都可以使用这个类
 * @param bytesPreSimple 一个样本所占字节
 * @param audioFormat 音频编码
 * @param channelConfig 声道*/
enum class AudioDecodingFormat(
    val bytesPreSimple: Byte,
    val audioFormat: Int,
    val channelConfig: Int
) {

    PCM_8BIT_MONO(1, AudioFormat.ENCODING_PCM_8BIT, AudioFormat.CHANNEL_OUT_MONO),//8Bit单声道
    PCM_16BIT_MONO(2, AudioFormat.ENCODING_PCM_16BIT, AudioFormat.CHANNEL_OUT_MONO),//16bit单声道
    PCM_8BIT_STEREO(2, AudioFormat.ENCODING_PCM_8BIT, AudioFormat.CHANNEL_OUT_STEREO),//8bit双声道
    PCM_16BIT_STEREO(4, AudioFormat.ENCODING_PCM_16BIT, AudioFormat.CHANNEL_OUT_STEREO)//16bit双声道
}