package com.baishengye.libaudio.recorder

import android.Manifest
import androidx.annotation.RequiresPermission
import com.baishengye.libaudio.config.AudioRecordConfig
import java.io.File

class PcmRecorder @RequiresPermission(Manifest.permission.RECORD_AUDIO) constructor(
    file: File,
    config: AudioRecordConfig,
    pullTransport: PullTransport
) : BaseDataRecorder(file, config, pullTransport)