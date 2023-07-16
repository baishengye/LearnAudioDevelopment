package com.baishengye.libaudio.player

interface PlayHelper {
    /**
     * 开始播放
     */
    fun startPlaying(path:String)

    /**
     * 暂停播放
     */
    fun pausePlaying()

    /**
     * 继续播放
     */
    fun resumePlaying()

    /**
     * 停止播放
     */
    fun stopPlaying()
}