package com.baishengye.libaudio.config

import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.abs
import kotlin.math.log10

/**
 * 音频数据包装器
 */
interface AudioChunk {
    // 获取最大峰值(振幅)
    fun maxAmplitude(): Double

    // 获取byte类型数据
    fun toBytes(): ByteArray

    // 获取short类型数据
    fun toShorts(): ShortArray
    abstract class AbstractAudioChunk : AudioChunk {
        override fun maxAmplitude(): Double {
            var nMaxAmp = 0
            for (sh in toShorts()) {
                if (sh > nMaxAmp) {
                    nMaxAmp = sh.toInt()
                }
            }
            return if (nMaxAmp > 0) {
                abs(20 * log10(nMaxAmp / REFERENCE))
            } else {
                0.0
            }
        }

        companion object {
            private const val REFERENCE = 0.6
        }
    }

    /**
     * byte类型数据包装器
     */
    class Bytes(private val bytes: ByteArray) : AbstractAudioChunk() {
        override fun toBytes(): ByteArray {
            return bytes
        }

        override fun toShorts(): ShortArray {
            val shorts = ShortArray(bytes.size / 2)
            ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer()[shorts]
            return shorts
        }
    }
}