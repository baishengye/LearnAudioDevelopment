package com.baishengye.libaudio.recordHelper.wav

import android.Manifest
import androidx.annotation.RequiresPermission
import com.baishengye.libaudio.config.encode.AudioEncodeConfig
import com.baishengye.libaudio.recordHelper.BaseAudioRecordHelper
import com.baishengye.libaudio.recordHelper.PullTransport
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.io.RandomAccessFile

/**
 * Wav格式的音频记录器
 */
class WavRecordHelper
/**
 * 构造方法
 *
 * @param file          保存录音的文件
 * @param config        录音参数配置
 * @param pullTransport 数据推送器
 */
@RequiresPermission(Manifest.permission.RECORD_AUDIO) constructor(
    file: File,
    config: AudioEncodeConfig,
    pullTransport: PullTransport
) : BaseAudioRecordHelper(
    file, config, pullTransport
) {
    override fun stopRecording() {
        try {
            super.stopRecording()
            writeWavHeader()
        } catch (e: IOException) {
            throw RuntimeException("Error in applying wav header", e)
        }
    }

    /**
     * 写入wav文件头
     */
    @Throws(IOException::class)
    private fun writeWavHeader() {
        val wavFile = randomAccessFile(file)
        wavFile.seek(0) // to the beginning
        wavFile.write(WavHeader(config, file.length()).toBytes())
        wavFile.close()
    }

    private fun randomAccessFile(file: File): RandomAccessFile {
        val randomAccessFile: RandomAccessFile = try {
            RandomAccessFile(file, "rw")
        } catch (e: FileNotFoundException) {
            throw RuntimeException(e)
        }
        return randomAccessFile
    }
}