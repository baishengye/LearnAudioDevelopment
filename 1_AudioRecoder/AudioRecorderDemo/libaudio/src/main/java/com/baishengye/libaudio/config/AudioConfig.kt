package com.baishengye.libaudio.config

import android.media.MediaRecorder

open class AudioConfig(
    /**
     * 音频源，详见
     * - [MediaRecorder.AudioSource]
     */
    var audioSource: Int,
    /**
     * 采样率 赫兹
     * - 44100Hz 所有设备均可用
     * - 22050Hz  16000Hz  11025Hz
     */
    var sampleRateInHz: Int,
    /**
     * 帧数:每采160次样本通知将数据编码设置的音频格式*/
    var simplePreNotify: Int
) {
}