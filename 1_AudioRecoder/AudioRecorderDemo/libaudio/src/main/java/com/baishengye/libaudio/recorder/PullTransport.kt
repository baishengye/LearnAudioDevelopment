package com.baishengye.libaudio.recorder

import android.media.AudioRecord
import android.os.Handler
import android.os.Looper
import com.baishengye.libaudio.recorder.PullTransport.OnAudioChunkPulledListener
import java.io.IOException
import java.io.OutputStream

/**
 * 录音数据 拉取 运输机
 *
 * 此类表示「录音机」和「输出文件」之间的总线。
 * 基本上它只是从[AudioRecord]中提取数据，并将其传输到[OutputStream]，以写入输出文件。
 * 可以对每次音频数据拉取过程进行监听[OnAudioChunkPulledListener]
 */
interface PullTransport {
    /**
     * 是否开启拉取
     *
     * @param enabledToBePulled 是否开启拉取
     */
    fun isEnableToBePulled(enabledToBePulled: Boolean)

    /**
     * 开始推送数据和写入文件
     *
     * @param audioRecord 音频录制工具[AudioRecord]
     * @param pullSizeInBytes 缓存字节数
     * @param outputStream 输出流[OutputStream]
     */
    @Throws(IOException::class)
    fun startPoolingAndWriting(
        audioRecord: AudioRecord,
        pullSizeInBytes: Int,
        outputStream: OutputStream
    )

    /**
     * 设置【音频数据块】拉取监听器
     */
    interface OnAudioChunkPulledListener {
        /**
         * 拉取 音频原始数据
         *
         * @param audioChunk 音频数据块
         */
        fun onAudioChunkPulled(audioChunk: AudioChunk?)
    }

    abstract class AbstractPullTransport internal constructor() : PullTransport {
        @Volatile
        var pull = false
        var onAudioChunkPulledListener: OnAudioChunkPulledListener? = null
        var handler = Handler(Looper.getMainLooper())

        override fun isEnableToBePulled(enabledToBePulled: Boolean) {
            pull = enabledToBePulled
        }

        /**
         * 推送 音频原始数据块
         */
        fun postPullEvent(audioChunk: AudioChunk?) {
            if (onAudioChunkPulledListener != null) {
                handler.post { onAudioChunkPulledListener!!.onAudioChunkPulled(audioChunk) }
            }
        }
    }

    class Default : AbstractPullTransport() {
        /**
         * 音频数据推送监听，不间断的回调录音数据。
         */
        fun setOnAudioChunkPulledListener(onAudioChunkPulledListener: OnAudioChunkPulledListener?): Default {
            this.onAudioChunkPulledListener = onAudioChunkPulledListener
            return this
        }

        @Throws(IOException::class)
        override fun startPoolingAndWriting(
            audioRecord: AudioRecord,
            pullSizeInBytes: Int,
            outputStream: OutputStream
        ) {
            val audioChunk: AudioChunk = AudioChunk.Bytes(ByteArray(pullSizeInBytes))
            while (pull) {
                val count = audioRecord.read(audioChunk.toBytes(), 0, pullSizeInBytes)
                if (AudioRecord.ERROR_INVALID_OPERATION != count && AudioRecord.ERROR_BAD_VALUE != count) {
                    postPullEvent(audioChunk) // 推送原始音频数据块
                    outputStream.write(audioChunk.toBytes()) // 将数据写入文件
                }
            }
        }
    }
}