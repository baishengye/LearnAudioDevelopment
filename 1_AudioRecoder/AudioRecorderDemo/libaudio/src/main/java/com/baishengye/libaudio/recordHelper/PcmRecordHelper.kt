package com.baishengye.libaudio.recordHelper

import android.Manifest
import androidx.annotation.RequiresPermission
import com.baishengye.libaudio.config.AudioRecordConfig
import java.io.File

class PcmRecordHelper @RequiresPermission(Manifest.permission.RECORD_AUDIO) constructor(
    file: File,
    config: AudioRecordConfig,
    pullTransport: PullTransport
) : BaseAudioRecorder(file, config, pullTransport)