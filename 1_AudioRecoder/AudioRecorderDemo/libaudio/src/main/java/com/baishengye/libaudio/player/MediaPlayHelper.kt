package com.baishengye.libaudio.player

import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import com.baishengye.libaudio.config.MediaPlayException
import com.baishengye.libaudio.config.MediaPlayState

class MediaPlayHelper : PlayHelper {

    private var player: MediaPlayer? = null

    private var playState:MediaPlayState = MediaPlayState.IDLE
    private val handler : Handler = Handler(Looper.getMainLooper())

    private var filePath:String ?= null

    private var onPlayStateChange: ((MediaPlayState) -> Unit)? = null
    fun setOnPlayStateChange(onPlayStateChange: ((MediaPlayState) -> Unit)){
        this.onPlayStateChange = onPlayStateChange
    }

    override fun startPlaying(path: String) {
        try {
            filePath = path
            player = MediaPlayer()
            player?.let { it ->
                playState = MediaPlayState.IDLE
                notifyState()
                it.setDataSource(filePath)
                it.prepareAsync()
                it.setOnPreparedListener {mediaPlayer-> // 装载完毕回调
                    playState = MediaPlayState.PLAYING
                    notifyState()
                    mediaPlayer.start()
                }

                it.setOnCompletionListener { stopPlaying() }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun notifyState() {
        handler.post {
            onPlayStateChange?.invoke(playState)
        }
    }

    override fun pausePlaying() {
        if(playState!=MediaPlayState.PLAYING){
            throw MediaPlayException("播放状态异常,当前状态为:${playState}")
        }

        try {
            if (player != null) {
                player!!.pause()
                playState = MediaPlayState.PAUSE
                notifyState()
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    override fun resumePlaying() {
        if(playState!=MediaPlayState.PAUSE){
            throw MediaPlayException("播放状态异常,当前状态为:${playState}")
        }
        try {
            player?.let {
                it.start()
                playState = MediaPlayState.PLAYING
                notifyState()
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    override fun stopPlaying() {
        if(playState==MediaPlayState.STOP||playState==MediaPlayState.IDLE){
            throw MediaPlayException("播放状态异常,当前状态为:${playState}")
        }
        try {
            player?.let {
                it.stop()
                it.reset()
                playState = MediaPlayState.STOP
                notifyState()
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    fun getCurrPos():Int{
        if(player!=null){
            return player!!.currentPosition
        }
        return 0
    }

    fun getDur():Int{
        if(player!=null){
            return player!!.duration
        }
        return 0
    }

    fun isPlaying():Boolean{
        return playState == MediaPlayState.PLAYING
    }

    fun isPaused():Boolean{
        return playState == MediaPlayState.PAUSE
    }
}