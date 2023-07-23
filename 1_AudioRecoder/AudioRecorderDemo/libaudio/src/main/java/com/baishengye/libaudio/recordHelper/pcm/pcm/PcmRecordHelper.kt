package com.baishengye.libaudio.recordHelper.pcm.pcm

import android.Manifest
import androidx.annotation.RequiresPermission
import com.baishengye.libaudio.recordHelper.pcm.BaseAudioRecordHelper
import com.baishengye.libaudio.recordHelper.pcm.PcmPullTransport
import java.io.File

class PcmRecordHelper @RequiresPermission(Manifest.permission.RECORD_AUDIO) constructor(
    file: File,
    config: PcmEncodeConfig,
    pullTransport: PcmPullTransport
) : BaseAudioRecordHelper(file, config, pullTransport)