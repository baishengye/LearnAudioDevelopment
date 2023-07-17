package com.baishengye.libaudio.config

import android.media.AudioFormat

/**
 * @param bytesPreSimple 一个样本所占字节
 * @param audioFormat 音频编码
 * @param channelConfig 声道*/
enum class AudioEncodingFormat(val bytesPreSimple:Byte, val audioFormat:Int,val channelConfig:Int) {

    PCM_8BIT_MONO(1, AudioFormat.ENCODING_PCM_8BIT, AudioFormat.CHANNEL_IN_MONO),//8Bit单声道
    PCM_16BIT_MONO(2, AudioFormat.ENCODING_PCM_16BIT, AudioFormat.CHANNEL_IN_MONO),//16bit单声道
    PCM_8BIT_STEREO(2, AudioFormat.ENCODING_PCM_8BIT, AudioFormat.CHANNEL_IN_STEREO),//8bit双声道
    PCM_16BIT_STEREO(4, AudioFormat.ENCODING_PCM_16BIT, AudioFormat.CHANNEL_IN_STEREO)//16bit双声道
}