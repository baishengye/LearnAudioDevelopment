package com.baishengye.liblame

import java.io.IOException
import java.io.InputStream

object LameLoader {

    private const val MP3_BUFFER_SIZE = 1024
    const val LAME_PRESET_DEFAULT = 0
    const val LAME_PRESET_MEDIUM = 1
    const val LAME_PRESET_STANDARD = 2
    const val LAME_PRESET_EXTREME = 3

    external fun getLameVersion(): String

    external fun initializeEncoder(
        sampleRate: Int,
        numChannels: Int,
        outSampleRate: Int,
        outBitrate: Int,
        quality: Int,
    ): Int

    external fun setEncoderPreset(preset: Int)

    external fun encode(
        leftChannel: ShortArray?,
        rightChannel: ShortArray?, channelSamples: Int, mp3Buffer: ByteArray?,
        bufferSize: Int
    ): Int

    external fun flushEncoder(mp3Buffer: ByteArray?, bufferSize: Int): Int

    external fun closeEncoder(): Int

    external fun wav2mp3(wavPath: String, mp3Path: String)


    external fun wav2mp3Speed(wavPath: String, mp3Path: String, speed: Int)

    /***********************************************下面解码相关*********************************************************************/

    external fun initializeDecoder(): Int

    external fun getDecoderSampleRate(): Int

    external fun getDecoderChannels(): Int

    external fun getDecoderDelay(): Int

    external fun getDecoderPadding(): Int

    external fun getDecoderTotalFrames(): Int

    external fun getDecoderFrameSize(): Int

    external fun getDecoderBitrate(): Int

    @Throws(IOException::class)
    fun configureDecoder(input: InputStream): Int {
        var size = 100
        val id3Length: Long
        val aidLength: Long
        val buf = ByteArray(size)
        if (input.read(buf, 0, 4) != 4) {
            return -1
        }
        if (isId3Header(buf)) {
            // ID3 header found, skip past it
            if (input.read(buf, 0, 6) != 6) {
                return -1
            }
            buf[2] = (buf[2].toInt() and 0x7F).toByte()
            buf[3] = (buf[3].toInt() and 0x7F).toByte()
            buf[4] = (buf[4].toInt() and 0x7F).toByte()
            buf[5] = (buf[5].toInt() and 0x7F).toByte()
            id3Length = ((((buf[2].toInt() shl 7) + buf[3] shl 7) + buf[4] shl 7) + buf[5]).toLong()
            input.skip(id3Length)
            if (input.read(buf, 0, 4) != 4) {
                return -1
            }
        }
        if (isAidHeader(buf)) {
            // AID header found, skip past it too
            if (input.read(buf, 0, 2) != 2) {
                return -1
            }
            aidLength = (buf[0] + 256 * buf[1]).toLong()
            input.skip(aidLength)
            if (input.read(buf, 0, 4) != 4) {
                return -1
            }
        }
        while (!isMp123SyncWord(buf)) {
            // search for MP3 syncword one byte at a time
            for (i in 0..2) {
                buf[i] = buf[i + 1]
            }
            val `val`: Int = input.read()
            if (`val` == -1) {
                return -1
            }
            buf[3] = `val`.toByte()
        }
        do {
            size = input.read(buf)
            if (nativeConfigureDecoder(buf, size) == 0) {
                return 0
            }
        } while (size > 0)
        return -1
    }

    private fun isId3Header(buf: ByteArray): Boolean {
        return buf[0] == 'I'.code.toByte() && buf[1] == 'D'.code.toByte() && buf[2] == '3'.code.toByte()
    }

    private fun isAidHeader(buf: ByteArray): Boolean {
        return buf[0] == 'A'.code.toByte() && buf[1] == 'i'.code.toByte() && buf[2] == 'D'.code.toByte() && buf[3] == '\u0001'.code.toByte()
    }

    private fun isMp123SyncWord(buf: ByteArray): Boolean {
        val abl2 = charArrayOf(
            0.toChar(),
            7.toChar(),
            7.toChar(),
            7.toChar(),
            0.toChar(),
            7.toChar(),
            0.toChar(),
            0.toChar(),
            0.toChar(),
            0.toChar(),
            0.toChar(),
            8.toChar(),
            8.toChar(),
            8.toChar(),
            8.toChar(),
            8.toChar()
        )
        if (buf[0].toInt() and 0xFF != 0xFF) {
            return false
        }
        if (buf[1].toInt() and 0xE0 != 0xE0) {
            return false
        }
        if (buf[1].toInt() and 0x18 == 0x08) {
            return false
        }
        if (buf[1].toInt() and 0x06 == 0x00) {
            // not layer I/II/III
            return false
        }
        if (buf[2].toInt() and 0xF0 == 0xF0) {
            // bad bitrate
            return false
        }
        if (buf[2].toInt() and 0x0C == 0x0C) {
            // bad sample frequency
            return false
        }
        if ((buf[1].toInt() and 0x18 == 0x18) && (buf[1].toInt() and 0x06 == 0x04) && (abl2[buf[2].toInt() shr 4].code and (1 shl (buf[3].toInt() shr 6)) != 0)) {
            return false
        }
        return buf[3].toInt() and 0x03 != 2
    }

    private external fun nativeConfigureDecoder(inputBuffer: ByteArray, bufferSize: Int): Int

    @Throws(IOException::class)
    fun decodeFrame(
        input: InputStream,
        pcmLeft: ShortArray, pcmRight: ShortArray
    ): Int {
        var len = 0
        var samplesRead = 0
        val buf = ByteArray(MP3_BUFFER_SIZE)

        // check for buffered data
        samplesRead = nativeDecodeFrame(buf, len, pcmLeft, pcmRight)
        if (samplesRead != 0) {
            return samplesRead
        }
        while (true) {
            len = input.read(buf)
            if (len == -1) {
                // finished reading input buffer, check for buffered data
                samplesRead = nativeDecodeFrame(buf, len, pcmLeft, pcmRight)
                break
            }
            samplesRead = nativeDecodeFrame(buf, len, pcmLeft, pcmRight)
            if (samplesRead > 0) {
                break
            }
        }
        return samplesRead
    }

    private external fun nativeDecodeFrame(
        inputBuffer: ByteArray, bufferSize: Int,
        pcmLeft: ShortArray, pcmRight: ShortArray
    ): Int

    external fun closeDecoder(): Int
}