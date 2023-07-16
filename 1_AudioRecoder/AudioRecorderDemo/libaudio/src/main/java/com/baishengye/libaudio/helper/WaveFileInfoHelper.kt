package com.baishengye.libaudio.helper

import java.io.BufferedInputStream
import java.io.FileInputStream
import java.io.IOException

/**
 * wav文件 解析器
 */
class WaveFileInfoHelper(filename: String) {
    /**
     * 获取数据
     * 数据是一个二维数组，data.get(n).get(m)代表第n个声道的第m个采样值
     */
    var data: Array<IntArray>? = null
        private set

    /**
     * 获取数据长度，也就是一共采样多少个
     */
    var dataLen = 0
        private set

    /**
     * 获取声道个数，1代表单声道 2代表立体声
     */
    var numChannels = 0
        private set

    /**
     * 获取采样率
     */
    var sampleRate: Long = 0
        private set

    /**
     * 获取每个采样的编码长度，8bit或者16bit
     */
    var bitPerSample = 0
        private set
    private var fis: FileInputStream? = null
    private var bis: BufferedInputStream? = null

    /**
     * 判断是否创建wav读取器成功
     */
    var isSuccess = false
        private set

    init {
        initReader(filename)
    }

    private fun initReader(filename: String) {
        try {
            fis = FileInputStream(filename)
            bis = BufferedInputStream(fis)

            // --- RIFF区块 ---
            val riffFlag = readString(4)
            require("RIFF" == riffFlag) { "RIFF miss, $filename is not a wave file." }
            val chunkSize = readLong() // 文件数据长度: 该值 = fileSize - 8。
            val waveFlag = readString(4)
            require("WAVE" == waveFlag) { "WAVE miss, $filename is not a wave file." }

            // --- FORMAT区块 ---
            val fmtFlag = readString(4)
            require("fmt " == fmtFlag) { "fmt miss, $filename is not a wave file." }
            val subChunk1Size = readLong() // 格式块长度: 取决于编码格式，可以是 16、18、20、40 等
            val audioFormat = readInt() // AudioFormat(音频格式): 常见的 PCM 音频数据的值为1。
            numChannels = readInt() // NumChannels(声道数): 1：单声道，2：双声道/立体声
            sampleRate =
                readLong() // SampleRate(采样率): 每个声道单位时间采样次数。常用的采样频率有 11025, 22050 和 44100 kHz。
            val byteRate = readLong() // ByteRate(数据传输速率): 每秒数据字节数，该数值为:声道数×采样频率×采样位数/8。
            val blockAlign = readInt() // BlockAlign(数据块对齐): 采样帧大小。该数值为:声道数×采样位数/8。
            bitPerSample = readInt() // BitsPerSample(采样位数): 每个采样存储的bit数。常见的位数有 8、16

            // --- DATA区块 ---
            val dataFlag = readString(4)
            require("data" == dataFlag) { "data miss, $filename is not a wave file." }
            val audioLength = readLong() // 音频数据长度: N = ByteRate * seconds

            // 读取数据
            dataLen = (audioLength / (bitPerSample / 8) / numChannels).toInt()
            data = Array(numChannels) { IntArray(dataLen) }
            for (i in 0 until dataLen) {
                for (n in 0 until numChannels) {
                    if (bitPerSample == 8) {
                        data!![n][i] = bis!!.read()
                    } else if (bitPerSample == 16) {
                        data!![n][i] = readInt()
                    }
                }
            }
            isSuccess = true
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                if (bis != null) {
                    bis!!.close()
                }
                if (fis != null) {
                    fis!!.close()
                }
            } catch (e1: Exception) {
                e1.printStackTrace()
            }
        }
    }

    private fun readString(len: Int): String {
        val buf = ByteArray(len)
        try {
            if (bis!!.read(buf) != len) {
                throw IOException("no more data!!!")
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return String(buf)
    }

    private fun readInt(): Int {
        val buf = ByteArray(2)
        var res = 0
        try {
            if (bis!!.read(buf) != 2) {
                throw IOException("no more data!!!")
            }
            res = buf[0].toInt() and 0x000000FF or (buf[1].toInt() shl 8)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return res
    }

    private fun readLong(): Long {
        var res: Long = 0
        try {
            val l = LongArray(4)
            for (i in 0..3) {
                l[i] = bis!!.read().toLong()
                if (l[i] == -1L) {
                    throw IOException("no more data!!!")
                }
            }
            res = l[0] or (l[1] shl 8) or (l[2] shl 16) or (l[3] shl 24)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return res
    }

    private fun readBytes(len: Int): ByteArray {
        val buf = ByteArray(len)
        try {
            if (bis!!.read(buf) != len) {
                throw IOException("no more data!!!")
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return buf
    }

    companion object {
        fun readSingleChannel(filename: String): IntArray? {
            if (filename.isEmpty()) {
                return null
            }
            try {
                val reader = WaveFileInfoHelper(filename)
                return reader.data?.get(0)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return null
        }
    }
}