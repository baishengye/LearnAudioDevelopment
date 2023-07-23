package com.baishengye.libaudio.playhelper.mediacodec.acc

import android.media.AudioTrack
import android.media.MediaCodec
import android.media.MediaExtractor
import android.os.Handler
import android.os.Looper
import com.baishengye.libaudio.config.AudioChunk
import com.baishengye.libaudio.playhelper.mediacodec.acc.AccPushTransportWithMediaCodec.OnAudioChunkPushedListener
import java.io.IOException
import java.io.InputStream
import java.lang.Thread.sleep
import java.nio.ByteBuffer


/**
 * 录音数据 拉取 运输机
 *
 * 此类表示「录音机」和「输出文件」之间的总线。
 * 基本上它只是从[InputStream]中提取数据，并将其传输到[AudioTrack]，以写入输出文件。
 * 可以对每次音频数据拉取过程进行监听[OnAudioChunkPushedListener]
 */
interface AccPushTransportWithMediaCodec {
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
        mediaCodec: MediaCodec,
        mediaExtractor: MediaExtractor,
        startMs: Long,
        info: MediaCodec.BufferInfo,
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

    abstract class AbstractPushTransport internal constructor() : AccPushTransportWithMediaCodec {
        companion object {
            const val TIME_OUT = 2000L
        }

        @Volatile
        var push = false
        var onAudioChunkPushedListener: OnAudioChunkPushedListener? = null
        var handler = Handler(Looper.getMainLooper())
        var config: MediaCodecDecodeConfig = MediaCodecDecodeConfig()

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

        /**
         * 音频数据推送监听，不间断的回调录音数据。
         */
        fun setMediaCodecEncodeConfig(mediaCodecDecodeConfig: MediaCodecDecodeConfig): AccPushTransportWithMediaCodec.Default {
            this.config = mediaCodecDecodeConfig
            return this
        }

        @Throws(IOException::class)
        override fun startPoolingAndReading(
            audioTrack: AudioTrack,
            mediaCodec: MediaCodec,
            mediaExtractor: MediaExtractor,
            startMs: Long,
            info: MediaCodec.BufferInfo,
            pushSizeInBytes: Int,
            inputStream: InputStream,
            postPushEndEvent: (Boolean) -> Unit
        ) {
            val audioChunk = AudioChunk.Bytes(ByteArray(pushSizeInBytes))
            while (push) {
                val inputBufferId: Int = mediaCodec.dequeueInputBuffer(TIME_OUT)
                if (inputBufferId >= 0) {
                    val inputBuffer: ByteBuffer? = mediaCodec.getInputBuffer(inputBufferId)
                    var readSize = -1
                    if (inputBuffer != null) {
                        readSize = mediaExtractor.readSampleData(inputBuffer, 0)
                    }
                    if (readSize <= 0) {
                        mediaCodec.queueInputBuffer(
                            inputBufferId,
                            0,
                            0,
                            0,
                            MediaCodec.BUFFER_FLAG_END_OF_STREAM
                        )
                        postPushEndEvent.invoke(true)
                    } else {
                        mediaCodec.queueInputBuffer(
                            inputBufferId,
                            0,
                            readSize,
                            mediaExtractor.sampleTime,
                            0
                        )
                        mediaExtractor.advance()
                    }
                }

                val outputBufferId: Int = mediaCodec.dequeueOutputBuffer(info, TIME_OUT)
                if (outputBufferId >= 0) {
                    val outputBuffer: ByteBuffer? = mediaCodec.getOutputBuffer(outputBufferId)
                    if (outputBuffer != null && info.size > 0) {
                        while (info.presentationTimeUs / 1000 > System.currentTimeMillis() - startMs) {
                            try {
                                sleep(10)
                            } catch (e: InterruptedException) {
                                e.printStackTrace()
                                break
                            }
                        }
                        val data = ByteArray(info.size)
                        outputBuffer.get(data)
                        outputBuffer.clear()
                        audioTrack.write(data, 0, info.size)
                    }
                    mediaCodec.releaseOutputBuffer(outputBufferId, false)
                }
            }
        }
    }
}