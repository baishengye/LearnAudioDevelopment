package com.baishengye.libaudio.recordHelper.meidacodec.acc

import android.Manifest
import android.annotation.SuppressLint
import android.media.AudioRecord
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import androidx.annotation.RequiresPermission
import com.baishengye.libaudio.config.encode.MediaRecordException
import com.baishengye.libaudio.config.encode.MediaRecordState
import com.baishengye.libaudio.recordHelper.RecordHelper
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.concurrent.Executors


/**
 * 录音机基类
 */
open class AccAudioRecordHelperWithMediaCodec @RequiresPermission(Manifest.permission.RECORD_AUDIO) constructor(
    protected val file: File,
    protected val config: MediaCodecEncodeConfig,
    protected val pullTransport: AccPullTransportWithMediaCodec
) : RecordHelper {

    protected var bufferSizeInBytes: Int = 0 // 缓冲区大小
    protected var recordState: MediaRecordState = MediaRecordState.IDLE

    private var audioRecord: AudioRecord? = null
    private var mediaCodec: MediaCodec? = null
    private var outputStream: OutputStream? = null

    private val executorService = Executors.newSingleThreadExecutor()

    @SuppressLint("MissingPermission")
    fun initAudioRecord() {
        if (audioRecord == null) {
            // 计算缓冲区大小
            bufferSizeInBytes = AudioRecord.getMinBufferSize(
                config.sampleRateInHz,//采样率
                config.audioEncodingFormat.channelConfig,//通道
                config.audioEncodingFormat.audioFormat//音频编码
            )

            var simpleSize =
                (bufferSizeInBytes / config.audioEncodingFormat.bytesPreSimple)//1700/2=850
            if (simpleSize % config.simplePreNotify != 0) {//850/160=5余50
                simpleSize += (config.simplePreNotify - simpleSize % config.simplePreNotify)//850+160-50=960，960/160=6,可以除尽
                bufferSizeInBytes = simpleSize * config.audioEncodingFormat.bytesPreSimple//
            }

            audioRecord = AudioRecord(
                config.audioSource,
                config.sampleRateInHz,
                config.audioEncodingFormat.channelConfig,
                config.audioEncodingFormat.audioFormat,
                bufferSizeInBytes
            )

            if (audioRecord!!.state != AudioRecord.STATE_INITIALIZED) {
                throw IllegalStateException(
                    "AudioRecord 初始化失败，请检查是否有RECORD_AUDIO权限。" +
                            "或者使用了系统APP才能用的配置项（MediaRecorder.AudioSource.REMOTE_SUBMIX 等），" +
                            "或者使用了该设备不支持的配置项。"
                )
            }
        }

        if (recordState == MediaRecordState.STOP) {
            recordState = MediaRecordState.IDLE
        }
    }

    private fun initMediaCodec() {
        if (mediaCodec == null) {
            val format = MediaFormat.createAudioFormat(
                config.mime, config.sampleRateInHz, config.channels()
            )
            format.setInteger(MediaFormat.KEY_BIT_RATE, config.bitRates())
            format.setInteger(
                MediaFormat.KEY_AAC_PROFILE,
                MediaCodecInfo.CodecProfileLevel.AACObjectLC
            )
            format.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, bufferSizeInBytes * 2)
            try {
                mediaCodec = MediaCodec.createEncoderByType(config.mime)
                mediaCodec!!.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
            } catch (e: IOException) {
                e.printStackTrace()
                mediaCodec = null
            }
        }
    }

    override fun startRecording() {
        initAudioRecord()
        initMediaCodec()

        if (recordState != MediaRecordState.IDLE) {
            throw MediaRecordException("状态异常，此时录音机状态为:${recordState}")
        }
        recordState = MediaRecordState.RECORDING

        executorService.submit { notifyState() }
    }

    override fun resumeRecording() {
        initAudioRecord()
        initMediaCodec()

        if (recordState != MediaRecordState.PAUSE) {
            throw MediaRecordException("状态异常，此时录音机状态为:${recordState}")
        }
        recordState = MediaRecordState.RECORDING

        executorService.submit { notifyState() }
    }

    private fun startRecord() {
        try {
            if (outputStream == null) {
                outputStream = FileOutputStream(file)
            }

            val buffInfo: MediaCodec.BufferInfo = MediaCodec.BufferInfo()
            audioRecord!!.startRecording()
            mediaCodec!!.start()
            pullTransport.isEnableToBePulled(true)
            pullTransport.startPoolingAndWriting(
                (audioRecord)!!,
                mediaCodec!!,
                buffInfo,
                bufferSizeInBytes,
                outputStream!!
            ) {
                if (it) {
                    stopRecording()
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun pauseRecording() {
        pullTransport.isEnableToBePulled(false)

        if (recordState != MediaRecordState.RECORDING) {
            throw MediaRecordException("状态异常，此时录音机状态为:${recordState}")
        }
        recordState = MediaRecordState.PAUSE

        notifyState()
    }

    override fun stopRecording() {
        pullTransport.isEnableToBePulled(false)

        if (recordState == MediaRecordState.STOP || recordState == MediaRecordState.IDLE) {
            throw MediaRecordException("状态异常，此时录音机状态为:${recordState}")
        }
        recordState = MediaRecordState.STOP

        notifyState()
    }

    private fun notifyState() {
        when (recordState) {
            MediaRecordState.RECORDING -> startRecord()
            MediaRecordState.STOP -> releaseResource()
            MediaRecordState.PAUSE -> stopAudioRecord()
            else -> {}
        }
        //todo 录音状态监听
    }

    private fun releaseResource() {
        if (audioRecord != null) {
            audioRecord!!.stop()
            audioRecord!!.release()
            audioRecord = null
        }
        if (mediaCodec != null) {
            mediaCodec!!.stop()
            mediaCodec!!.release()
            mediaCodec = null
        }
        if (outputStream != null) {
            try {
                outputStream!!.flush()
                outputStream!!.close()
                outputStream = null
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun stopAudioRecord() {
        if (audioRecord != null) {
            audioRecord!!.stop()
        }
        if (mediaCodec != null) {
            mediaCodec!!.stop()
        }
    }
}