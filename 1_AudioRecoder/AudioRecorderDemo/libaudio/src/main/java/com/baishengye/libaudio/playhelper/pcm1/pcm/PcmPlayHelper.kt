package com.baishengye.libaudio.playhelper.pcm1.pcm

import com.baishengye.libaudio.playhelper.pcm1.BasePlayHelper
import com.baishengye.libaudio.playhelper.pcm1.PcmPushTransport

class PcmPlayHelper(
    config: PcmDecodeConfig,
    pushTransport: PcmPushTransport
) : BasePlayHelper(config, pushTransport) {
}