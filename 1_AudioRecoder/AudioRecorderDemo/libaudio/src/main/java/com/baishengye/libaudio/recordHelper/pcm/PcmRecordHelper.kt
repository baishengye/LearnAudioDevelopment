package com.baishengye.libaudio.recordHelper.pcm

import android.Manifest
import androidx.annotation.RequiresPermission
import com.baishengye.libaudio.config.encode.AudioEncodeConfig
import com.baishengye.libaudio.recordHelper.BaseAudioRecordHelper
import com.baishengye.libaudio.recordHelper.PullTransport
import java.io.File

class PcmRecordHelper @RequiresPermission(Manifest.permission.RECORD_AUDIO) constructor(
    file: File,
    config: AudioEncodeConfig,
    pullTransport: PullTransport
) : BaseAudioRecordHelper(file, config, pullTransport)