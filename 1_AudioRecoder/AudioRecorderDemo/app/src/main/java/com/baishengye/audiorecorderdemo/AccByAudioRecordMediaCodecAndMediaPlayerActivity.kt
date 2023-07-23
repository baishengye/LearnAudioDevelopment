package com.baishengye.audiorecorderdemo

import android.annotation.SuppressLint
import android.os.Build
import android.text.TextUtils
import android.util.Log
import android.view.View
import com.baishengye.audiorecorderdemo.databinding.ActivityWavBinding
import com.baishengye.audiorecorderdemo.databinding.ItemAudioFileBinding
import com.baishengye.libaudio.config.decode.MediaPlayState.*
import com.baishengye.libaudio.playhelper.MediaPlayHelper
import com.baishengye.libaudio.recordHelper.meidacodec.acc.AccAudioRecordHelperWithMediaCodec
import com.baishengye.libaudio.recordHelper.meidacodec.acc.AccPullTransportWithMediaCodec
import com.baishengye.libaudio.recordHelper.meidacodec.acc.MediaCodecEncodeConfig
import com.baishengye.libbase.base.BaseViewBindingActivity
import com.baishengye.libutil.utils.DateUtil
import com.baishengye.libutil.utils.DateUtil.formatTime
import com.baishengye.libutil.utils.FolderUtils
import com.drake.brv.utils.linear
import com.drake.brv.utils.setup
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.*

class AccByAudioRecordMediaCodecAndMediaPlayerActivity :
    BaseViewBindingActivity<ActivityWavBinding>() {
    private var accRecordHelper: AccAudioRecordHelperWithMediaCodec? = null
    private val accPlayHelper: MediaPlayHelper = MediaPlayHelper()

    private var accDirPath: String? = null
    private var recordFile: File? = null
    private var accFilesName: MutableList<String> = mutableListOf()

    override fun getViewBinding(): ActivityWavBinding = ActivityWavBinding.inflate(layoutInflater)

    override fun initViews() {

        binding.rvAudioList.linear().setup {
            addType<String>(R.layout.item_audio_file)

            onBind {
                val binding = getBinding<ItemAudioFileBinding>()
                binding.tvAudio.text = getModel<String>()
            }

            onClick(R.id.flAudio) {
                val fileName = getModel<String>()

                startPlay(fileName)
            }
        }.models = accFilesName
    }

    private fun startPlay(fileName: String) {
        accPlayHelper.let { mediaPlayHelper ->
            mediaPlayHelper.setOnPlayStateChange { mediaPlayState ->
                when (mediaPlayState) {
                    STOP -> binding.llPlayer.visibility = View.GONE
                    PAUSE -> binding.tvPlay.text = "播放"
                    IDLE -> binding.llPlayer.visibility = View.VISIBLE
                    PLAYING -> {
                        binding.sbBar.max = mediaPlayHelper.getDur()
                        binding.tvRightTime.text = formatTime(mediaPlayHelper.getDur())
                        binding.tvPlay.text = "暂停"
                    }
                }
            }
            mediaPlayHelper.startPlaying(accDirPath + fileName)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                binding.sbBar.min = 0
            }
            binding.sbBar.progress = 0

            //----------定时器记录播放进度---------//
            val mTimerTask: TimerTask = object : TimerTask() {
                override fun run() {
                    this@AccByAudioRecordMediaCodecAndMediaPlayerActivity.runOnUiThread {
                        if (mediaPlayHelper.isPlaying()) {
                            binding.sbBar.progress = mediaPlayHelper.getCurrPos()
                            binding.tvLeftTime.text = formatTime(mediaPlayHelper.getCurrPos())
                        }
                    }
                }
            }
            Timer().schedule(mTimerTask, 0, 1000)
        }
    }

    override fun initData() {
        accDirPath = FolderUtils.getAudioFolderPath(this)
        Log.d("TEST", "pcmDirPath:${accDirPath}")
    }

    @SuppressLint("MissingPermission", "NotifyDataSetChanged")
    override fun initListeners() {
        binding.tvPlay.setOnClickListener {
            if (accPlayHelper.isPlaying()) {
                accPlayHelper.pausePlaying()
            } else {
                accPlayHelper.resumePlaying()
            }
        }

        binding.tvCancel.setOnClickListener {
            if (accPlayHelper.isPlaying() || accPlayHelper.isPaused()) {
                accPlayHelper.stopPlaying()
            }
        }

        binding.btnWavRecordStartStop.setOnClickListener {
            if (binding.btnWavRecordStartStop.text == "开始录音" && !TextUtils.isEmpty(accDirPath)) {
                binding.btnWavRecordStartStop.text = "停止录音"
                binding.btnWavRecordPauseResume.text = "暂停"
                binding.btnWavRecordPauseResume.visibility = View.VISIBLE

                recordFile =
                    File(accDirPath + "record_${DateUtil.calenderToFormatString(Calendar.getInstance())}.acc")
                val mediaCodecEncodeConfig = MediaCodecEncodeConfig()
                accRecordHelper = AccAudioRecordHelperWithMediaCodec(
                    recordFile!!, mediaCodecEncodeConfig,
                    AccPullTransportWithMediaCodec.Default()
                        .setMediaCodecEncodeConfig(mediaCodecEncodeConfig)
                )
                accRecordHelper?.startRecording()

            } else if (!TextUtils.isEmpty(accDirPath)) {
                binding.btnWavRecordStartStop.text = "开始录音"
                binding.btnWavRecordPauseResume.visibility = View.GONE

                accRecordHelper?.stopRecording()
                recordFile = null
                accRecordHelper = null
            }
        }

        binding.btnWavRecordPauseResume.setOnClickListener {
            if (binding.btnWavRecordPauseResume.text == "暂停" && !TextUtils.isEmpty(accDirPath)) {
                binding.btnWavRecordPauseResume.text = "继续"

                accRecordHelper?.pauseRecording()

            } else if (!TextUtils.isEmpty(accDirPath)) {
                binding.btnWavRecordPauseResume.text = "暂停"

                accRecordHelper?.resumeRecording()
            }
        }

        binding.btnWavRefreshAudio.setOnClickListener {
            // 在协程中执行异步任务
            CoroutineScope(Dispatchers.Main).launch {
                val result = withContext(Dispatchers.IO) {
                    accFilesName.clear()
                    // 在 IO 线程执行耗时操作，例如网络请求或数据库查询
                    accFilesName.addAll(getAllPcmFiles(accDirPath!!, ".acc"))
                }

                binding.rvAudioList.adapter?.notifyDataSetChanged()
            }
        }
    }
}