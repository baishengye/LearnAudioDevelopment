package com.baishengye.liblame

class LameBuilder {
    enum class Mode {
        STEREO, JSTEREO, MONO, DEFAULT
    }

    enum class VbrMode {
        VBR_OFF, VBR_RH, VBR_MTRH, VBR_ABR, VBR_DEFAULT
    }

    var inSampleRate = 44100
    var outSampleRate = 0
    var outBitrate = 128
    var outChannel = 2
    var quality = 5
    var vbrQuality: Int
    var abrMeanBitrate: Int
    var lowpassFreq: Int
    var highpassFreq: Int
    var scaleInput = 1f
    var mode: Mode
    var vbrMode: VbrMode
    var id3tagTitle: String? = null
    var id3tagArtist: String? = null
    var id3tagAlbum: String? = null
    var id3tagComment: String? = null
    var id3tagYear: String? = null

    init {

        //default 0, Lame picks best according to compression
        mode = Mode.DEFAULT
        vbrMode = VbrMode.VBR_OFF
        vbrQuality = 5
        abrMeanBitrate = 128

        //default =0, Lame chooses
        lowpassFreq = 0
        highpassFreq = 0
    }

    /*设置质量*/
    fun setQuality(quality: Int): LameBuilder {
        this.quality = quality
        return this
    }

    /*设置输入采样率*/
    fun setInSampleRate(inSampleRate: Int): LameBuilder {
        this.inSampleRate = inSampleRate
        return this
    }

    /*设置输出采样率*/
    fun setOutSampleRate(outSampleRate: Int): LameBuilder {
        this.outSampleRate = outSampleRate
        return this
    }

    /*输出比特率*/
    fun setOutBitrate(bitrate: Int): LameBuilder {
        outBitrate = bitrate
        return this
    }

    /*输出声道*/
    fun setOutChannels(channels: Int): LameBuilder {
        outChannel = channels
        return this
    }

    /*MP3标题(ID3)*/
    fun setId3tagTitle(title: String?): LameBuilder {
        id3tagTitle = title
        return this
    }

    /*MP3艺术家(ID3)*/
    fun setId3tagArtist(artist: String?): LameBuilder {
        id3tagArtist = artist
        return this
    }

    /*MP3专辑(ID3)*/
    fun setId3tagAlbum(album: String?): LameBuilder {
        id3tagAlbum = album
        return this
    }

    /*MP3备注(ID3)*/
    fun setId3tagComment(comment: String?): LameBuilder {
        id3tagComment = comment
        return this
    }

    /*MP3年(ID3)*/
    fun setId3tagYear(year: String?): LameBuilder {
        id3tagYear = year
        return this
    }

    /*振幅缩放比*/
    fun setScaleInput(scaleAmount: Float): LameBuilder {
        scaleInput = scaleAmount
        return this
    }

    /*声道模式*/
    fun setMode(mode: Mode): LameBuilder {
        this.mode = mode
        return this
    }

    /*VBR模式*/
    fun setVbrMode(mode: VbrMode): LameBuilder {
        vbrMode = mode
        return this
    }

    /*VBR输出音频质量*/
    fun setVbrQuality(quality: Int): LameBuilder {
        vbrQuality = quality
        return this
    }

    /*VBR模式下的平均比特率*/
    fun setAbrMeanBitrate(bitrate: Int): LameBuilder {
        abrMeanBitrate = bitrate
        return this
    }

    /*低通滤波器*/
    fun setLowpassFreqency(freq: Int): LameBuilder {
        lowpassFreq = freq
        return this
    }

    /*高通滤波器*/
    fun setHighpassFreqency(freq: Int): LameBuilder {
        highpassFreq = freq
        return this
    }

    fun initLame() {
        LameLoader.initialize(this)
    }
}