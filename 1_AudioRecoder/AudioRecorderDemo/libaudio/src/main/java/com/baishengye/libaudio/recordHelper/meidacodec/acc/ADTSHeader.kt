package com.baishengye.libaudio.recordHelper.meidacodec.acc

object ADTSHeader {
    val sampleRateMap: Map<Int, Int> = mapOf(
        Pair(96000, 0),
        Pair(88200, 1),
        Pair(64000, 2),
        Pair(48000, 3),
        Pair(44100, 4),
        Pair(32000, 5),
        Pair(24000, 6),
        Pair(22050, 7),
        Pair(16000, 8),
        Pair(12000, 9),
        Pair(11025, 10),
        Pair(8000, 11),
        Pair(7350, 12)
    )
}