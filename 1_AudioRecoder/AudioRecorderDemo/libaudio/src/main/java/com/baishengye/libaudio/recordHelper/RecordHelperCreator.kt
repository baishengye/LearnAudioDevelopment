package com.baishengye.libaudio.recordHelper

import android.Manifest
import androidx.annotation.RequiresPermission
import com.baishengye.libaudio.recordHelper.pcm.PcmEncodeConfig
import com.baishengye.libaudio.recordHelper.pcm.PcmRecordHelper
import com.baishengye.libaudio.recordHelper.wav.WavRecordHelper
import java.io.File

object RecordHelperCreator {

    /**
     * 获取 pcm 格式的音频记录器
     *
     * @param file          保存录音的文件
     * @param config        录音参数配置
     * @param pullTransport 数据推送器
     * @return pcm格式的音频记录器
     */
    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    @Throws(IllegalArgumentException::class)
    fun pcm(file: File, config: PcmEncodeConfig, pullTransport: PullTransport): RecordHelper {
        return PcmRecordHelper(file, config, pullTransport)
    }

    /**
     * 获取 Wav 格式的音频记录器
     *
     * @param file          保存录音的文件
     * @param config        录音参数配置
     * @param pullTransport 数据推送器
     * @return pcm格式的音频记录器
     */
    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    @Throws(IllegalArgumentException::class)
    fun wav(file: File, config: PcmEncodeConfig, pullTransport: PullTransport): RecordHelper {
        return WavRecordHelper(file, config, pullTransport)
    }
}