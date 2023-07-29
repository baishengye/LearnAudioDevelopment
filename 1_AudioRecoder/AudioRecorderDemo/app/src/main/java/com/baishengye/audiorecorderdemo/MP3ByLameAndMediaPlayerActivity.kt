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
import com.baishengye.libbase.base.BaseViewBindingActivity
import com.baishengye.liblame.record.PullTransport
import com.baishengye.liblame.record.RecordHelper
import com.baishengye.liblame.record.pcm.Mp3RecordHelper
import com.baishengye.liblame.record.pcm.PcmEncodeConfig
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

class MP3ByLameAndMediaPlayerActivity : BaseViewBindingActivity<ActivityWavBinding>() {
    private var mp3RecordHelper: RecordHelper? = null
    private val mp3playHelper: MediaPlayHelper = MediaPlayHelper()

    private var mp3DirPath: String? = null
    private var recordFile: File? = null
    private var mp3FilesName: MutableList<String> = mutableListOf()

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
        }.models = mp3FilesName
    }

    private fun startPlay(fileName: String) {
        mp3playHelper.let { mediaPlayHelper ->
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
            mediaPlayHelper.startPlaying(mp3DirPath + fileName)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                binding.sbBar.min = 0
            }
            binding.sbBar.progress = 0

            //----------定时器记录播放进度---------//
            val mTimerTask: TimerTask = object : TimerTask() {
                override fun run() {
                    this@MP3ByLameAndMediaPlayerActivity.runOnUiThread {
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
        mp3DirPath = FolderUtils.getAudioFolderPath(this)
        Log.d("TEST", "pcmDirPath:${mp3DirPath}")
    }

    @SuppressLint("MissingPermission", "NotifyDataSetChanged")
    override fun initListeners() {
        binding.tvPlay.setOnClickListener {
            if (mp3playHelper.isPlaying()) {
                mp3playHelper.pausePlaying()
            } else {
                mp3playHelper.resumePlaying()
            }
        }

        binding.tvCancel.setOnClickListener {
            if (mp3playHelper.isPlaying() || mp3playHelper.isPaused()) {
                mp3playHelper.stopPlaying()
            }
        }

        binding.btnWavRecordStartStop.setOnClickListener {
            if (binding.btnWavRecordStartStop.text == "开始录音" && !TextUtils.isEmpty(mp3DirPath)) {
                binding.btnWavRecordStartStop.text = "停止录音"
                binding.btnWavRecordPauseResume.text = "暂停"
                binding.btnWavRecordPauseResume.visibility = View.VISIBLE

                recordFile =
                    File(mp3DirPath + "record_${DateUtil.calenderToFormatString(Calendar.getInstance())}.mp3")
                val pcmEncodeConfig = PcmEncodeConfig()
                mp3RecordHelper = Mp3RecordHelper(
                    recordFile!!,
                    pcmEncodeConfig,
                    PullTransport.Mp3PullTransport(pcmEncodeConfig)
                )
                mp3RecordHelper?.startRecording()

            } else if (!TextUtils.isEmpty(mp3DirPath)) {
                binding.btnWavRecordStartStop.text = "开始录音"
                binding.btnWavRecordPauseResume.visibility = View.GONE

                mp3RecordHelper?.stopRecording()
                recordFile = null
                mp3RecordHelper = null
            }
        }

        binding.btnWavRecordPauseResume.setOnClickListener {
            if (binding.btnWavRecordPauseResume.text == "暂停" && !TextUtils.isEmpty(mp3DirPath)) {
                binding.btnWavRecordPauseResume.text = "继续"

                mp3RecordHelper?.pauseRecording()

            } else if (!TextUtils.isEmpty(mp3DirPath)) {
                binding.btnWavRecordPauseResume.text = "暂停"

                mp3RecordHelper?.resumeRecording()
            }
        }

        binding.btnWavRefreshAudio.setOnClickListener {
            // 在协程中执行异步任务
            CoroutineScope(Dispatchers.Main).launch {
                val result = withContext(Dispatchers.IO) {
                    mp3FilesName.clear()
                    // 在 IO 线程执行耗时操作，例如网络请求或数据库查询
                    mp3FilesName.addAll(getAllPcmFiles(mp3DirPath!!, ".mp3"))
                }

                binding.rvAudioList.adapter?.notifyDataSetChanged()
            }
        }
    }
}