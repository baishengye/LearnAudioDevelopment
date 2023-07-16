package com.baishengye.libaudio.config

import android.media.MediaRecorder

data class AudioRecordConfig(
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
    var audioEncodingFormat: AudioEncodingFormat = AudioEncodingFormat.PCM_16BIT_MONO,
    /**
     * 帧数:每多少帧进行多少次采样*/
    var simplePreFrame:Int = 160
){
    fun bytesPerSample(): Byte {
        return audioEncodingFormat.bytesPreSimple
    }
}