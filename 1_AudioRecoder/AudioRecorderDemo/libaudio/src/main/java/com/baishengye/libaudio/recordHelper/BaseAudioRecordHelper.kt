package com.baishengye.libaudio.recordHelper

import android.Manifest
import android.annotation.SuppressLint
import android.media.AudioRecord
import androidx.annotation.RequiresPermission
import com.baishengye.libaudio.config.encode.AudioEncodeConfig
import com.baishengye.libaudio.config.encode.MediaRecordException
import com.baishengye.libaudio.config.encode.MediaRecordState
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.concurrent.Executors


/**
 * 录音机基类
 */
open class BaseAudioRecordHelper @RequiresPermission(Manifest.permission.RECORD_AUDIO) protected constructor(
    protected val file: File,
    protected val config: AudioEncodeConfig,
    protected val pullTransport: PullTransport
) : RecordHelper {

    protected var bufferSizeInBytes: Int = 0 // 缓冲区大小
    protected var recordState: MediaRecordState = MediaRecordState.IDLE

    private var audioRecord: AudioRecord? = null
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

            //根据采样数计算新的缓冲区,使缓冲区大小除以每个样本所占字节数为正数，并每帧的样本数整除
            //假设：buffSizeInBytes = 1700
            //config.audioEncodingFormat.bytesPreSimple = 2
            //config.simplePreNotify = 160
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

        if(recordState == MediaRecordState.STOP){
            recordState = MediaRecordState.IDLE
        }
    }

    override fun startRecording() {
        initAudioRecord()

        if(recordState != MediaRecordState.IDLE){
            throw MediaRecordException("状态异常，此时录音机状态为:${recordState}")
        }
        recordState = MediaRecordState.RECORDING

        executorService.submit { notifyState() }
    }

    override fun resumeRecording() {
        initAudioRecord()

        if(recordState != MediaRecordState.PAUSE){
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
            audioRecord!!.startRecording()
            pullTransport.isEnableToBePulled(true)
            pullTransport.startPoolingAndWriting((audioRecord)!!, bufferSizeInBytes, outputStream!!)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun pauseRecording() {
        pullTransport.isEnableToBePulled(false)

        if(recordState != MediaRecordState.RECORDING){
            throw MediaRecordException("状态异常，此时录音机状态为:${recordState}")
        }
        recordState = MediaRecordState.PAUSE

        notifyState()
    }

    override fun stopRecording() {
        pullTransport.isEnableToBePulled(false)

        if(recordState == MediaRecordState.STOP || recordState == MediaRecordState.IDLE){
            throw MediaRecordException("状态异常，此时录音机状态为:${recordState}")
        }
        recordState = MediaRecordState.STOP

        notifyState()
    }

    private fun notifyState(){
        when(recordState){
            MediaRecordState.RECORDING -> startRecord()
            MediaRecordState.STOP -> releaseResource()
            MediaRecordState.PAUSE -> stopAudioRecord()
            else -> {}
        }
        //todo 录音状态监听
    }

    private fun releaseResource(){
        if (audioRecord != null) {
            audioRecord!!.stop()
            audioRecord!!.release()
            audioRecord = null
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

    private fun stopAudioRecord(){
        if (audioRecord != null) {
            audioRecord!!.stop()
        }
    }
}