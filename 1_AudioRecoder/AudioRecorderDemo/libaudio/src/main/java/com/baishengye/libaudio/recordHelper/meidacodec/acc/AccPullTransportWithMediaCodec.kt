package com.baishengye.libaudio.recordHelper.meidacodec.acc

import android.media.AudioRecord
import android.media.MediaCodec
import android.os.Handler
import android.os.Looper
import com.baishengye.libaudio.config.AudioChunk
import com.baishengye.libaudio.recordHelper.meidacodec.acc.AccPullTransportWithMediaCodec.OnAudioChunkPulledListener
import java.io.IOException
import java.io.OutputStream
import java.nio.ByteBuffer

/**
 * 录音数据 拉取 运输机
 *
 * 此类表示「录音机」和「输出文件」之间的总线。
 * 基本上它只是从[AudioRecord]中提取数据，并将其传输到[OutputStream]，以写入输出文件。
 * 可以对每次音频数据拉取过程进行监听[OnAudioChunkPulledListener]
 */
interface AccPullTransportWithMediaCodec {

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
        mediaCodec: MediaCodec,
        info: MediaCodec.BufferInfo,
        pullSizeInBytes: Int,
        outputStream: OutputStream,
        postPullEndEvent: (Boolean) -> Unit
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

    abstract class AbstractPullTransport internal constructor() : AccPullTransportWithMediaCodec {
        @Volatile
        var pull = false
        var onAudioChunkPulledListener: OnAudioChunkPulledListener? = null
        var config: MediaCodecEncodeConfig = MediaCodecEncodeConfig()
        var handler = Handler(Looper.getMainLooper())

        override fun isEnableToBePulled(enabledToBePulled: Boolean) {
            pull = enabledToBePulled
        }

        /**
         * 推送 音频原始数据块
         */
        fun postPullDataEvent(audioChunk: AudioChunk?) {
            if (onAudioChunkPulledListener != null) {
                handler.post { onAudioChunkPulledListener!!.onAudioChunkPulled(audioChunk) }
            }
        }
    }

    class Default : AbstractPullTransport() {
        companion object {
            const val TIME_OUT = 2000L
        }

        /**
         * 音频数据推送监听，不间断的回调录音数据。
         */
        fun setOnAudioChunkPulledListener(onAudioChunkPulledListener: OnAudioChunkPulledListener): Default {
            this.onAudioChunkPulledListener = onAudioChunkPulledListener
            return this
        }

        /**
         * 音频数据推送监听，不间断的回调录音数据。
         */
        fun setMediaCodecEncodeConfig(mediaCodecEncodeConfig: MediaCodecEncodeConfig): Default {
            this.config = mediaCodecEncodeConfig
            return this
        }

        @Throws(IOException::class)
        override fun startPoolingAndWriting(
            audioRecord: AudioRecord,
            mediaCodec: MediaCodec,
            info: MediaCodec.BufferInfo,
            pullSizeInBytes: Int,
            outputStream: OutputStream,
            postPullEndEvent: (Boolean) -> Unit
        ) {
            val audioChunk: AudioChunk = AudioChunk.Bytes(ByteArray(pullSizeInBytes))
            while (pull) {
                val inputBufferId = mediaCodec.dequeueInputBuffer(TIME_OUT)
                if (inputBufferId >= 0) {
                    val inputBuffer: ByteBuffer? = mediaCodec.getInputBuffer(inputBufferId)
                    var readSize = -1
                    inputBuffer?.let {
                        readSize = audioRecord.read(inputBuffer, pullSizeInBytes)
                        inputBuffer.get(audioChunk.toBytes())
                    }
                    if (AudioRecord.ERROR_INVALID_OPERATION != readSize && AudioRecord.ERROR_BAD_VALUE != readSize) {
                        postPullDataEvent(audioChunk) // 推送原始音频数据块
                        mediaCodec.queueInputBuffer(
                            inputBufferId,
                            0,
                            readSize,
                            System.nanoTime() / 1000,
                            0
                        );
                    } else {
                        mediaCodec.queueInputBuffer(
                            inputBufferId,
                            0,
                            0,
                            0,
                            MediaCodec.BUFFER_FLAG_END_OF_STREAM
                        );
                        postPullEndEvent.invoke(true)
                    }
                }

                val outputBufferId = mediaCodec.dequeueOutputBuffer(info, TIME_OUT)
                if (outputBufferId >= 0) {
                    val outputBuffer = mediaCodec.getOutputBuffer(outputBufferId)
                    val size = info.size
                    if (outputBuffer != null && size > 0) {
                        val data = ByteArray(size + 7)
                        addADTSHeader(data, size + 7)
                        outputBuffer[data, 7, size]
                        outputBuffer.clear()
                        try {
                            outputStream.write(data)
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                        mediaCodec.releaseOutputBuffer(outputBufferId, false)
                    }
                }
            }
        }

        /**
         * 添加AAC帧文件头
         *
         * @param packet    packet
         * @param packetLen packetLen
         */
        private fun addADTSHeader(packet: ByteArray, packetLen: Int) {
            val profile = 2 // AAC LC
            val freqIdx = ADTSHeader.sampleRateMap[config.sampleRateInHz] ?: 4 // 44.1kHz
            packet[0] = 0xFF.toByte()
            packet[1] = 0xF9.toByte()
            packet[2] = ((profile - 1 shl 6) + (freqIdx shl 2) + (config.channels() shr 2)).toByte()
            packet[3] = ((config.channels() and 3 shl 6) + (packetLen shr 11)).toByte()
            packet[4] = (packetLen and 0x7FF shr 3).toByte()
            packet[5] = ((packetLen and 7 shl 5) + 0x1F).toByte()
            packet[6] = 0xFC.toByte()
        }
    }
}