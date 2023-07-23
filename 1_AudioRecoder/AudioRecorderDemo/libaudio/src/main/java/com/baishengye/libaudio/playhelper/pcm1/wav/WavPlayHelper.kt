package com.baishengye.libaudio.playhelper.pcm1.wav

import com.baishengye.libaudio.config.decode.AudioDecodingFormat
import com.baishengye.libaudio.helper.WaveFileInfoHelper
import com.baishengye.libaudio.playhelper.pcm1.BasePlayHelper
import com.baishengye.libaudio.playhelper.pcm1.PcmPushTransport
import com.baishengye.libaudio.playhelper.pcm1.pcm.PcmDecodeConfig

class WavPlayHelper(
    config: PcmDecodeConfig,
    pushTransport: PcmPushTransport
) : BasePlayHelper(config, pushTransport) {
    override fun startPlaying(path: String) {
        //todo 将Wav播放所需的WavHead中的声道,音频编码(声道位数),采样率
        getWavHeader(path)

        super.startPlaying(path)
    }

    private fun getWavHeader(path: String) {
        val wavInfo = WaveFileInfoHelper(path)

        if (wavInfo.isSuccess) {
            config.sampleRateInHz = wavInfo.sampleRate.toInt()
            if (wavInfo.bitPerChannel == 1 && wavInfo.numChannels == 1) {
                config.audioDecodingFormat = AudioDecodingFormat.PCM_8BIT_MONO
            } else if (wavInfo.bitPerChannel == 2 && wavInfo.numChannels == 1) {
                config.audioDecodingFormat = AudioDecodingFormat.PCM_16BIT_MONO
            } else if (wavInfo.bitPerChannel == 1 && wavInfo.numChannels == 2) {
                config.audioDecodingFormat = AudioDecodingFormat.PCM_8BIT_STEREO
            } else if (wavInfo.bitPerChannel == 2 && wavInfo.numChannels == 1) {
                config.audioDecodingFormat = AudioDecodingFormat.PCM_16BIT_STEREO
            }
        }
    }
}