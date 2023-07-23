package com.baishengye.libaudio.playhelper.mediacodec.acc

import android.annotation.SuppressLint
import android.media.*
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import com.baishengye.libaudio.config.decode.MediaPlayException
import com.baishengye.libaudio.config.decode.MediaPlayState
import com.baishengye.libaudio.config.encode.MediaRecordException
import com.baishengye.libaudio.playhelper.PlayHelper
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.util.concurrent.Executors


class AccPlayHelperWithMediaCodec constructor(
    protected val config: MediaCodecDecodeConfig,
    protected val pushTransport: AccPushTransportWithMediaCodec
) : PlayHelper {
    protected var bufferSizeInBytes: Int = 0 // 缓冲区大小

    @Volatile
    protected var playState: MediaPlayState = MediaPlayState.IDLE
        set(value) {
            field = value
            notifyState(value)
        }

    private var file: File? = null
    private var audioTrack: AudioTrack? = null
    private var mediaCodec: MediaCodec? = null
    private var mediaExtractor: MediaExtractor? = null
    private var inputStream: InputStream? = null
    private val executorService = Executors.newSingleThreadExecutor()

    private val handler: Handler = Handler(Looper.getMainLooper())
    private var playStateChange: ((MediaPlayState) -> Unit)? = null
    fun setPlayStateChange(playStateChange: ((MediaPlayState) -> Unit)) {
        this.playStateChange = playStateChange
    }

    @SuppressLint("MissingPermission")
    fun initAudioTrack() {
        if (audioTrack == null) {
            // 计算缓冲区大小
            bufferSizeInBytes = AudioTrack.getMinBufferSize(
                config.sampleRateInHz,//采样率
                config.audioDecodingFormat.channelConfig,//通道
                config.audioDecodingFormat.audioFormat//音频编码
            )

            //根据采样数计算新的缓冲区,使缓冲区大小除以每个样本所占字节数为正数，并每帧的样本数整除
            //假设：buffSizeInBytes = 1700
            //config.audioEncodingFormat.bytesPreSimple = 2
            //config.simplePreNotify = 160
            var simpleSize =
                (bufferSizeInBytes / config.audioDecodingFormat.bytesPreSimple)//1700/2=850
            if (simpleSize % config.simplePreNotify != 0) {//850/160=5余50
                simpleSize += (config.simplePreNotify - simpleSize % config.simplePreNotify)//850+160-50=960，960/160=6,可以除尽
                bufferSizeInBytes = simpleSize * config.audioDecodingFormat.bytesPreSimple//
            }


            audioTrack = AudioTrack.Builder()
                .setBufferSizeInBytes(bufferSizeInBytes)
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setSampleRate(config.sampleRateInHz)//采样率
                        .setEncoding(config.audioDecodingFormat.audioFormat)//音频编码位数
                        .setChannelMask(config.audioDecodingFormat.channelConfig)//声道类型
                        .build()
                )
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setLegacyStreamType(config.streamType)
                        .build()
                )
                .setTransferMode(config.mod)
                .build()

            if (audioTrack!!.state != AudioTrack.STATE_INITIALIZED) {
                throw IllegalStateException(
                    "AudioTrack 初始化失败，请检查是否有响应权限。" +
                            "或者使用了系统APP才能用的配置项，" +
                            "或者使用了该设备不支持的配置项。"
                )
            }
        }

        if (playState == MediaPlayState.STOP) {
            playState = MediaPlayState.IDLE
        }
    }


    private fun initMediaCodec() {
        if (TextUtils.isEmpty(config.mime) || config.format == null) {
            return
        }
        try {
            mediaCodec = MediaCodec.createDecoderByType(config.mime)
            mediaCodec!!.configure(config.format, null, null, 0)
        } catch (e: IOException) {
            e.printStackTrace()
            mediaCodec = null
        }
    }

    private fun initMediaExtractor(path: String) {
        try {
            mediaExtractor = MediaExtractor()
            mediaExtractor!!.setDataSource(path)
            val trackCount = mediaExtractor!!.trackCount
            for (i in 0 until trackCount) {
                val format = mediaExtractor!!.getTrackFormat(i)
                val mime = format.getString(MediaFormat.KEY_MIME)
                if (!TextUtils.isEmpty(mime) && mime!!.startsWith("audio/")) {
                    mediaExtractor!!.selectTrack(i)
                    config.format = format
                    config.mime = mime
                    break
                }
            }
        } catch (e: IOException) {
            mediaExtractor = null
        }
    }

    override fun startPlaying(path: String) {
        file = File(path)

        initAudioTrack()
        initMediaExtractor(path)
        initMediaCodec()

        if (playState != MediaPlayState.IDLE) {
            throw MediaPlayException("状态异常，此时播放器状态为:${playState}")
        }

        executorService.submit { playState = MediaPlayState.PLAYING }
    }

    private fun startPlay() {
        try {
            if (inputStream == null) {
                inputStream = FileInputStream(file!!)
            }
            val startMs = System.currentTimeMillis()
            val info = MediaCodec.BufferInfo()


            audioTrack!!.play()
            mediaCodec!!.start()

            pushTransport.isEnableToBePushed(true)
            pushTransport.startPoolingAndReading(
                audioTrack!!,
                mediaCodec!!,
                mediaExtractor!!,
                startMs,
                info,
                bufferSizeInBytes,
                inputStream!!
            ) {
                if (it) {
                    stopPlaying()
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun resumePlaying() {
        initAudioTrack()

        if (playState != MediaPlayState.PAUSE) {
            throw MediaRecordException("状态异常，此时播放器状态为:${playState}")
        }

        executorService.submit { playState = MediaPlayState.PLAYING }
    }

    override fun pausePlaying() {
        pushTransport.isEnableToBePushed(false)

        if (playState != MediaPlayState.PLAYING) {
            throw MediaRecordException("状态异常，此时播放器状态为:${playState}")
        }
        playState = MediaPlayState.PAUSE
    }

    override fun stopPlaying() {
        pushTransport.isEnableToBePushed(false)

        if (playState == MediaPlayState.STOP || playState == MediaPlayState.IDLE) {
            throw MediaRecordException("状态异常，此时播放器状态为:${playState}")
        }
        playState = MediaPlayState.STOP
    }


    private fun stopPlay() {
        if (audioTrack != null) {
            audioTrack!!.stop()
        }
        if (mediaCodec != null) {
            mediaCodec!!.stop()
        }
    }

    fun isPlaying(): Boolean {
        return playState == MediaPlayState.PLAYING
    }

    fun isPaused(): Boolean {
        return playState == MediaPlayState.PAUSE
    }

    private fun notifyState(state: MediaPlayState) {
        //播放状态监听
        handler.post {
            playStateChange?.invoke(state)
        }

        when (state) {
            MediaPlayState.STOP -> releaseResource()
            MediaPlayState.PAUSE -> stopPlay()
            MediaPlayState.PLAYING -> startPlay()
            else -> {}
        }
    }

    private fun releaseResource() {
        if (audioTrack != null) {
            audioTrack!!.stop()
            audioTrack!!.release()
            audioTrack = null
        }
        if (mediaExtractor != null) {
            mediaExtractor!!.release();
            mediaExtractor = null;
        }

        if (mediaCodec != null) {
            mediaCodec!!.stop();
            mediaCodec!!.release();
            mediaCodec = null;
        }
        if (inputStream != null) {
            try {
                inputStream!!.close()
                inputStream = null
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
}