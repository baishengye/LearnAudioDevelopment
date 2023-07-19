package com.baishengye.libaudio.playhelper

import com.baishengye.libaudio.config.decode.AudioDecodeConfig

class PcmPlayHelper(
    config: AudioDecodeConfig,
    pushTransport: PushTransport
) : BasePlayHelper(config, pushTransport) {
}