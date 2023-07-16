package com.baishengye.libaudio.helper

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

object AudioFormatConvertHelper {
    fun convertWavToPcm(wavFilePath: String, pcmFilePath: String) {
        val wavFile = File(wavFilePath)
        val pcmFile = File(pcmFilePath)

        // 读取 WAV 文件
        val wavData = ByteArray(wavFile.length().toInt())
        val wavInputStream = FileInputStream(wavFile)
        wavInputStream.read(wavData)
        wavInputStream.close()

        // 解析 WAV 文件头
        val sampleRate = ByteBuffer.wrap(wavData.copyOfRange(24, 28)).order(ByteOrder.LITTLE_ENDIAN).int
        val bitsPerSample = ByteBuffer.wrap(wavData.copyOfRange(34, 36)).order(ByteOrder.LITTLE_ENDIAN).short.toInt()
        val dataOffset = ByteBuffer.wrap(wavData.copyOfRange(40, 44)).order(ByteOrder.LITTLE_ENDIAN).int
        val dataSize = ByteBuffer.wrap(wavData.copyOfRange(44, 48)).order(ByteOrder.LITTLE_ENDIAN).int

        // 提取音频数据并写入 PCM 文件
        val pcmOutputStream = FileOutputStream(pcmFile)
        val wavDataSize = wavData.size - dataOffset
        val pcmDataSize = dataSize / (bitsPerSample / 8)
        val pcmData = ByteArray(pcmDataSize)
        System.arraycopy(wavData, dataOffset, pcmData, 0, pcmDataSize)
        pcmOutputStream.write(pcmData)
        pcmOutputStream.close()
    }
}