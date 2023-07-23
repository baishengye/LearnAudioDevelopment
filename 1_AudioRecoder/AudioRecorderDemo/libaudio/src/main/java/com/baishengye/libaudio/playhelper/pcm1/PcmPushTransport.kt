package com.baishengye.libaudio.playhelper.pcm1

import android.media.AudioTrack
import android.os.Handler
import android.os.Looper
import com.baishengye.libaudio.config.AudioChunk
import com.baishengye.libaudio.playhelper.pcm1.PcmPushTransport.OnAudioChunkPushedListener
import java.io.IOException
import java.io.InputStream

/**
 * 录音数据 拉取 运输机
 *
 * 此类表示「录音机」和「输出文件」之间的总线。
 * 基本上它只是从[InputStream]中提取数据，并将其传输到[AudioTrack]，以写入输出文件。
 * 可以对每次音频数据拉取过程进行监听[OnAudioChunkPushedListener]
 */
interface PcmPushTransport {
    /**
     * 是否开启推送
     *
     * @param enabledToBePushed 是否开启推送
     */
    fun isEnableToBePushed(enabledToBePushed: Boolean)

    /**
     * 开始读取文件读取推送数据
     *
     * @param audioTrack 音频播放工具[AudioTrack]
     * @param pushSizeInBytes 缓存字节数
     * @param inputStream 输出流[InputStream]
     * @param postPushEndEvent 文件读取结束
     */
    @Throws(IOException::class)
    fun startPoolingAndReading(
        audioTrack: AudioTrack,
        pushSizeInBytes: Int,
        inputStream: InputStream,
        postPushEndEvent: (Boolean) -> Unit
    )

    /**
     * 设置【音频数据块】拉取监听器
     */
    interface OnAudioChunkPushedListener {
        /**
         * 拉取 音频原始数据
         *
         * @param audioChunk 音频数据块
         */
        fun onAudioChunkPushed(audioChunk: AudioChunk?)
    }

    abstract class AbstractPushTransport internal constructor() : PcmPushTransport {
        @Volatile
        var push = false
        var onAudioChunkPushedListener: OnAudioChunkPushedListener? = null
        var handler = Handler(Looper.getMainLooper())

        override fun isEnableToBePushed(enabledToBePushed: Boolean) {
            push = enabledToBePushed
        }

        /**
         * 推送 音频原始数据块
         */
        fun postPushDataEvent(audioChunk: AudioChunk?) {
            if (onAudioChunkPushedListener != null) {
                handler.post { onAudioChunkPushedListener!!.onAudioChunkPushed(audioChunk) }
            }
        }
    }

    class Default : AbstractPushTransport() {
        /**
         * 音频数据推送监听，不间断的回调录音数据。
         */
        fun setOnAudioChunkPushedListener(onAudioChunkPushedListener: OnAudioChunkPushedListener?): Default {
            this.onAudioChunkPushedListener = onAudioChunkPushedListener
            return this
        }

        @Throws(IOException::class)
        override fun startPoolingAndReading(
            audioTrack: AudioTrack,
            pushSizeInBytes: Int,
            inputStream: InputStream,
            postPushEndEvent: (Boolean) -> Unit
        ) {
            val audioChunk = AudioChunk.Bytes(ByteArray(pushSizeInBytes))
            audioTrack.play()
            while (push) {
                var readSize = -1
                try {
                    readSize = inputStream.read(audioChunk.toBytes())
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                if (readSize <= 0) {
                    postPushEndEvent(true)
                    continue
                }
                postPushDataEvent(audioChunk)
                audioTrack.write(audioChunk.toBytes(), 0, readSize)
            }
        }
    }
}