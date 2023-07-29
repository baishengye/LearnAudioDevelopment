package com.baishengye.liblame.record

import android.media.AudioRecord
import android.os.Handler
import android.os.Looper
import com.baishengye.liblame.LameBuilder
import com.baishengye.liblame.record.PullTransport.OnAudioChunkPulledListener
import com.baishengye.liblame.record.pcm.PcmEncodeConfig
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
        fun postPullDataEvent(audioChunk: AudioChunk?) {
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
            outputStream: OutputStream,
            postPullEndEvent: (Boolean) -> Unit
        ) {
            val audioChunk: AudioChunk =
                AudioChunk.Bytes(ByteArray(pullSizeInBytes), pullSizeInBytes)
            while (pull) {
                val count = audioRecord.read(audioChunk.toBytes(), 0, pullSizeInBytes)
                if (AudioRecord.ERROR_INVALID_OPERATION != count && AudioRecord.ERROR_BAD_VALUE != count) {
                    postPullDataEvent(audioChunk) // 推送原始音频数据块
                    outputStream.write(audioChunk.toBytes()) // 将数据写入文件
                } else {
                    postPullEndEvent.invoke(true)
                }
            }
        }
    }

    class Mp3PullTransport(private val config: PcmEncodeConfig) : AbstractPullTransport() {
        val encode: MP3EncodeThread = MP3EncodeThread("MP3EncodeThread")

        override fun startPoolingAndWriting(
            audioRecord: AudioRecord,
            pullSizeInBytes: Int,
            outputStream: OutputStream,
            postPullEndEvent: (Boolean) -> Unit
        ) {
            LameBuilder()
                .setInSampleRate(config.sampleRateInHz)
                .setOutChannels(config.channels())
                .setOutSampleRate(config.sampleRateInHz)
                .setOutBitrate(128)
                .setQuality(7).initLame()

//            LameLoader.initLess(config.sampleRateInHz,config.channels(),config.sampleRateInHz,128,7)

            encode.setOutputStream(outputStream)
            encode.setBufferSize(pullSizeInBytes)
            encode.start()

            audioRecord.setRecordPositionUpdateListener(encode, encode.stopHandler)
            audioRecord.positionNotificationPeriod = config.simplePreNotify

            val pcmData = ShortArray(pullSizeInBytes)

            while (pull) {
                val count = audioRecord.read(pcmData, 0, pullSizeInBytes)
                if (AudioRecord.ERROR_INVALID_OPERATION != count && AudioRecord.ERROR_BAD_VALUE != count) {
                    val audioChunk = AudioChunk.Shorts(pcmData, count)
                    encode.addData(audioChunk)
                    postPullDataEvent(audioChunk) // 推送原始音频数据块
                } else {
                    encode.stopThread()
                    postPullEndEvent.invoke(true)
                }
            }
        }

    }
}