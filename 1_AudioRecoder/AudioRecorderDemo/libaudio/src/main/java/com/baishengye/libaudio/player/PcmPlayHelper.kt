package com.baishengye.libaudio.player

import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.IOException

/**
 * 使用AudioTask录制PCM音频*/
class PcmPlayHelper(private val playerStop: (Boolean) -> Unit):PlayHelper {
    private var playThread: PlayThread? = null
    private var handler:Handler = Handler(Looper.getMainLooper())
    private var filePath: String ?= null

    private class PlayThread(
        private val path: String?,

        /**
         * 音频流格式（一般使用music）
         */
        private var streamType: Int,

        /**
         * 采样率
         */
        private val sampleRateInHz: Int,

        /**
         * 声道设置
         */
        private val channelConfig: Int,

        /**
         * 编码格式
         */
        private val audioFormat: Int,
        /**
         * 播放模式（一般使用流模式）
         */
        private val mod: Int,

        /**
         * 音频自动停止回调*/
        val playerStop:(Boolean)->Unit
    ) : Thread() {
        /**
         * pcm播放组件
         */
        private var audioTrack: AudioTrack? = null

        /**
         * 文件输入
         */
        private var fileInputStream: FileInputStream? = null

        /**
         * 音频缓存大小
         */
        private var bufferSizeInBytes = 0

        /**
         * 是否停止播放
         */
        private var isStopPlay = false

        override fun run() {
            super.run()
            initIo()
            initAudioTrack()
            play()
        }

        /**
         * 初始化IO
         */
        private fun initIo() {
            if (TextUtils.isEmpty(path)) {
                return
            }
            fileInputStream = try {
                FileInputStream(path)
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
                null
            }
        }

        /**
         * 初始化pcm播放组件
         */
        private fun initAudioTrack() {
            bufferSizeInBytes =
                AudioTrack.getMinBufferSize(sampleRateInHz, channelConfig, audioFormat)
            audioTrack = AudioTrack(
                streamType,
                sampleRateInHz,
                channelConfig,
                audioFormat,
                bufferSizeInBytes,
                mod
            )
        }

        /**
         * 开始播放
         */
private fun play() {
    if (audioTrack == null || fileInputStream == null) {
        return
    }
    val data = ByteArray(bufferSizeInBytes)
    audioTrack!!.play()
    while (true) {
        if (isStopPlay) {
            release()
            break
        }
        var readSize = -1
        try {
            readSize = fileInputStream!!.read(data)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        if (readSize <= 0) {
            isStopPlay = true
            playerStop.invoke(true)
            continue
        }
        audioTrack!!.write(data, 0, readSize)
    }
}

        /**
         * 停止播放
         */
        fun stopPlay() {
            isStopPlay = true
            try {
                join(2000)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }

        /**
         * 释放资源
         */
        private fun release() {
            if (audioTrack != null) {
                audioTrack!!.stop()
                audioTrack!!.release()
                audioTrack = null
            }
            if (fileInputStream != null) {
                try {
                    fileInputStream!!.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun startPlaying(path: String) {
        this.filePath = path
        stopPlaying()

        playThread = PlayThread(
            path,
            AudioManager.STREAM_MUSIC,
            44100,
            AudioFormat.CHANNEL_OUT_STEREO,
            AudioFormat.ENCODING_PCM_16BIT,
            AudioTrack.MODE_STREAM,
            this::playerStopPerform
        )
        playThread!!.start()
    }

    fun playerStopPerform(isStop:Boolean){
        handler.post {
            playerStop.invoke(isStop)
        }
    }

    override fun pausePlaying() {
    }

    override fun resumePlaying() {
    }

    override fun stopPlaying() {
        if (playThread == null) {
            return
        }
        playThread!!.stopPlay()
        playThread = null
    }
}