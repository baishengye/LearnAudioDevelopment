package com.baishengye.liblame.record.pcm

import android.Manifest
import androidx.annotation.RequiresPermission
import com.baishengye.liblame.record.BaseAudioRecordHelper
import com.baishengye.liblame.record.PullTransport
import java.io.File

class Mp3RecordHelper @RequiresPermission(Manifest.permission.RECORD_AUDIO) constructor(
    file: File,
    config: PcmEncodeConfig,
    pullTransport: PullTransport
) : BaseAudioRecordHelper(file, config, pullTransport)