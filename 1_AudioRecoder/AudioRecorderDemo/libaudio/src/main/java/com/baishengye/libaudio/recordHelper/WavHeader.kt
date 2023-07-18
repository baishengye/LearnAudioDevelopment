package com.baishengye.libaudio.recordHelper

import com.baishengye.libaudio.config.AudioRecordConfig

/**
 * WAV文件头工具类
 */
class WavHeader internal constructor(// wav录音配置参数
    private val config: AudioRecordConfig, // 音频数据总长度
    private val totalAudioLength: Long
) {
    /**
     * 返回WAV文件头的byte数组
     */
    fun toBytes(): ByteArray {
        val sampleRateInHz = config.sampleRateInHz.toLong()
        val bytesPerSample: Byte = config.bytesPerSample()
        return wavFileHeader(
            totalAudioLength - 44,
            totalAudioLength - 8,
            sampleRateInHz,
            config.channels(),
            bytesPerSample * sampleRateInHz,
            (bytesPerSample / 8).toByte()
        )
    }

    /**
     * 获取wav文件头
     *
     * @param totalAudioLen  - 音频数据总长度
     * @param totalDataLen   - 文件总长度-8
     * @param longSampleRate - 采样率
     * @param channels       - 通道数
     * @param byteRate       - 每秒数据字节数
     * @param bitsPerSimple  - 每次采样(一帧)所占位数
     * @return 文件头
     */
    private fun wavFileHeader(
        totalAudioLen: Long, totalDataLen: Long, longSampleRate: Long,
        channels: Int, byteRate: Long, bitsPerSimple: Byte
    ): ByteArray {
        val header = ByteArray(44)
        // --- RIFF区块 ---
        // 文档标识: 大写字符串"RIFF"，标明该文件为有效的 RIFF 格式文档。
        header[0] = 'R'.code.toByte()
        header[1] = 'I'.code.toByte()
        header[2] = 'F'.code.toByte()
        header[3] = 'F'.code.toByte()
        // 文件数据长度: 从下一个字段首地址开始到文件末尾的总字节数。该值 = fileSize - 8。
        header[4] = (totalDataLen and 0xffL).toByte()
        header[5] = (totalDataLen shr 8 and 0xffL).toByte()
        header[6] = (totalDataLen shr 16 and 0xffL).toByte()
        header[7] = (totalDataLen shr 24 and 0xffL).toByte()
        // 文件格式类型: 所有 WAV 格式的文件此处为字符串"WAVE"，标明该文件是 WAV 格式文件。
        header[8] = 'W'.code.toByte()
        header[9] = 'A'.code.toByte()
        header[10] = 'V'.code.toByte()
        header[11] = 'E'.code.toByte()

        // --- FORMAT区块 ---
        // 格式块标识: 小写字符串"fmt "。
        header[12] = 'f'.code.toByte()
        header[13] = 'm'.code.toByte()
        header[14] = 't'.code.toByte()
        header[15] = ' '.code.toByte()
        // 格式块长度: 取决于编码格式，可以是 16、18、20、40 等
        header[16] = 16
        header[17] = 0
        header[18] = 0
        header[19] = 0
        // AudioFormat(音频格式): 常见的 PCM 音频数据的值为1。
        header[20] = 1
        header[21] = 0
        // NumChannels(声道数): 1：单声道，2：双声道/立体声
        header[22] = channels.toByte()
        header[23] = 0
        // SampleRate(采样率): 每个声道单位时间采样次数。常用的采样频率有 11025, 22050 和 44100 kHz。
        header[24] = (longSampleRate and 0xffL).toByte()
        header[25] = (longSampleRate shr 8 and 0xffL).toByte()
        header[26] = (longSampleRate shr 16 and 0xffL).toByte()
        header[27] = (longSampleRate shr 24 and 0xffL).toByte()
        // ByteRate(数据传输速率): 每秒数据字节数，该数值为:声道数×采样率×采样位数/8。
        header[28] = (byteRate and 0xffL).toByte()
        header[29] = (byteRate shr 8 and 0xffL).toByte()
        header[30] = (byteRate shr 16 and 0xffL).toByte()
        header[31] = (byteRate shr 24 and 0xffL).toByte()
        // BlockAlign(数据块对齐): 采样帧大小(一次采样)。该数值为:声道数×采样位数/8。
        header[32] = bitsPerSimple
        header[33] = 0
        // BitsPerSample(采样位数): 每个采样存储的bit数。常见的位数有 8、16、32
        header[34] = bitsPerSimple
        header[35] = 0

        // --- DATA区块 ---
        // 标识: 标示头结束，开始数据区域。
        header[36] = 'd'.code.toByte()
        header[37] = 'a'.code.toByte()
        header[38] = 't'.code.toByte()
        header[39] = 'a'.code.toByte()
        // 音频数据长度: N = ByteRate * seconds
        header[40] = (totalAudioLen and 0xffL).toByte()
        header[41] = (totalAudioLen shr 8 and 0xffL).toByte()
        header[42] = (totalAudioLen shr 16 and 0xffL).toByte()
        header[43] = (totalAudioLen shr 24 and 0xffL).toByte()
        return header
    }
}