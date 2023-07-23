package com.baishengye.audiorecorderdemo

import android.annotation.SuppressLint
import android.os.Build
import android.text.TextUtils
import android.util.Log
import android.view.View
import com.baishengye.audiorecorderdemo.databinding.ActivityWavBinding
import com.baishengye.audiorecorderdemo.databinding.ItemAudioFileBinding
import com.baishengye.libaudio.config.decode.MediaPlayState.*
import com.baishengye.libaudio.playhelper.PushTransport
import com.baishengye.libaudio.playhelper.pcm.PcmDecodeConfig
import com.baishengye.libaudio.playhelper.wav.WavPlayHelper
import com.baishengye.libaudio.recordHelper.PullTransport
import com.baishengye.libaudio.recordHelper.RecordHelper
import com.baishengye.libaudio.recordHelper.RecordHelperCreator
import com.baishengye.libaudio.recordHelper.pcm.PcmEncodeConfig
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

class WavByAudioRecordAndAudioTrackActivity : BaseViewBindingActivity<ActivityWavBinding>() {
    private var wavRecordHelper: RecordHelper? = null
    private val wavPlayHelper: WavPlayHelper =
        WavPlayHelper(PcmDecodeConfig(), PushTransport.Default())

    private var wavDirPath: String? = null
    private var recordFile: File? = null
    private var wavFilesName: MutableList<String> = mutableListOf()

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
        }.models = wavFilesName
    }

    private fun startPlay(fileName: String) {
        wavPlayHelper.apply {
            setPlayStateChange { mediaPlayState ->
                when (mediaPlayState) {
                    STOP -> binding.llPlayer.visibility = View.GONE
                    PAUSE -> binding.tvPlay.text = "播放"
                    IDLE -> binding.llPlayer.visibility = View.VISIBLE
                    PLAYING -> {
                        binding.sbBar.max = 100
                        binding.tvRightTime.text = formatTime(0)
                        binding.tvPlay.text = "暂停"
                    }
                }
            }
            startPlaying(wavDirPath + fileName)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                binding.sbBar.min = 0
            }
            binding.sbBar.progress = 0

            //----------定时器记录播放进度---------//
            val mTimerTask: TimerTask = object : TimerTask() {
                override fun run() {
                    this@WavByAudioRecordAndAudioTrackActivity.runOnUiThread {
                        if (isPlaying()) {
                            binding.sbBar.progress = 0
                            binding.tvLeftTime.text = formatTime(0)
                        }
                    }
                }
            }
            Timer().schedule(mTimerTask, 0, 1000)
        }
    }

    override fun initData() {
        wavDirPath = FolderUtils.getAudioFolderPath(this)
        Log.d("TEST", "wavDirPath:${wavDirPath}")
    }

    @SuppressLint("MissingPermission", "NotifyDataSetChanged")
    override fun initListeners() {
        binding.tvPlay.setOnClickListener {
            if (wavPlayHelper.isPlaying()) {
                wavPlayHelper.pausePlaying()
            } else {
                wavPlayHelper.resumePlaying()
            }
        }

        binding.tvCancel.setOnClickListener {
            if (wavPlayHelper.isPlaying() || wavPlayHelper.isPaused()) {
                wavPlayHelper.stopPlaying()
            }
        }

        binding.btnWavRecordStartStop.setOnClickListener {
            if (binding.btnWavRecordStartStop.text == "开始录音" && !TextUtils.isEmpty(wavDirPath)) {
                binding.btnWavRecordStartStop.text = "停止录音"
                binding.btnWavRecordPauseResume.text = "暂停"
                binding.btnWavRecordPauseResume.visibility = View.VISIBLE

                recordFile =
                    File(wavDirPath + "record_${DateUtil.calenderToFormatString(Calendar.getInstance())}.wav")
                wavRecordHelper = RecordHelperCreator.wav(
                    recordFile!!,
                    PcmEncodeConfig(),
                    PullTransport.Default()
                )
                wavRecordHelper?.startRecording()

            } else if (!TextUtils.isEmpty(wavDirPath)) {
                binding.btnWavRecordStartStop.text = "开始录音"
                binding.btnWavRecordPauseResume.visibility = View.GONE

                wavRecordHelper?.stopRecording()
                recordFile = null
                wavRecordHelper = null
            }
        }

        binding.btnWavRecordPauseResume.setOnClickListener {
            if (binding.btnWavRecordPauseResume.text == "暂停" && !TextUtils.isEmpty(wavDirPath)) {
                binding.btnWavRecordPauseResume.text = "继续"

                wavRecordHelper?.pauseRecording()

            } else if (!TextUtils.isEmpty(wavDirPath)) {
                binding.btnWavRecordPauseResume.text = "暂停"

                wavRecordHelper?.resumeRecording()
            }
        }

        binding.btnWavRefreshAudio.setOnClickListener {
            // 在协程中执行异步任务
            CoroutineScope(Dispatchers.Main).launch {
                val result = withContext(Dispatchers.IO) {
                    wavFilesName.clear()
                    // 在 IO 线程执行耗时操作，例如网络请求或数据库查询
                    wavFilesName.addAll(getAllPcmFiles(wavDirPath!!, ".wav"))
                }

                binding.rvAudioList.adapter?.notifyDataSetChanged()
            }
        }
    }
}