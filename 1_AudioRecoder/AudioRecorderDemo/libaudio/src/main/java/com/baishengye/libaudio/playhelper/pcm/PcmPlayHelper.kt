package com.baishengye.libaudio.playhelper.pcm

import com.baishengye.libaudio.playhelper.BasePlayHelper
import com.baishengye.libaudio.playhelper.PushTransport

class PcmPlayHelper(
    config: PcmDecodeConfig,
    pushTransport: PushTransport
) : BasePlayHelper(config, pushTransport) {
}